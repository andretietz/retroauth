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

/**
 * This is the implementation of a [CredentialStorage] in Android using the Android [AccountManager]
 */
class AndroidAccountManagerCredentialStorage constructor(
  private val application: Application
) : CredentialStorage<Account, AndroidCredential> {

  private val accountManager by lazy { AccountManager.get(application) }

  companion object {
    @JvmStatic
    private fun createDataKey(type: CredentialType, key: String) =
      "%s_%s".format(type.type, key)
  }

  override fun getCredentials(
    owner: Account,
    type: CredentialType
  ): AndroidCredential? {
    val future = accountManager.getAuthToken(
      owner,
      type.type,
      null,
      ActivityManager[application].activity,
      null,
      null)

    var token = future.result.getString(AccountManager.KEY_AUTHTOKEN)
    if (token == null) token = accountManager.peekAuthToken(owner, type.type)
    if (token == null) {
      return null
//  TODO: before:    throw AuthenticationCanceledException()
    }
    return AndroidCredential(
      token,
      type.dataKeys
        ?.associateTo(HashMap()) {
          it to accountManager.getUserData(owner, createDataKey(type, it))
        }
    )
  }

  override fun removeCredentials(owner: Account, type: CredentialType) {
    getCredentials(owner, type)?.let { credential ->
      accountManager.invalidateAuthToken(owner.type, credential.token)
      type.dataKeys?.forEach { accountManager.setUserData(owner, createDataKey(type, it), null) }
    }
  }

  override fun storeCredentials(owner: Account, type: CredentialType, credentials: AndroidCredential) {
    accountManager.setAuthToken(owner, type.type, credentials.token)
    val dataKeys = type.dataKeys
    if (dataKeys != null && credentials.data != null) {
      dataKeys.forEach {
        require(credentials.data.containsKey(it)) {
          throw IllegalArgumentException(
            "The credentials you want to store, needs to contain credentials-data with the keys: %s"
              .format(type.dataKeys.toString())
          )
        }
        accountManager.setUserData(owner, createDataKey(type, it), credentials.data[it])
      }
    }
  }
}
