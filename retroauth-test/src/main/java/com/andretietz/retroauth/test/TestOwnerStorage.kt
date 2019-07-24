package com.andretietz.retroauth.test

import com.andretietz.retroauth.Callback
import com.andretietz.retroauth.OwnerStorage
import java.util.concurrent.Future

class TestOwnerStorage @JvmOverloads constructor(
  private val owners: Map<String, MutableList<String>> = HashMap(),
  private var currentlyActive: String? = null
) : OwnerStorage<String, String, String> {

  override fun createOwner(ownerType: String, credentialType: String, callback: Callback<String>?): Future<String> {
    throw IllegalStateException("Cannot create owners using the test class")
  }

  override fun getOwner(ownerType: String, ownerName: String): String? {
    if (owners[ownerType] != null) {
      if (owners[ownerType]?.contains(ownerName) == true) {
        return ownerName
      } else {
        throw IllegalStateException("Owner you want to switch to, does not exist!")
      }
    }
    return null
  }

  override fun openOwnerPicker(ownerType: String, callback: Callback<String?>?): Future<String?> {
    throw IllegalStateException("Cannot open the account picker using the test class.")
  }

  override fun getActiveOwner(ownerType: String): String? = currentlyActive

  override fun getOwners(ownerType: String): List<String> = owners[ownerType] ?: emptyList()

  override fun switchActiveOwner(ownerType: String, owner: String?) {
    if (owners[ownerType] != null) {
      if (owners[ownerType]?.contains(owner) == true) {
        currentlyActive = owner
      } else {
        throw IllegalStateException("Owner you want to switch to, does not exist!")
      }
    }
  }

  override fun removeOwner(ownerType: String, owner: String, callback: Callback<Boolean>?): Future<Boolean> {
    (owners[ownerType]?.remove(owner) != null).also {
      callback?.onResult(it)
      return TestFuture.create(it)
    }
  }
}
