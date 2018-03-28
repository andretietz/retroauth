package com.andretietz.retroauth.testimpl

import com.andretietz.retroauth.TokenProvider
import okhttp3.Request

open class TestProvider : TokenProvider<String> {
    override fun isTokenValid(token: String): Boolean = true

    override fun authenticateRequest(request: Request, token: String): Request =
            request
                    .newBuilder()
                    .header("auth", token)
                    .build()

    override fun refreshToken(token: String): String = token
}
