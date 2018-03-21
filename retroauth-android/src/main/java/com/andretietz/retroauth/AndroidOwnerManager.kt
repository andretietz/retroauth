/*
 * Copyright (c) 2016 Andre Tietz
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.andretietz.retroauth

import android.accounts.Account
import android.accounts.AccountManager
import android.app.AlertDialog
import android.app.Application
import android.content.Context
import android.os.Looper
import java.util.concurrent.locks.Condition
import java.util.concurrent.locks.Lock
import java.util.concurrent.locks.ReentrantLock

/**
 * This is the Android implementation of an [OwnerManager]. It does all the Android [Account] handling
 */
internal class AndroidOwnerManager(private val application: Application, private val accountManager: AuthAccountManager)
    : OwnerManager<Account, AndroidTokenType> {

    private val activityManager: ActivityManager = ActivityManager.get(application)

    @Throws(ChooseOwnerCanceledException::class)
    override fun getOwner(type: AndroidTokenType): Account? {
        // get active account name
        var accountName = accountManager.getActiveAccountName(type.accountType)
        // if this one exists, try to get the account
        if (accountName != null) return accountManager.getAccountByName(type.accountType, accountName)
        // if it doesn't, ask the user to pick an account
        accountName = showAccountPickerDialog(type.accountType, true)
        // if the user has chosen an existing account
        accountName?.let {
            accountManager.setActiveAccount(type.accountType, it)
            return accountManager.getAccountByName(type.accountType, it)
        }
        // if the user chose to add an account, handled by the android token storage
        return null
    }

    /**
     * Shows an account picker for the user to choose an account. Make sure you're calling this from a non-ui thread
     *
     * @param accountType   Account type of the accounts the user can choose
     * @param canAddAccount if `true` the user has the option to add an account
     * @return the accounts the user chooses from
     */
    @Throws(ChooseOwnerCanceledException::class)
    private fun showAccountPickerDialog(accountType: String, canAddAccount: Boolean): String? {
        if (Looper.myLooper() == Looper.getMainLooper()) {
            throw RuntimeException("Method was called from the wrong thread!")
        }
        val accounts = AccountManager.get(application).getAccountsByType(accountType)
        if (accounts.isEmpty()) return null
        val accountList = ArrayList<String>()
        for (i in accounts.indices) {
            accountList[i] = accounts[i].name
        }
        if (canAddAccount) {
            accountList[accounts.size] = application.getString(R.string.add_account_button_label)
        }
        val lock = ReentrantLock()
        val condition = lock.newCondition()
        val activity = activityManager.activity
        // show the account chooser
        val showDialog = ShowAccountChooser(application, activityManager, accountList.toTypedArray(), lock, condition)
        activity?.let {
            activity.runOnUiThread(showDialog)
            lock.lock()
            try {
                // wait until the user has chosen
                condition.await()
            } catch (e: InterruptedException) {
                // ignore
            } finally {
                lock.unlock()
            }
        }
        if (showDialog.canceled) {
            throw ChooseOwnerCanceledException("User canceled authentication!")
        }
        return showDialog.selectedOption
    }

    /**
     * This [Runnable] shows an [AlertDialog] where the user can choose an account or create a new one
     */
    private class ShowAccountChooser(
            private val context: Context,
            private val activityManager: ActivityManager,
            private val options: Array<String>,
            private val lock: Lock,
            private val condition: Condition) : Runnable {
        internal var canceled = false
        var selectedOption: String? = null

        init {
            this.selectedOption = options[0]
        }

        override fun run() {
            val builder = AlertDialog.Builder(activityManager.activity)
            builder.setTitle(context.getString(R.string.choose_account_label))
            builder.setCancelable(false)
            builder.setSingleChoiceItems(options, 0) { _, which ->
                if (which < options.size - 1) {
                    selectedOption = options[which]
                } else {
                    selectedOption = null
                }
            }
            builder.setPositiveButton(android.R.string.ok) { _, _ ->
                lock.lock()
                try {
                    condition.signal()
                } finally {
                    lock.unlock()
                }
            }
            builder.setNegativeButton(android.R.string.cancel) { _, _ ->
                canceled = true
                lock.lock()
                try {
                    condition.signal()
                } finally {
                    lock.unlock()
                }
            }
            builder.show()
        }
    }
}
