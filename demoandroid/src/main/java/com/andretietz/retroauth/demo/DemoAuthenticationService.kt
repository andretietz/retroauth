package com.andretietz.retroauth.demo

import android.accounts.Account
import com.andretietz.retroauth.AuthenticationService
import timber.log.Timber

class DemoAuthenticationService : AuthenticationService() {
  override fun getLoginAction(): String = getString(R.string.authentication_ACTION)
  override fun cleanupAccount(account: Account) {
    // Here you can trigger your account cleanup.
    Timber.e("Remove account: ${account.name}")
  }
}
