package com.andretietz.retroauth.test

import com.andretietz.retroauth.Callback
import com.andretietz.retroauth.TokenStorage
import java.util.concurrent.Future

class TestTokenStorage(
  private val tokens: MutableMap<String, MutableMap<String, String>> = HashMap()
) : TokenStorage<String, String, String> {

  override fun getToken(owner: String, type: String, callback: Callback<String>?): Future<String> {
    requireNotNull(requireNotNull(tokens[owner])[type]).also {
      callback?.onResult(it)
      return TestFuture.create(it)
    }
  }

  override fun removeToken(owner: String, type: String, token: String) {
    tokens[owner]?.remove(type)
  }

  override fun storeToken(owner: String, type: String, token: String) {
    if (tokens[owner] == null) tokens[owner] = HashMap()
    if (tokens[owner]?.get(type) == null) requireNotNull(tokens[owner])[type] = token
  }
}
