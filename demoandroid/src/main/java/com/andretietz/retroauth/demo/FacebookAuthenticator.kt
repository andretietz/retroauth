package com.andretietz.retroauth.demo

import android.accounts.Account
import android.app.Application
import com.andretietz.retroauth.AndroidToken
import com.andretietz.retroauth.AndroidTokenStorage
import com.andretietz.retroauth.AndroidTokenType
import com.andretietz.retroauth.Authenticator
import okhttp3.Request

/**
 * This is an optimistic implementation of facebook as [Authenticator].
 *
 * If the token for some reason is invalid, the returning 401 will cause the deletion of the token and a retry of the
 * call, in which it will get refreshed
 */
class FacebookAuthenticator(application: Application) : Authenticator<String, Account, AndroidTokenType, AndroidToken>() {

  private val tokenStorage by lazy { AndroidTokenStorage(application) }

  companion object {
    const val CLIENT_ID = "908466759214667"
    const val CLIENT_SECRET = "6254dc69ac506d95b897d35d0dcf9e1f"
    const val CLIENT_CALLBACK = "https://localhost:8000/accounts/facebook/login/callback/"
    const val KEY_TOKEN_VALIDITY = "token_validity"
  }

  val tokenType = AndroidTokenType(
    // type of the token
    application.getString(R.string.authentication_TOKEN),
    // key(s) of additional values to store to the token
    // i.e. token validity time
    setOf(KEY_TOKEN_VALIDITY)
  )

  val ownerType: String = application.getString(R.string.authentication_ACCOUNT)

  override fun getTokenType(annotationTokenType: Int): AndroidTokenType = tokenType

  override fun getOwnerType(annotationOwnerType: Int): String = ownerType

  override fun authenticateRequest(request: Request, token: AndroidToken): Request {
    return request.newBuilder()
      .header("Authorization", "Bearer " + token.token)
      .build()
  }

  override fun isTokenValid(token: AndroidToken): Boolean {
    token.data?.let {
      it[KEY_TOKEN_VALIDITY]?.let {
        // return false if the token is no longer valid
        return it.toLong() > System.currentTimeMillis()
      }
    }
    return true
  }

  override fun refreshToken(owner: Account, tokenType: AndroidTokenType, token: AndroidToken): AndroidToken? {
    // This is very implementation specific!
    // https://developers.facebook.com/docs/facebook-login/access-tokens/refreshing
    // `At any point, you can generate a new long-lived token by sending the
    // person back to the login flow used by your web app.`
    return tokenStorage.getToken(owner, tokenType).get()
  }
}
