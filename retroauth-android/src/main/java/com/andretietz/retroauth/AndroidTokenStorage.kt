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
import android.app.Application
import java.util.Locale

/**
 * This is the implementation of a [TokenStorage] in Android using the Android [AccountManager]
 */
class AndroidTokenStorage(
        application: Application,
        private val accountManager: AccountManager = AccountManager.get(application)
) :
        TokenStorage<Account, AndroidTokenType, AndroidToken> {

    private val activityManager = ActivityManager[application]


    companion object {
        @JvmStatic
        fun createDataKey(type: AndroidTokenType, key: String) = String.format(Locale.US, "%s_%s", type.tokenType, key)
    }


    override fun getToken(owner: Account, type: AndroidTokenType): AndroidToken {
        var token: String?
        val future = accountManager.getAuthToken(owner, type.tokenType, null, activityManager.activity, null, null)
        val result = future.result
        token = result.getString(AccountManager.KEY_AUTHTOKEN)
        if (token == null) {
            token = accountManager.peekAuthToken(owner, type.tokenType)
        }
        if (token == null) throw IllegalStateException(
                String.format("No token found! Make sure you store the token during login using %s#storeToken()",
                        AuthenticationActivity::class.java.simpleName)
        )
        return AndroidToken(
                token,
                type.dataKeys
                        ?.associateTo(HashMap()) {
                            it to accountManager.getUserData(owner, createDataKey(type, it))
                        }
        )
    }

    override fun removeToken(owner: Account, type: AndroidTokenType, token: AndroidToken) {
        accountManager.invalidateAuthToken(owner.type, token.token)
        type.dataKeys?.forEach { accountManager.setUserData(owner, createDataKey(type, it), null) }
    }

    override fun storeToken(owner: Account, type: AndroidTokenType, token: AndroidToken): AndroidToken {
        accountManager.setAuthToken(owner, type.tokenType, token.token)
        if (type.dataKeys != null && token.data != null) {
            type.dataKeys.forEach {
                accountManager.setUserData(owner, createDataKey(type, it), token.data[it])
            }
        }
        return token
    }
}
