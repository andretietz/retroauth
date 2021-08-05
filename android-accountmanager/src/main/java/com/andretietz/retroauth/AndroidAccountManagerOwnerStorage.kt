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
import android.content.Context
import android.os.Build
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * This is the Android implementation of an [OwnerStorage]. It does all the Android [Account]
 * handling using tha Android [AccountManager].
 */
@Suppress("unused")
class AndroidAccountManagerOwnerStorage constructor(
  private val application: Application
) : OwnerStorage<Account> {

  companion object {
    private const val RETROAUTH_ACCOUNT_NAME_KEY = "com.andretietz.retroauth.ACTIVE_ACCOUNT"
  }

  private val activityManager by lazy { ActivityManager[application] }
  private val accountManager by lazy { AccountManager.get(application) }

  @Suppress("BlockingMethodInNonBlockingContext")
  override suspend fun createOwner(ownerType: String, credentialType: String): Account? =
    withContext(Dispatchers.Default) {
      val bundle = accountManager.addAccount(
        ownerType,
        credentialType,
        null,
        null,
        activityManager.activity,
        null,
        null).result
      bundle.getString(AccountManager.KEY_ACCOUNT_NAME)?.let {
        Account(bundle.getString(AccountManager.KEY_ACCOUNT_NAME),
          bundle.getString(AccountManager.KEY_ACCOUNT_TYPE))
      }
    }


  override fun getOwner(ownerType: String, ownerName: String): Account? {
    return accountManager.getAccountsByType(ownerType)
      .firstOrNull { ownerName == it.name }
  }

  override fun getActiveOwner(ownerType: String): Account? {
    return application.getSharedPreferences(ownerType, Context.MODE_PRIVATE)
      .getString(RETROAUTH_ACCOUNT_NAME_KEY, null)?.let { accountName ->
        getOwner(ownerType, accountName)
      }
  }

  override fun getOwners(ownerType: String): List<Account> = accountManager.accounts.toList()
    .filter { it.type == ownerType }

  override fun switchActiveOwner(ownerType: String, owner: Account?) {
    val preferences = application.getSharedPreferences(ownerType, Context.MODE_PRIVATE)
    if (owner == null) {
      preferences.edit().remove(RETROAUTH_ACCOUNT_NAME_KEY).apply()
    } else {
      preferences.edit().putString(RETROAUTH_ACCOUNT_NAME_KEY, owner.name).apply()
    }
  }

  override fun removeOwner(ownerType: String, owner: Account): Boolean {
    val success = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP_MR1) {
      accountManager.removeAccount(owner, null, null, null).result
        .getBoolean(AccountManager.KEY_BOOLEAN_RESULT)
    } else {
      @Suppress("DEPRECATION")
      accountManager.removeAccount(owner, null, null).result
    }
    if (success) switchActiveOwner(owner.type)
    return success
  }
}
