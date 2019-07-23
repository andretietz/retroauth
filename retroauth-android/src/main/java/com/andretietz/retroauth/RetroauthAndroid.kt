package com.andretietz.retroauth

import android.accounts.Account
import android.app.Application

object RetroauthAndroid {
  @JvmStatic
  fun createBuilder(
    application: Application,
    authenticator: Authenticator<String, Account, AndroidCredentialType, AndroidCredentials>
  ): Retroauth.Builder<String, Account, AndroidCredentialType, AndroidCredentials> =
    Retroauth.Builder(
      authenticator,
      AndroidOwnerStorage(application),
      AndroidCredentialStorage(application),
      AndroidMethodCache()
    )
}
