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
 * This is the implementation of a [CredentialStorage] in Android using the Android [AccountManager]
 */
class AndroidCredentialStorage constructor(
  private val application: Application
) : CredentialStorage<Account, AndroidCredentialType, AndroidCredentials> {

  private val executor by lazy { Executors.newSingleThreadExecutor() }
  private val accountManager by lazy { AccountManager.get(application) }

  companion object {
    @JvmStatic
    private fun createDataKey(type: AndroidCredentialType, key: String) =
      String.format(Locale.US, "%s_%s", type.type, key)
  }

  override fun getCredentials(
    owner: Account,
    type: AndroidCredentialType,
    callback: Callback<AndroidCredentials>?
  ): Future<AndroidCredentials> {
    val task = GetTokenTask(application, accountManager, owner, type, callback)
    return if (Looper.myLooper() == Looper.getMainLooper())
      executor.submit(task)
    else
      FutureTask(task).also { it.run() }
  }

  override fun removeCredentials(owner: Account, type: AndroidCredentialType, credentials: AndroidCredentials) {
    accountManager.invalidateAuthToken(owner.type, credentials.token)
    type.dataKeys?.forEach { accountManager.setUserData(owner, createDataKey(type, it), null) }
  }

  override fun storeCredentials(owner: Account, type: AndroidCredentialType, credentials: AndroidCredentials) {
    accountManager.setAuthToken(owner, type.type, credentials.token)
    if (type.dataKeys != null && credentials.data != null) {
      type.dataKeys.forEach {
        if (!credentials.data.containsKey(it)) throw IllegalArgumentException(
          String.format(Locale.US,
            "The credentials you want to store, needs to contain credentials-data with the keys: %s",
            type.dataKeys.toString())
        )
        accountManager.setUserData(owner, createDataKey(type, it), credentials.data[it])
      }
    }
  }

  private class GetTokenTask(
    application: Application,
    private val accountManager: AccountManager,
    private val owner: Account,
    private val type: AndroidCredentialType,
    private val callback: Callback<AndroidCredentials>?
  ) : Callable<AndroidCredentials> {

    private val activityManager = ActivityManager[application]

    override fun call(): AndroidCredentials {
      val future = accountManager.getAuthToken(
        owner,
        type.type,
        null,
        activityManager.activity,
        null,
        null)

      var token = future.result.getString(AccountManager.KEY_AUTHTOKEN)
      if (token == null) token = accountManager.peekAuthToken(owner, type.type)
      if (token == null) {
        val error = AuthenticationCanceledException()
        callback?.onError(error)
        throw error
      }
      return AndroidCredentials(
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
