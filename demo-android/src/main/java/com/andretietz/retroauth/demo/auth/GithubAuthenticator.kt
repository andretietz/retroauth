package com.andretietz.retroauth.demo.auth

import android.accounts.Account
import android.app.Application
import android.content.Context
import com.andretietz.retroauth.Authenticator
import com.andretietz.retroauth.Credentials
import com.andretietz.retroauth.demo.R
import okhttp3.Request

/**
 * This is an optimistic implementation of facebook as [Authenticator].
 *
 * If the credential for some reason is invalid, the returning 401 will cause the deletion of the credential and a retry of the
 * call, in which it will get refreshed
 */
class GithubAuthenticator(private val application: Application) : Authenticator<Account>() {

  companion object {
    const val CLIENT_ID = "bb86ddeb2dd22163192f"
    const val CLIENT_SECRET = "0b2a017a3e481c1cb69739ff5a6c4de37009ed7a"
    const val CLIENT_CALLBACK = "https://localhost:8000/accounts/github/login/callback/"

    @JvmStatic
    fun createTokenType(context: Context) = context.getString(R.string.authentication_TOKEN)
  }

  private val credentialType = createTokenType(application)

  override fun getCredentialType(credentialType: Int): String = this.credentialType

  override fun authenticateRequest(request: Request, credential: Credentials): Request {
    return request.newBuilder()
      .header("Authorization", "Bearer ${credential.token}")
      .build()
  }
}
