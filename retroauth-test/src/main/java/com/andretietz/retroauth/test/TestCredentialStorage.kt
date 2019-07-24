package com.andretietz.retroauth.test

import com.andretietz.retroauth.Callback
import com.andretietz.retroauth.CredentialStorage
import java.util.concurrent.Future

class TestCredentialStorage @JvmOverloads constructor(
  private val credentials: MutableMap<String, MutableMap<String, String>> = HashMap()
) : CredentialStorage<String, String, String> {

  override fun getCredentials(owner: String, type: String, callback: Callback<String>?): Future<String> {
    requireNotNull(requireNotNull(credentials[owner])[type]).also {
      callback?.onResult(it)
      return TestFuture.create(it)
    }
  }

  override fun removeCredentials(owner: String, type: String, credentials: String) {
    this.credentials[owner]?.remove(type)
  }

  override fun storeCredentials(owner: String, type: String, credentials: String) {
    if (this.credentials[owner] == null) this.credentials[owner] = HashMap()
    if (this.credentials[owner]?.get(type) == null) requireNotNull(
      this.credentials[owner])[type] = credentials
  }
}
