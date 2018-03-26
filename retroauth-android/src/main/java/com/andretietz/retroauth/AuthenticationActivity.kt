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
import android.accounts.AccountAuthenticatorResponse
import android.accounts.AccountManager
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.support.v7.app.AppCompatActivity

/**
 * Your activity that's supposed to create the account (i.e. Login{@link android.app.Activity}) has to implement this.
 * It'll provide functionality to {@link #storeToken(Account, String, String)} and
 * {@link #storeUserData(Account, String, String)} when logging in. In case your service is providing a refresh token,
 * use {@link #storeToken(Account, String, String, String)}. This will additionally store a refresh token that can be used
 * in {@link TokenProvider#validateResponse(int, okhttp3.Response, TokenStorage, Object, Object, Object)}
 * to update the access-token
 */
abstract class AuthenticationActivity : AppCompatActivity() {

    protected var accountAuthenticatorResponse: AccountAuthenticatorResponse? = null
    protected lateinit var accountType: String
    protected lateinit var accountManager: AccountManager
    private var tokenType: String? = null
    private lateinit var resultBundle: Bundle

    companion object {
        @JvmStatic
        fun createLoginIntent(action: String, accountType: String, tokenType: String?): Intent {
            val intent = Intent(action)
            intent.putExtra(AccountManager.KEY_ACCOUNT_TYPE, accountType)
            intent.putExtra(AccountAuthenticator.KEY_TOKEN_TYPE, tokenType)
            return intent
        }
    }

    override fun onCreate(icicle: Bundle?) {
        super.onCreate(icicle)
        val intent = intent
        accountAuthenticatorResponse = intent.getParcelableExtra(AccountManager.KEY_ACCOUNT_AUTHENTICATOR_RESPONSE)
        accountAuthenticatorResponse?.onRequestContinued()
        val accountType = intent.getStringExtra(AccountManager.KEY_ACCOUNT_TYPE)
        if (accountType == null) {
            accountAuthenticatorResponse?.onError(AccountManager.ERROR_CODE_CANCELED, "canceled")
            throw IllegalStateException(
                    String.format(
                            "This Activity cannot be started without the \"%s\" extra in the intent! "
                                    + "Use the \"addAccount\"-Method of the \"%s\" for opening the Login manually.",
                            AccountManager.KEY_ACCOUNT_TYPE, AuthAccountManager::class.java.simpleName))
        }
        this.accountType = accountType
        tokenType = intent.getStringExtra(AccountAuthenticator.KEY_TOKEN_TYPE)
        accountManager = AccountManager.get(this)

        resultBundle = Bundle()
        resultBundle.putString(AccountManager.KEY_ACCOUNT_TYPE, accountType)
    }

    /**
     * This method stores an authentication Token to a specific account.
     *
     * @param account      Account you want to store the token for
     * @param token        Token itself
     * @param tokenType    type of the token you want to store
     * @param refreshToken a refresh token if present
     */
    @JvmOverloads
    fun storeToken(account: Account, tokenType: String, token: String, refreshToken: String? = null) {
        accountManager.setAuthToken(account, tokenType, token)
        if (refreshToken != null) {
            accountManager.setAuthToken(account, String.format("%s_refresh", tokenType), refreshToken)
        }
    }

    /**
     * With this you can store some additional userdata in key-value-pairs to the account.
     *
     * @param account Account you want to store information for
     * @param key     the key for the data
     * @param value   the actual data you want to store
     */
    fun storeUserData(account: Account, key: String, value: String) {
        accountManager.setUserData(account, key, value)
    }

    /**
     * This method will finish the login process. Depending on the finishActivity flag, the activity will be finished or not
     * The account which is reached into this method will be set as
     * "current-active" account. Use [AuthAccountManager.resetActiveAccount] to
     * reset this if required
     *
     * @param account        Account you want to set as current active
     * @param finishActivity when `true`, the activity will be finished after finalization.
     */
    @JvmOverloads
    fun finalizeAuthentication(account: Account, finishActivity: Boolean = true) {
        resultBundle.putString(AccountManager.KEY_ACCOUNT_NAME, account.name)
        val preferences = getSharedPreferences(accountType, Context.MODE_PRIVATE)
        preferences.edit().putString(AuthAccountManager.RETROAUTH_ACCOUNT_NAME_KEY, account.name).apply()
        if (finishActivity) finish()
    }

    /**
     * Tries finding an existing account with the given name.
     * It creates a new Account if it couldn't find it
     *
     * @param accountName Name of the account you're searching for
     * @return The account if found, or a newly created one
     */
    fun createOrGetAccount(accountName: String): Account {
        // if this is a relogin
        val accountList = accountManager.getAccountsByType(accountType)
        for (account in accountList) {
            if (account.name == accountName)
                return account
        }
        val account = Account(accountName, accountType)
        accountManager.addAccountExplicitly(account, null, null)
        return account
    }

    /**
     * If for some reason an account was created already and the login couldn't complete successfully, you can user this
     * method to remove this account
     *
     * @param account to remove
     */
    @Suppress("DEPRECATION")
    fun removeAccount(account: Account) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP_MR1) {
            accountManager.removeAccount(account, null, null, null)
        } else {
            accountManager.removeAccount(account, null, null)
        }
    }

    /**
     * Sends the result or a Constants.ERROR_CODE_CANCELED error if a result isn't present.
     */
    override fun finish() {
        if (accountAuthenticatorResponse != null) {
            accountAuthenticatorResponse!!.onResult(resultBundle)
            accountAuthenticatorResponse = null
        } else {
            if (resultBundle.containsKey(AccountManager.KEY_ACCOUNT_NAME)) {
                val intent = Intent()
                intent.putExtras(resultBundle)
                setResult(Activity.RESULT_OK, intent)
            } else {
                setResult(Activity.RESULT_CANCELED)
            }
        }
        super.finish()
    }

    /**
     * @return The requested account type if available. otherwise `null`
     */
    fun getRequestedAccountType(): String {
        return accountType
    }

    /**
     * @return The requested token type if available. otherwise `null`
     */
    fun getRequestedTokenType(): String? {
        return tokenType
    }
}