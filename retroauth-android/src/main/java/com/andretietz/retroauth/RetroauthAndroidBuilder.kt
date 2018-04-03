package com.andretietz.retroauth

import android.app.Application

object RetroauthAndroidBuilder {
    @JvmStatic
    fun createBuilder(
            application: Application,
            tokenProvider: TokenProvider<String, AndroidOwner, AndroidTokenType, AndroidToken>): Retroauth.Builder<String, AndroidOwner, AndroidTokenType, AndroidToken> {
        return Retroauth.Builder(
                tokenProvider,
                AndroidOwnerManager(application),
                AndroidTokenStorage(application),
                AndroidMethodCache()
        )
    }
}