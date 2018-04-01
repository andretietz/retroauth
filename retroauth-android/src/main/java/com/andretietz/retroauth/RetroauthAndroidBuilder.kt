package com.andretietz.retroauth

import android.accounts.Account
import android.app.Application

object RetroauthAndroidBuilder {
    @JvmStatic
    fun createBuilder(
            application: Application,
            tokenProvider: TokenProvider<Account, AndroidTokenType, AndroidToken>): Retroauth.Builder<Account, AndroidTokenType, AndroidToken> {
        return Retroauth.Builder(
                tokenProvider,
                AndroidOwnerManager(application),
                AndroidTokenStorage(application),
                AndroidMethodCache()
        )
    }
}