package com.andretietz.retroauth.test

import com.andretietz.retroauth.Authenticator
import okhttp3.Request

/**
 * This is an Authenticator to test authentication with.
 */
class TestAuthenticator @JvmOverloads constructor(
  private val ownerType: String,
  private val credentialType: String,
  private val authenticate: (request: Request, credential: String) -> Request = { request, _ -> request }
) :
  Authenticator<String, String, String, String>() {

  override fun authenticateRequest(request: Request, credential: String) = authenticate(request, credential)

  override fun getOwnerType(annotationOwnerType: Int) = ownerType

  override fun getCredentialType(annotationCredentialType: Int) = credentialType
}
