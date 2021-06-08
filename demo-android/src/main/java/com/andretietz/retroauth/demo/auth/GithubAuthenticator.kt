package com.andretietz.retroauth.demo.auth

import android.accounts.Account
import android.app.Application
import android.content.Context
import com.andretietz.retroauth.AndroidCredentialStorage
import com.andretietz.retroauth.AndroidCredentialType
import com.andretietz.retroauth.AndroidCredentials
import com.andretietz.retroauth.AndroidOwnerStorage
import com.andretietz.retroauth.Authenticator
import com.andretietz.retroauth.demo.R
import okhttp3.Request

/**
 * This is an optimistic implementation of facebook as [Authenticator].
 *
 * If the credential for some reason is invalid, the returning 401 will cause the deletion of the credential and a retry of the
 * call, in which it will get refreshed
 */
class GithubAuthenticator(application: Application) : Authenticator<String, Account, AndroidCredentialType, AndroidCredentials>() {

  private val credentialStorage by lazy { AndroidCredentialStorage(application) }
  private val ownerStorage by lazy { AndroidOwnerStorage(application) }

  companion object {
    const val CLIENT_ID = "bb86ddeb2dd22163192f"
    const val CLIENT_SECRET = "0b2a017a3e481c1cb69739ff5a6c4de37009ed7a"
    const val CLIENT_CALLBACK = "https://localhost:8000/accounts/github/login/callback/"
    const val KEY_TOKEN_VALIDITY = "credential_validity"

    @JvmStatic
    fun createTokenType(context: Context): AndroidCredentialType {
      return AndroidCredentialType(
        // type of the credential
        context.getString(R.string.authentication_TOKEN),
        // key(s) of additional values to store to the credential
        // i.e. credential validity time
        setOf(KEY_TOKEN_VALIDITY)
      )
    }
  }

  private val credentialType = createTokenType(application)

  private val ownerType: String = application.getString(R.string.authentication_ACCOUNT)

  override fun getCredentialType(annotationCredentialType: Int): AndroidCredentialType = credentialType

  override fun getOwnerType(annotationOwnerType: Int): String = ownerType

  override fun authenticateRequest(request: Request, credential: AndroidCredentials): Request {
    return request.newBuilder()
      .header("Authorization", "Bearer ${credential.token}")
      .build()
  }

  override fun refreshCredentials(
    owner: Account,
    credentialType: AndroidCredentialType,
    credential: AndroidCredentials
  ): AndroidCredentials? {
    // This is very implementation specific!
    // https://developers.facebook.com/docs/facebook-login/access-tokens/refreshing
    // `At any point, you can generate a new long-lived credential by sending the
    // person back to the login flow used by your web app.`
    ownerStorage.createOwner(owner.type, credentialType).get()
    return credentialStorage.getCredentials(owner, credentialType).get()
  }
}
