package com.andretietz.retroauth.demo

import android.accounts.Account
import android.util.Log
import com.andretietz.retroauth.AuthenticationService

class DemoAuthenticationService : AuthenticationService() {
  override fun getLoginAction(): String = getString(R.string.authentication_ACTION)
  override fun cleanupAccount(account: Account) {
    // Here you can trigger your account cleanup.
    Log.e("Account Cleanup", "Remove account: ${account.name}")
  }
}
