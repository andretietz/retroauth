package com.andretietz.retroauth.test

import com.andretietz.retroauth.Authenticator
import okhttp3.Request

/**
 * This is an Authenticator to test authentication with.
 */
class TestAuthenticator constructor(
  private val testOwnerType: String,
  private val credentials: String,
  private val authenticate: (request: Request, credential: String) -> Request = { request, _ -> request }
) :
  Authenticator<String, String, String, String>() {

  override fun authenticateRequest(request: Request, credential: String) = authenticate(request, credential)

  override fun getOwnerType(annotationOwnerType: Int) = testOwnerType

  override fun getCredentialType(annotationCredentialType: Int) = credentials
}
