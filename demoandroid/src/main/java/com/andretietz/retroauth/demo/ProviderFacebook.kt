package com.andretietz.retroauth.demo

import android.app.Application
import com.andretietz.retroauth.AndroidOwner
import com.andretietz.retroauth.AndroidToken
import com.andretietz.retroauth.AndroidTokenStorage
import com.andretietz.retroauth.AndroidTokenType
import com.andretietz.retroauth.TokenProvider
import okhttp3.Request

/**
 * This is an optimistic implementation of facebook as token provider.
 *
 * If the token for some reason is invalid, the returning 401 will cause the deletion of the token and a retry of the
 * call, in which it will get refreshed
 */
class ProviderFacebook(application: Application)
    : TokenProvider<String, AndroidOwner, AndroidTokenType, AndroidToken>() {


    private val tokenStorage by lazy { AndroidTokenStorage(application) }

    companion object {
        const val CLIENT_ID = "908466759214667"
        const val CLIENT_SECRET = "6254dc69ac506d95b897d35d0dcf9e1f"
        const val CLIENT_CALLBACK = "http://localhost:8000/accounts/facebook/login/callback/"
        const val KEY_TOKEN_VALIDITY = "token_validity"
    }

    val tokenType = AndroidTokenType(
            application.getString(R.string.com_andretietz_retroauth_authentication_TOKEN),
            setOf(KEY_TOKEN_VALIDITY))

    val ownerType = application.getString(R.string.com_andretietz_retroauth_authentication_ACCOUNT)

    override fun getTokenType(annotationValues: IntArray?): AndroidTokenType = tokenType

    override fun getOwnerType(annotationValues: IntArray?): String = ownerType

    override fun authenticateRequest(request: Request, token: AndroidToken): Request {
        return request.newBuilder()
                .header("Authorization", "Bearer " + token.token)
                .build()
    }

    override fun isTokenValid(token: AndroidToken): Boolean {
        token.data?.let {
            it[KEY_TOKEN_VALIDITY]?.let {
                return it.toLong() > System.currentTimeMillis()
            }
        }
        return true
    }

    override fun refreshToken(owner: AndroidOwner, tokenType: AndroidTokenType, token: AndroidToken): AndroidToken? {
        return tokenStorage.getToken(owner, tokenType)
    }
}
