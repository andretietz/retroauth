package com.andretietz.retroauth.demo

import android.accounts.Account
import android.app.Application
import com.andretietz.retroauth.AndroidToken
import com.andretietz.retroauth.AndroidTokenType
import com.andretietz.retroauth.AuthAccountManager
import com.andretietz.retroauth.TokenProvider
import okhttp3.Request

/**
 * This is an optimistic implementation of facebook as token provider.
 *
 * If the token for some reason is invalid, the returning 401 will cause the deletion of the token and a retry of the
 * call, in which it will get refreshed
 */
class ProviderFacebook(application: Application, private val accountManager: AuthAccountManager)
    : TokenProvider<Account, AndroidTokenType, AndroidToken> {

    companion object {
        const val CLIENT_ID = "908466759214667"
        const val CLIENT_SECRET = "6254dc69ac506d95b897d35d0dcf9e1f"
        const val CLIENT_CALLBACK = "http://localhost:8000/accounts/facebook/login/callback/"
        const val KEY_TOKEN_VALIDITY = "token_validity"
    }

    private val tokenType = AndroidTokenType(
            application.getString(R.string.com_andretietz_retroauth_authentication_ACCOUNT),
            application.getString(R.string.com_andretietz_retroauth_authentication_TOKEN),
            setOf(KEY_TOKEN_VALIDITY))

    override fun createTokenType(annotationValues: IntArray): AndroidTokenType = tokenType

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

    override fun refreshToken(owner: Account, tokenType: AndroidTokenType, token: AndroidToken): AndroidToken? {
        return accountManager.getToken(owner, tokenType)
    }
}
