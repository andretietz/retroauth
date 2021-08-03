package com.andretietz.retroauth

import android.accounts.Account
import android.app.Application
import retrofit2.Retrofit

object RetroauthAndroid {
  @JvmStatic
  fun setup(
    retrofit: Retrofit,
    application: Application,
    authenticator: Authenticator<Account, AndroidCredential>
  ): Retrofit {
    return Retroauth.setup(
      retrofit,
      authenticator,
      AndroidAccountManagerOwnerStorage(application),
      AndroidAccountManagerCredentialStorage(application)
    )
  }
}

fun Retrofit.androidAuthentication(
  application: Application,
  authenticator: Authenticator<Account, AndroidCredential>
) = RetroauthAndroid.setup(this, application, authenticator)
