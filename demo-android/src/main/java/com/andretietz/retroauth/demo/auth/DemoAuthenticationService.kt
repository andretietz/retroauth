package com.andretietz.retroauth.demo.auth

import android.accounts.Account
import com.andretietz.retroauth.AuthenticationService
import com.andretietz.retroauth.demo.R
import timber.log.Timber

class DemoAuthenticationService : AuthenticationService() {
  override fun getLoginAction(): String = getString(R.string.authentication_ACTION)
  override fun cleanupAccount(account: Account) {
    // Here you can trigger your account cleanup.
    // Note: This might be executed on a different process! (when the account is removed
    // from the android account manager.
    Timber.e("Remove account: ${account.name}")
  }
}
