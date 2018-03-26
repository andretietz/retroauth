package com.andretietz.retroauth.demo

import com.andretietz.retroauth.AndroidToken
import com.andretietz.retroauth.TokenProvider
import okhttp3.Request

class ProviderFacebook : TokenProvider<AndroidToken> {

    override fun authenticateRequest(request: Request, token: AndroidToken): Request {
        return request.newBuilder()
                .header("Authorization", "Bearer " + token.token)
                .build()
    }

    companion object {
        const val CLIENT_ID = "908466759214667"
        const val CLIENT_SECRET = "6254dc69ac506d95b897d35d0dcf9e1f"
        const val CLIENT_CALLBACK = "http://localhost:8000/accounts/facebook/login/callback/"
    }
}
