package com.andretietz.retroauth.test

import com.andretietz.retroauth.Authenticator
import okhttp3.Request

/**
 * This is an Authenticator to test authentication with.
 */
class TestAuthenticator constructor(
  private val testOwnerType: String,
  private val token: String,
  private val authenticate: (request: Request, token: String) -> Request = { request, _ -> request }
) :
  Authenticator<String, String, String, String>() {

  override fun authenticateRequest(request: Request, token: String) = authenticate(request, token)

  override fun getOwnerType(annotationOwnerType: Int) = testOwnerType

  override fun getTokenType(annotationTokenType: Int) = token
}
