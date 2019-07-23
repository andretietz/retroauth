package com.andretietz.retroauth.test

import com.andretietz.retroauth.Retroauth
import okhttp3.Request

object RetroauthTestHelper {

  const val OWNER_TYPE = "test-owner-type"
  const val OWNER = "test-account"
  const val TOKEN_TYPE = "test-token-type"
  const val TOKEN = "test-token"

  const val TEST_AUTH_HEADER_NAME = "test-auth-header"

  @JvmStatic @JvmOverloads
  fun createBuilder(
    ownerType: String = OWNER_TYPE,
    owner: String = OWNER,
    tokenType: String = TOKEN_TYPE,
    token: String = TOKEN,
    authenticate: (request: Request, token: String) -> Request = { request, _ ->
      request.newBuilder().addHeader(TEST_AUTH_HEADER_NAME, token).build()
    }
  ): Retroauth.Builder<String, String, String, String> = Retroauth.Builder(
    TestAuthenticator(ownerType, token, authenticate),
    TestOwnerManager(
      owners = HashMap<String, MutableList<String>>().also {
        it[ownerType] = mutableListOf(owner)
      },
      currentlyActive = owner
    ),
    TestTokenStorage(
      tokens = HashMap<String, MutableMap<String, String>>().also {
        it[owner] = mutableMapOf(
          token to tokenType
        )
      }
    )
  )
}
