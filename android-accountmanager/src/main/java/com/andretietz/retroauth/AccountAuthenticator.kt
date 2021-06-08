/*
 * Copyright (c) 2015 Andre Tietz
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

import android.accounts.AbstractAccountAuthenticator
import android.accounts.Account
import android.accounts.AccountAuthenticatorResponse
import android.accounts.AccountManager
import android.accounts.NetworkErrorException
import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log

/**
 * This AccountAuthenticator is a very basic implementation of Android's
 * [AbstractAccountAuthenticator]. This implementation is intentional as empty as it is. Cause of this is, that
 * it's executed in a different process, which makes it difficult to provide login endpoints from
 * the app process in here.
 *
 * NOTE: This class cannot be replaced with a kotlin version yet, since Android cannot load Authenticators
 * that are non java once
 */
class AccountAuthenticator(
  context: Context,
  internal val action: String,
  private val cleanupUserData: (account: Account) -> Unit
) : AbstractAccountAuthenticator(context) {

  override fun addAccount(
    response: AccountAuthenticatorResponse,
    accountType: String,
    authCredentialType: String?,
    requiredFeatures: Array<String>?,
    options: Bundle
  ) = createAuthBundle(response, action, accountType, authCredentialType, null)

  override fun getAuthToken(
    response: AccountAuthenticatorResponse,
    account: Account,
    authTokenType: String,
    options: Bundle
  ) = createAuthBundle(response, action, account.type, authTokenType, account.name)

  /**
   * Creates an Intent to open the Activity to login.
   *
   * @param response needed parameter
   * @param accountType The account Type
   * @param credentialType The requested credential-type
   * @param accountName The name of the account
   * @return a bundle to open the activity
   */
  private fun createAuthBundle(
    response: AccountAuthenticatorResponse,
    action: String,
    accountType: String,
    credentialType: String?,
    accountName: String?
  ): Bundle = Bundle().apply {
    putParcelable(
      AccountManager.KEY_INTENT,
      Intent(action).apply {
        putExtra(AccountManager.KEY_ACCOUNT_AUTHENTICATOR_RESPONSE, response)
        putExtra(AccountManager.KEY_ACCOUNT_TYPE, accountType)
        putExtra(KEY_CREDENTIAL_TYPE, credentialType)
        accountName?.let {
          putExtra(AccountManager.KEY_ACCOUNT_NAME, it)
        }
      })
  }

  override fun confirmCredentials(
    response: AccountAuthenticatorResponse,
    account: Account,
    options: Bundle?
  ) = null

  override fun editProperties(response: AccountAuthenticatorResponse, accountType: String) = null

  override fun getAuthTokenLabel(authCredentialType: String) = null

  override fun updateCredentials(
    response: AccountAuthenticatorResponse,
    account: Account,
    authCredentialType: String,
    options: Bundle
  ): Bundle? = null

  override fun hasFeatures(
    response: AccountAuthenticatorResponse,
    account: Account,
    features: Array<String>
  ) = null

  @SuppressLint("CheckResult")
  @Throws(NetworkErrorException::class)
  override fun getAccountRemovalAllowed(response: AccountAuthenticatorResponse, account: Account): Bundle? {
    val result = super.getAccountRemovalAllowed(response, account)
    if (
      result != null && result.containsKey(AccountManager.KEY_BOOLEAN_RESULT) &&
      result.getBoolean(AccountManager.KEY_BOOLEAN_RESULT, false)) {
      try {
        cleanupUserData(account)
      } catch (exception: Exception) {
        Log.w("AuthenticationService", "Your cleanup method threw an exception:", exception)
      }
    }
    return result
  }

  companion object {
    internal const val KEY_CREDENTIAL_TYPE = "account_credential_type"
  }
}
