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
import android.util.Base64
import android.util.Base64.DEFAULT
import java.util.concurrent.Callable
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit

/**
 * This is the implementation of a [CredentialStorage] in Android using the Android [AccountManager]
 */
class AndroidAccountManagerCredentialStorage constructor(
  private val application: Application
) : CredentialStorage<Account> {

  private val accountManager by lazy { AccountManager.get(application) }

  private val executor = Executors.newSingleThreadExecutor()

  companion object {
    private fun createDataKey(type: String, key: String) = "${type}_$key"
  }

  override fun getCredentials(
    owner: Account,
    credentialType: String
  ): Credentials? {
    val future = accountManager.getAuthToken(
      owner,
      credentialType,
      null,
      ActivityManager[application].activity,
      null,
      null
    )
    var token: String? = if (Looper.myLooper() == Looper.getMainLooper()) {
      executor.submit(Callable<String?> {
        future.result.getString(AccountManager.KEY_AUTHTOKEN)
      }).get(100, TimeUnit.MILLISECONDS)
    } else future.result.getString(AccountManager.KEY_AUTHTOKEN)
    if (token == null) token = accountManager.peekAuthToken(owner, credentialType)
    if (token == null) {
      return null
    }

    val dataKeys = accountManager.getUserData(owner, "keys_${owner.type}_$credentialType")
      ?.let { Base64.decode(it, DEFAULT).toString() }
      ?.split(",")
    return Credentials(
      token,
      dataKeys
        ?.associate {
          it to accountManager.getUserData(owner, createDataKey(credentialType, it))
        }
    )
  }

  override fun removeCredentials(owner: Account, credentialType: String) {
    getCredentials(owner, credentialType)?.let { credential ->
      accountManager.invalidateAuthToken(owner.type, credential.token)
      val dataKeys = accountManager.getUserData(owner, "keys_${owner.type}_$credentialType")
        ?.let { Base64.decode(it, DEFAULT).toString() }
        ?.split(",")
      dataKeys?.forEach {
        accountManager.setUserData(
          owner,
          createDataKey(credentialType, it),
          null
        )
      }
    }
  }

  override fun storeCredentials(owner: Account, credentialType: String, credentials: Credentials) {
    accountManager.setAuthToken(owner, credentialType, credentials.token)
    val data = credentials.data
    if (data != null) {
      val dataKeys = data.keys
        .map { Base64.encodeToString(it.toByteArray(), DEFAULT) }
        .joinToString { it }
      accountManager.setUserData(owner, "keys_${owner.type}_$credentialType", dataKeys)
      data.forEach { (key, value) ->
        accountManager.setUserData(owner, createDataKey(credentialType, key), value)
      }
    }
  }
}
