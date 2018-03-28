package com.andretietz.retroauth.demo

import com.andretietz.retroauth.AndroidToken
import com.andretietz.retroauth.TokenProvider
import okhttp3.Request

/**
 * This is an optimistic implementation of facebook as token provider.
 *
 * If the token for some reason is invalid, the returning 401 will cause the deletion of the token and a retry of the
 * call, in which it will get refreshed
 */
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
