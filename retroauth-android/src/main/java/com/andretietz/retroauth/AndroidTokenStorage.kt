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
import android.os.Looper
import java.util.Locale
import java.util.concurrent.Callable
import java.util.concurrent.Executors
import java.util.concurrent.Future
import java.util.concurrent.FutureTask

/**
 * This is the implementation of a [TokenStorage] in Android using the Android [AccountManager]
 */
class AndroidTokenStorage constructor(
  private val application: Application
) : TokenStorage<Account, AndroidTokenType, AndroidToken> {

  private val executor by lazy { Executors.newSingleThreadExecutor() }
  private val accountManager by lazy { AccountManager.get(application) }

  companion object {
    @JvmStatic
    private fun createDataKey(type: AndroidTokenType, key: String) =
      String.format(Locale.US, "%s_%s", type.tokenType, key)
  }

  override fun getToken(
    owner: Account,
    type: AndroidTokenType,
    callback: Callback<AndroidToken>?
  ): Future<AndroidToken> {
    val task = GetTokenTask(application, accountManager, owner, type, callback)
    if (Looper.myLooper() == Looper.getMainLooper()) {
      return executor.submit(task)
    }
    val future = FutureTask(task)
    future.run()
    return future
  }

  override fun removeToken(owner: Account, type: AndroidTokenType, token: AndroidToken) {
    accountManager.invalidateAuthToken(owner.type, token.token)
    type.dataKeys?.forEach { accountManager.setUserData(owner, createDataKey(type, it), null) }
  }

  override fun storeToken(owner: Account, type: AndroidTokenType, token: AndroidToken) {
    accountManager.setAuthToken(owner, type.tokenType, token.token)
    if (type.dataKeys != null && token.data != null) {
      type.dataKeys.forEach {
        if (!token.data.containsKey(it)) throw IllegalArgumentException(
          String.format(Locale.US,
            "The token you want to store, needs to contain token-data with the keys: %s",
            type.dataKeys.toString())
        )
        accountManager.setUserData(owner, createDataKey(type, it), token.data[it])
      }
    }
  }

  private class GetTokenTask(
    application: Application,
    private val accountManager: AccountManager,
    private val owner: Account,
    private val type: AndroidTokenType,
    private val callback: Callback<AndroidToken>?
  ) : Callable<AndroidToken> {

    private val activityManager = ActivityManager[application]

    override fun call(): AndroidToken {
      val future = accountManager.getAuthToken(
        owner,
        type.tokenType,
        null,
        activityManager.activity,
        null,
        null)
      return (future.result.getString(AccountManager.KEY_AUTHTOKEN)
        ?: accountManager.peekAuthToken(owner, type.tokenType)
        ?: throw AuthenticationCanceledException()).let { token ->
        AndroidToken(
          token,
          type.dataKeys
            ?.associateTo(HashMap()) {
              it to accountManager.getUserData(owner, createDataKey(type, it))
            }
        ).apply {
          callback?.onResult(this)
        }
      }
    }
  }
}
