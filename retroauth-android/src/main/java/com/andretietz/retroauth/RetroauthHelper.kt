package com.andretietz.retroauth

import android.Manifest
import android.accounts.Account
import android.accounts.AccountManager
import android.accounts.AccountManagerCallback
import android.accounts.AccountManagerFuture
import android.app.Application
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.support.annotation.RequiresPermission

class RetroauthHelper(application: Application) {

    private val accountManager by lazy { AccountManager.get(application) }
    private val activityManager by lazy { ActivityManager.get(application) }
    private val accountHelper by lazy { AccountHelper(application) }
    private val tokenStorage by lazy { AndroidTokenStorage(application) }

    /**
     * Creates a new account for the given account type. The tokenType is optional. you can request this type in the login
     * [AuthenticationActivity] calling [AuthenticationActivity.getRequestedTokenType].
     * This value will not be available when you're creating an account from Android-Settings-Accounts-Add Account
     *
     * @param accountType the account type you want to create an account for
     * @param tokenType   the type of token you want to create
     * @param callback    which is called when the account has been created or account creation was canceled.
     */
    @JvmOverloads
    @Suppress("unused")
    fun createAccount(accountType: String, tokenType: String? = null, callback: AccountCallback? = null) {
        accountManager.addAccount(
                accountType,
                tokenType,
                null,
                null,
                activityManager.activity,
                if (callback != null) CreateAccountCallback(callback) else null,
                null)
    }

    fun getCurrentAccount(accountType: String): Account? {
        accountHelper.getCurrentAccountName(accountType)?.let {
            return accountHelper.getAccountByNameIfExists(accountType, it)
        }
        return null
    }

    /**
     * Removes the given account
     *
     * @param account the account which you want to delete.
     * @param callback    callback returns, when account was deleted.
     */
    @JvmOverloads
    @Suppress("unused")
    fun removeAccount(account: Account, callback: AccountCallback? = null) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP_MR1) {
            val rac = if (callback != null) RemoveLollipopAccountCallback(callback) else null
            accountManager.removeAccount(account, null, rac, null)
        } else {
            val rac = if (callback != null) RemoveAccountCallback(callback) else null
            @Suppress("DEPRECATION")
            accountManager.removeAccount(account, rac, null)
        }
        accountHelper.resetCurrentAccount(account.type)
    }

    fun setToken(account: Account, tokenType: AndroidTokenType, token: AndroidToken) = tokenStorage.storeToken(account, tokenType, token)

    fun getToken(owner: Account, tokenType: AndroidTokenType): AndroidToken? = tokenStorage.getToken(owner, tokenType)

    fun removeToken(account: Account, tokenType: AndroidTokenType, token: AndroidToken) = tokenStorage.removeToken(account, tokenType, token)

    /**
     * Returns an intent to open an account chooser
     *
     * @param accountType Make sure this is your accountType or you requested the permission for
     * [Manifest.permission.GET_ACCOUNTS], otherwise there will be a [SecurityException]
     * thrown
     * @return an Intent which you can start for result to open an account chooser.
     */
    @Suppress("unused")
    @RequiresPermission(Manifest.permission.GET_ACCOUNTS)
    fun newChooseAccountIntent(accountType: String): Intent {
        val accounts = accountManager.getAccountsByType(accountType).toMutableList()
        val account = getCurrentAccount(accountType)
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
     * Callback wrapper for account removing on prelollipop (22 -> MR1) devices
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