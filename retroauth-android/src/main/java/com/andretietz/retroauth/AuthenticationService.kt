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
import android.app.Service
import android.content.Intent
import android.os.IBinder

/**
 * You have to extend this service if you want to provide your own implementation of the [AuthenticationService].
 */
abstract class AuthenticationService : Service() {

  override fun onBind(intent: Intent): IBinder? = AccountAuthenticator(
    this,
    getLoginAction(),
    this::cleanupAccount
  ).iBinder

  /**
   * @return An Action String to open the activity to login
   */
  abstract fun getLoginAction(): String

  /**
   * Called when an account is intended to be removed. Use it if you need to remove any kinds of user related data.
   *
   * At this point the account hasn't been removed yet. It will right after this method has been executed. If
   *
   * <b>Caution</b>: This method can (and will) be called from a different process (when the user removes the account
   * using the account settings on the android device). Consider that when cleaning up your user data.
   *
   * @param account that will be removed.
   */
  open fun cleanupAccount(account: Account) {}
}
