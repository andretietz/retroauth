package com.andretietz.retroauth.test

import com.andretietz.retroauth.Retroauth
import okhttp3.Request

object TestRetroauth {

  const val OWNER_TYPE = "test-owner-type"
  const val OWNER = "test-account"
  const val CREDENTIAL_TYPE = "test-credential-type"
  const val CREDENTIAL = "test-credential"

  const val TEST_AUTH_HEADER_NAME = "test-auth-header"

  @JvmStatic @JvmOverloads
  fun createBuilder(
    ownerType: String = OWNER_TYPE,
    owner: String = OWNER,
    credentialType: String = CREDENTIAL_TYPE,
    credentials: String = CREDENTIAL,
    authenticate: (request: Request, credential: String) -> Request = { request, _ ->
      request.newBuilder().addHeader(TEST_AUTH_HEADER_NAME, credentials).build()
    }
  ): Retroauth.Builder<String, String, String, String> = Retroauth.Builder(
    TestAuthenticator(ownerType, credentials, authenticate),
    TestOwnerManager(
      owners = HashMap<String, MutableList<String>>().also {
        it[ownerType] = mutableListOf(owner)
      },
      currentlyActive = owner
    ),
    TestCredentialStorage(
      credentials = HashMap<String, MutableMap<String, String>>().also {
        it[owner] = mutableMapOf(
          credentials to credentialType
        )
      }
    )
  )
}
