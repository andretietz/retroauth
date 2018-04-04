package com.andretietz.retroauth

import android.accounts.Account
import android.app.Application

object RetroauthAndroidBuilder {
    @JvmStatic
    fun createBuilder(
            application: Application,
            tokenProvider: TokenProvider<String, Account, AndroidTokenType, AndroidToken>): Retroauth.Builder<String, Account, AndroidTokenType, AndroidToken> {
        return Retroauth.Builder(
                tokenProvider,
                AndroidOwnerManager(application),
                AndroidTokenStorage(application),
                AndroidMethodCache()
        )
    }
}