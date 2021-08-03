package com.andretietz.retroauth

import com.andretietz.retroauth.sqlite.User
import com.andretietz.retroauth.sqlite.Users
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.transactions.transaction

class SQLiteOwnerManager(
  private val database: Database
) : OwnerStorage<Account> {

  override suspend fun createOwner(ownerType: String, credentialType: CredentialType): Account? {
    TODO("Not yet implemented")
  }

  override fun getOwner(ownerType: String, ownerName: String): Account? = transaction(database) {
    User.find { Users.type eq ownerType }
      .firstOrNull()
      ?.let { Account(it.id.value, it.name, it.email) }
  }

  override fun getActiveOwner(ownerType: String): Account? = transaction(database) {
    User.find { Users.active eq true }
      .firstOrNull()
      ?.let { Account(it.id.value, it.name, it.email) }
  }


  override fun getOwners(ownerType: String): List<Account> = transaction(database) {
    User.all().map { Account(it.id.value, it.name, it.email) }
  }


  override fun switchActiveOwner(ownerType: String, owner: Account?) {
    transaction(database) {
      User.all().map {
        it.active = (owner != null &&
          it.type == ownerType &&
          it.name == owner.name &&
          it.email == owner.email)
      }
    }
  }

  override fun removeOwner(ownerType: String, owner: Account): Boolean {
    return transaction(database) {
      User.find { Users.type.eq(ownerType).and { Users.name.eq(owner.name) } }
        .map { it.delete() }
        .toList()
        .isNotEmpty()
    }
  }

}
