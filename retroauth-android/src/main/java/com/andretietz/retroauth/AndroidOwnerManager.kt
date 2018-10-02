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
import android.accounts.AccountManagerCallback
import android.accounts.AccountManagerFuture
import android.app.AlertDialog
import android.app.Application
import android.content.Context
import android.os.Build
import android.os.Bundle
import android.os.Looper
import android.view.WindowManager
import java.util.concurrent.Callable
import java.util.concurrent.CountDownLatch
import java.util.concurrent.Executors
import java.util.concurrent.Future
import java.util.concurrent.FutureTask
import java.util.concurrent.TimeUnit

/**
 * This is the Android implementation of an [OwnerManager]. It does all the Android [Account] handling
 */
@Suppress("unused")
class AndroidOwnerManager constructor(
  private val application: Application
) : OwnerManager<String, Account, AndroidTokenType> {

  companion object {
    private const val RETROAUTH_ACCOUNT_NAME_KEY = "com.andretietz.retroauth.ACTIVE_ACCOUNT"
  }

  private val activityManager by lazy { ActivityManager[application] }
  private val executor by lazy { Executors.newSingleThreadExecutor() }
  private val accountManager by lazy { AccountManager.get(application) }

  @Throws(AuthenticationCanceledException::class)
  override fun createOwner(
    ownerType: String,
    tokenType: AndroidTokenType,
    callback: Callback<Account>?
  ): Future<Account> {
    val future = accountManager.addAccount(
      ownerType,
      tokenType.tokenType,
      null,
      null,
      activityManager.activity,
      if (callback != null) CreateAccountCallback(callback) else null,
      null)
    return AccountFuture(future)
  }

  override fun getOwner(ownerType: String, ownerName: String): Account? {
    val accounts = accountManager.getAccountsByType(ownerType)
    for (account in accounts) {
      if (ownerName == account.name) return account
    }
    return null
  }

  override fun getActiveOwner(ownerType: String): Account? {
    val preferences = application.getSharedPreferences(ownerType, Context.MODE_PRIVATE)
    preferences.getString(RETROAUTH_ACCOUNT_NAME_KEY, null)?.let { accountName ->
      return getOwner(ownerType, accountName)
    }
    return null
  }

  override fun openOwnerPicker(ownerType: String, callback: Callback<Account?>?): Future<Account?> {
    val task = ShowDialogPickerTask(application, accountManager, ownerType, callback)
    if (Looper.myLooper() == Looper.getMainLooper()) {
      return executor.submit(task)
    }
    val future = FutureTask(task)
    future.run()
    return future
  }

  override fun switchActiveOwner(ownerType: String, owner: Account?) {
    val preferences = application.getSharedPreferences(ownerType, Context.MODE_PRIVATE)
    if (owner == null) {
      preferences.edit().remove(RETROAUTH_ACCOUNT_NAME_KEY).apply()
    } else {
      preferences.edit().putString(RETROAUTH_ACCOUNT_NAME_KEY, owner.name).apply()
    }
  }

  override fun removeOwner(owner: Account, callback: Callback<Boolean>?): Future<Boolean> {
    val accountFuture: Future<Boolean>
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP_MR1) {
      val rac = if (callback != null) RemoveLollipopAccountCallback(callback) else null
      accountFuture = RemoveAccountFuture(accountManager.removeAccount(owner, null, rac, null))
    } else {
      val rac = if (callback != null) RemoveAccountCallback(callback) else null
      @Suppress("DEPRECATION")
      accountFuture = PreLollipopRemoveAccountFuture(accountManager.removeAccount(owner, rac, null))
    }
    switchActiveOwner(owner.type)
    return accountFuture
  }

  private class ShowDialogPickerTask(
    private val application: Application,
    private val accountManager: AccountManager,
    private val accountType: String,
    private val callback: Callback<Account?>?
  ) : Callable<Account?> {

    private val activityManager = ActivityManager[application]

    override fun call(): Account? {
      val accounts = accountManager.getAccountsByType(accountType)
      if (accounts.isEmpty()) return null
      val accountList = accounts.map { it.name }.toMutableSet()
      accountList.add(application.getString(R.string.add_account_button_label))
      val countDownLatch = CountDownLatch(1)
      val activity = activityManager.activity
      // show the account chooser
      val showDialog = ShowAccountChooser(
        application,
        activityManager,
        accountList.toTypedArray(),
        countDownLatch)
      activity?.let {
        activity.runOnUiThread(showDialog)
        countDownLatch.await()
      }
      if (showDialog.canceled) {
        throw AuthenticationCanceledException("User canceled authentication!")
      }
      val accountName = showDialog.selectedOption
      for (account in accounts) {
        if (accountName == account.name) {
          val preferences = application.getSharedPreferences(accountType, Context.MODE_PRIVATE)
          preferences.edit().putString(RETROAUTH_ACCOUNT_NAME_KEY, account.name).apply()
          callback?.onResult(account)
          return account
        }
      }
      return null
    }
  }

  /**
   * This [Runnable] shows an [AlertDialog] where the user can choose an account or create a new one
   */
  private class ShowAccountChooser(
    private val context: Context,
    private val activityManager: ActivityManager,
    private val options: Array<String>,
    private val countDownLatch: CountDownLatch
  ) : Runnable {
    internal var canceled = false
    var selectedOption: String? = null

    init {
      this.selectedOption = options[0]
    }

    override fun run() {
      val builder = AlertDialog.Builder(activityManager.activity)
        .setTitle(context.getString(R.string.choose_account_label))
        .setCancelable(false)
        .setSingleChoiceItems(options, 0) { _, which ->
          selectedOption = if (which < options.size - 1) {
            options[which]
          } else null
        }
        .setNegativeButton(android.R.string.cancel) { _, _ -> canceled = true }
        .setPositiveButton(android.R.string.ok) { _, _ -> canceled = false }
      val dialog = builder.create()
      dialog.setOnDismissListener { countDownLatch.countDown() }
      dialog.show()
      dialog.window?.let { window ->
        window.attributes = WindowManager.LayoutParams().apply {
          copyFrom(window.attributes)
          width = WindowManager.LayoutParams.WRAP_CONTENT
          height = WindowManager.LayoutParams.WRAP_CONTENT
        }
      }
    }
  }

  /**
   * Callback wrapper for adding an account
   */
  private class CreateAccountCallback(private val callback: Callback<Account>) : AccountManagerCallback<Bundle> {

    override fun run(accountManagerFuture: AccountManagerFuture<Bundle>) {
      val accountName = accountManagerFuture.result.getString(AccountManager.KEY_ACCOUNT_NAME)
      if (accountName != null) {
        callback.onResult(Account(accountName,
          accountManagerFuture.result.getString(AccountManager.KEY_ACCOUNT_TYPE)))
        return
      }
    }
  }

  /**
   * Callback wrapper for account removing on >= lollipop (22) devices
   */
  private class RemoveLollipopAccountCallback(
    private val callback: Callback<Boolean>
  ) : AccountManagerCallback<Bundle> {

    override fun run(accountManagerFuture: AccountManagerFuture<Bundle>) {
      try {
        callback.onResult(accountManagerFuture.result.getBoolean(AccountManager.KEY_BOOLEAN_RESULT))
      } catch (e: Exception) {
        callback.onResult(false)
      }
    }
  }

  /**
   * Callback wrapper for account removing on prelollipop (22 -> MR1) devices
   */
  private class RemoveAccountCallback(private val callback: Callback<Boolean>) : AccountManagerCallback<Boolean> {

    override fun run(accountManagerFuture: AccountManagerFuture<Boolean>) {
      try {
        callback.onResult(accountManagerFuture.result)
      } catch (e: Exception) {
        callback.onResult(false)
      }
    }
  }

  private class AccountFuture(
    private val accountFuture: AccountManagerFuture<Bundle>
  ) : Future<Account> {
    override fun isDone(): Boolean = accountFuture.isDone
    override fun get(): Account = createAccount(accountFuture.result)
    override fun get(p0: Long, p1: TimeUnit?): Account = createAccount(accountFuture.getResult(p0, p1))
    private fun createAccount(bundle: Bundle): Account {
      val accountName = bundle.getString(AccountManager.KEY_ACCOUNT_NAME)
      if (accountName != null) {
        return Account(bundle.getString(AccountManager.KEY_ACCOUNT_NAME),
          bundle.getString(AccountManager.KEY_ACCOUNT_TYPE))
      }
      throw AuthenticationCanceledException()
    }

    override fun cancel(p0: Boolean): Boolean = accountFuture.cancel(p0)
    override fun isCancelled(): Boolean = accountFuture.isCancelled
  }

  private class RemoveAccountFuture(private val accountFuture: AccountManagerFuture<Bundle>) : Future<Boolean> {
    override fun isDone(): Boolean = accountFuture.isDone
    override fun get(): Boolean = accountFuture.result.getBoolean(AccountManager.KEY_BOOLEAN_RESULT)
    override fun get(timeout: Long, timeUnit: TimeUnit?): Boolean = accountFuture.getResult(timeout, timeUnit)
      .getBoolean(AccountManager.KEY_BOOLEAN_RESULT)

    override fun cancel(mayInterruptIfRunning: Boolean): Boolean = accountFuture.cancel(mayInterruptIfRunning)
    override fun isCancelled(): Boolean = accountFuture.isCancelled
  }

  private class PreLollipopRemoveAccountFuture(
    private val accountFuture: AccountManagerFuture<Boolean>
  ) : Future<Boolean> {
    override fun isDone(): Boolean = accountFuture.isDone
    override fun get(): Boolean = accountFuture.result
    override fun get(timeout: Long, timeUnit: TimeUnit?): Boolean = accountFuture.getResult(timeout, timeUnit)
    override fun cancel(mayInterruptIfRunning: Boolean): Boolean = accountFuture.cancel(mayInterruptIfRunning)
    override fun isCancelled(): Boolean = accountFuture.isCancelled
  }
}
