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

import android.Manifest
import android.accounts.Account
import android.accounts.AccountManager
import android.accounts.AccountManagerCallback
import android.accounts.AccountManagerFuture
import android.app.Application
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Build
import android.os.Bundle
import android.support.annotation.RequiresPermission


/**
 * This class wraps the Android [android.accounts.AccountManager] and adds some retroauth specific
 * functionality.
 */
class AuthAccountManager internal constructor(
        private val application: Application,
        private val accountManager: AccountManager) {

    private val activityManager: ActivityManager = ActivityManager[application]

    companion object {
        const val RETROAUTH_ACCOUNT_NAME_KEY = "com.andretietz.retroauth.ACTIVE_ACCOUNT"
    }

    constructor(application: Application) : this(application, AccountManager.get(application))


    /**
     * This method returns the name of the active account of the chosen accountType.
     *
     * @param accountType of which you want to get the active accountname of
     * @return the name of the currently active account or `null`
     */
    fun getActiveAccountName(accountType: String): String? {
        val preferences = application.getSharedPreferences(accountType, Context.MODE_PRIVATE)
        return preferences.getString(RETROAUTH_ACCOUNT_NAME_KEY, null)
    }

    /**
     * @param accountType of which you want to get the active account
     * @return the currently active account or `null`
     */
    fun getActiveAccount(accountType: String): Account? {
        val accountName = getActiveAccountName(accountType)
        return if (accountName != null) {
            getAccountByName(accountType, accountName)
        } else null
    }

    /**
     * When calling this method make sure you have the correct permission to read this accountType. Since you
     * propably want to read your own account number, no permission is required for this.
     * If not, you need GET_ACCOUNTS permission
     *
     * @param accountType of which you want to get the active account
     * @param accountName account name you're searching for
     * @return the account if found. `null` if not
     */
    fun getAccountByName(accountType: String, accountName: String): Account? {
        val accounts = accountManager.getAccountsByType(accountType)
        for (account in accounts) {
            if (accountName == account.name) return account
        }
        return null
    }

    /**
     * @param accountType of which you want to get the active account
     * @param key         in which you stored userdata using
     * [AuthenticationActivity.storeUserData]
     * @return the userdata stored, using the given key or `null` if there's no userdata stored within the key
     */
    fun getActiveUserData(accountType: String, key: String): String? {
        return accountManager.getUserData(getActiveAccount(accountType), key)
    }

    /**
     * This will store the username of an accountType (different accountTypes can have the same username) in the
     * [SharedPreferences].
     *
     * @param accountType of which you want to get the active account
     * @param accountName account name you want to set as active
     * @return the account which is not the currently active user
     */
    fun setActiveAccount(accountType: String, accountName: String): Account? {
        val account = getAccountByName(accountType, accountName) ?: return null
        val preferences = application.getSharedPreferences(accountType, Context.MODE_PRIVATE)
        preferences.edit().putString(RETROAUTH_ACCOUNT_NAME_KEY, accountName).apply()
        return account
    }

    /**
     * Deletes the currently active user from the [SharedPreferences]. When the user next time calls an
     * [Authenticated] Request, he'll be asked which user to use as active user. This will be saved after choosing
     *
     * @param accountType accountType to reset
     */
    fun resetActiveAccount(accountType: String) {
        val preferences = application.getSharedPreferences(accountType, Context.MODE_PRIVATE)
        preferences.edit().remove(RETROAUTH_ACCOUNT_NAME_KEY).apply()
    }


    /**
     * Adds a new account for the given account type. The tokenType is optional. you can request this type in the login
     * [android.support.v7.app.AppCompatActivity] calling [AuthenticationActivity.getRequestedTokenType].
     * This value will not be available when you're creating an account from Android-Settings-Accounts-Add Account
     *
     * @param accountType the account type you want to create an account for
     * @param tokenType   the type of token you want to create
     * @param callback    which is called when the account has been created or account creation was canceled.
     */
    @JvmOverloads
    fun addAccount(accountType: String, tokenType: String? = null, callback: AccountCallback? = null) {
        val cac = if (callback != null) CreateAccountCallback(callback) else null
        val activity = activityManager.activity
        accountManager.addAccount(
                accountType,
                tokenType,
                null,
                null,
                activity, cac,
                null)
    }

    /**
     * When calling this method make sure you have the correct permission to read this accountType. Since you
     * propably want to read your own account number, no permission is required for this.
     * If not, you need GET_ACCOUNTS permission
     *
     * @param accountType AccountType which you want to know the amount of
     * @return number of existing accounts of this type. Depending on which accountType you're requesting this could
     * require additional permissions
     */
    fun accountAmount(accountType: String): Int {
        return accountManager.getAccountsByType(accountType).size
    }

    /**
     * Removes the currently active account
     *
     * @param accountType the account type of which you want to delete the active user from
     * @param callback    callback returns, when account was deleted.
     */
    @JvmOverloads
    fun removeActiveAccount(accountType: String, callback: AccountCallback? = null) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP_MR1) {
            val rac = if (callback != null) RemoveLollipopAccountCallback(callback) else null
            accountManager.removeAccount(getActiveAccount(accountType), null, rac, null)
        } else {
            val rac = if (callback != null) RemoveAccountCallback(callback) else null
            @Suppress("DEPRECATION")
            accountManager.removeAccount(getActiveAccount(accountType), rac, null)
        }
        resetActiveAccount(accountType)
    }

    /**
     * Returns an intent to open an account chooser
     *
     * @param accountType Make sure this is your account type or you requested the permission for
     * [Manifest.permission.GET_ACCOUNTS], otherwise there will be a [SecurityException]
     * thrown
     * @return an Intent which you can start for result to open an account chooser.
     */
    @RequiresPermission(Manifest.permission.GET_ACCOUNTS)
    fun newChooseAccountIntent(accountType: String): Intent {
        val accounts = accountManager.getAccountsByType(accountType).toMutableList()
        val account = getActiveAccount(accountType)
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            AccountManager.newChooseAccountIntent(
                    account,
                    accounts,
                    arrayOf(accountType),
                    null,
                    null,
                    null,
                    null)
        } else {
            @Suppress("DEPRECATION")
            AccountManager.newChooseAccountIntent(
                    account,
                    accounts as? ArrayList ?: ArrayList(accounts),
                    arrayOf(accountType),
                    false,
                    null,
                    null,
                    null,
                    null)
        }
    }

    interface AccountCallback {
        fun done(success: Boolean)
    }

    /**
     * Callback wrapper for adding an account
     */
    private class CreateAccountCallback(private val callback: AccountCallback) : AccountManagerCallback<Bundle> {

        override fun run(accountManagerFuture: AccountManagerFuture<Bundle>) {
            try {
                val accountName = accountManagerFuture.result.getString(AccountManager.KEY_ACCOUNT_NAME)
                callback.done(accountName != null)
            } catch (e: Exception) {
                callback.done(false)
            }
        }
    }

    /**
     * Callback wrapper for account removing on >= lollipop (22) devices
     */
    private class RemoveLollipopAccountCallback(private val callback: AccountCallback) : AccountManagerCallback<Bundle> {

        override fun run(accountManagerFuture: AccountManagerFuture<Bundle>) {
            try {
                callback.done(accountManagerFuture.result.getBoolean(AccountManager.KEY_BOOLEAN_RESULT))
            } catch (e: Exception) {
                callback.done(false)
            }
        }
    }

    /**
     * Callback wrapper for account removing on prelollipop (22) devices
     */
    private class RemoveAccountCallback(private val callback: AccountCallback) : AccountManagerCallback<Boolean> {

        override fun run(accountManagerFuture: AccountManagerFuture<Boolean>) {
            try {
                callback.done(accountManagerFuture.result)
            } catch (e: Exception) {
                callback.done(false)
            }

        }
    }
}