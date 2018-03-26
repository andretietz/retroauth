package com.andretietz.retroauth.testimpl

import com.andretietz.retroauth.TokenProvider

import okhttp3.Request

class TestProvider : TokenProvider<String> {
    override fun authenticateRequest(request: Request, token: String): Request =
            request
                    .newBuilder()
                    .header("auth", token)
                    .build()
}
