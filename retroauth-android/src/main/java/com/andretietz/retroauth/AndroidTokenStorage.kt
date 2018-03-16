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
import android.accounts.AuthenticatorException
import android.accounts.OperationCanceledException
import android.app.Activity
import android.app.Application

import java.io.IOException

/**
 * This is the implementation of a [TokenStorage] in Android using the Android [AccountManager]
 */
internal class AndroidTokenStorage(application: Application) : TokenStorage<Account, AndroidTokenType, AndroidToken> {

    private val accountManager: AccountManager = AccountManager.get(application)
    private val activityManager: ActivityManager = ActivityManager[application]

    @Throws(AuthenticationCanceledException::class)
    override fun getToken(owner: Account?, type: AndroidTokenType): AndroidToken {
        try {
            return (
                    if (owner == null) {
                        createAccountAndGetToken(activityManager.activity, type)
                    } else {
                        getToken(activityManager.activity, owner, type)
                    }) ?: throw AuthenticationCanceledException("user canceled the login!")

        } catch (e: AuthenticatorException) {
            throw AuthenticationCanceledException(null, e)
        } catch (e: OperationCanceledException) {
            throw AuthenticationCanceledException(null, e)
        } catch (e: IOException) {
            throw AuthenticationCanceledException(null, e)
        }
    }

    @Throws(AuthenticatorException::class, OperationCanceledException::class, IOException::class)
    private fun createAccountAndGetToken(activity: Activity?, type: AndroidTokenType): AndroidToken? {

        val future = accountManager
                .addAccount(type.accountType, type.tokenType, null, null, activity, null, null)
        val result = future.result
        val accountName = result.getString(AccountManager.KEY_ACCOUNT_NAME)
        if (accountName != null) {
            val account = Account(result.getString(AccountManager.KEY_ACCOUNT_NAME),
                    result.getString(AccountManager.KEY_ACCOUNT_TYPE))
            val token = accountManager.peekAuthToken(account, type.tokenType)
            val refreshToken = accountManager.peekAuthToken(account, getRefreshTokenType(type))
            if (token != null) return AndroidToken(token, refreshToken)
        }
        return null
    }

    @Throws(AuthenticatorException::class, OperationCanceledException::class, IOException::class)
    private fun getToken(activity: Activity?, account: Account, type: AndroidTokenType): AndroidToken? {
        // Clear the interrupted flag
        Thread.interrupted()
        val future = accountManager
                .getAuthToken(account, type.tokenType, null, activity, null, null)
        val result = future.result
        var token = result.getString(AccountManager.KEY_AUTHTOKEN)
        val refreshToken = accountManager.peekAuthToken(account, getRefreshTokenType(type))
        if (token == null) {
            token = accountManager.peekAuthToken(account, type.tokenType)
        }
        return if (token != null) AndroidToken(token, refreshToken) else null
    }

    private fun getRefreshTokenType(type: AndroidTokenType): String {
        return String.format("%s_refresh", type.tokenType)
    }

    override fun removeToken(owner: Account, type: AndroidTokenType, token: AndroidToken) {
        accountManager.invalidateAuthToken(owner.type, token.token)
        accountManager.invalidateAuthToken(owner.type, token.refreshToken)
    }

    override fun storeToken(owner: Account, type: AndroidTokenType, token: AndroidToken) {
        accountManager.setAuthToken(owner, type.tokenType, token.token)
        accountManager.setAuthToken(owner, getRefreshTokenType(type), token.refreshToken)
    }
}
