package com.andretietz.retroauth

import com.andretietz.retroauth.sqlite.DatabaseUser
import com.andretietz.retroauth.sqlite.UserTable
import com.andretietz.retroauth.sqlite.data.Account
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.transactions.transaction

class SQLiteOwnerStore(
  private val database: Database
) : OwnerStorage<Account> {

  override suspend fun createOwner(ownerType: String, credentialType: String): Account? {
    // FIXME
    return transaction(database) {
      DatabaseUser.new {
        name = "andre"
        email = "me@andretietz.com"
        type = ownerType
        active = false
      }
        .let { Account(it.id.value, it.name, it.email) }
    }
  }

  override fun getOwner(ownerType: String, ownerName: String): Account? = transaction(database) {
    DatabaseUser.find { UserTable.type eq ownerType }
      .firstOrNull()
      ?.let { Account(it.id.value, it.name, it.email) }
  }

  override fun getActiveOwner(ownerType: String): Account? = transaction(database) {
    DatabaseUser.find { UserTable.active eq true }
      .firstOrNull()
      ?.let { Account(it.id.value, it.name, it.email) }
  }


  override fun getOwners(ownerType: String): List<Account> = transaction(database) {
    DatabaseUser.all().map { Account(it.id.value, it.name, it.email) }
  }


  override fun switchActiveOwner(ownerType: String, owner: Account?) {
    transaction(database) {
      DatabaseUser.all().map {
        it.active = (owner != null &&
          it.type == ownerType &&
          it.name == owner.name &&
          it.email == owner.email)
      }
    }
  }

  override fun removeOwner(ownerType: String, owner: Account): Boolean {
    return transaction(database) {
      DatabaseUser.find { UserTable.type.eq(ownerType).and { UserTable.name.eq(owner.name) } }
        .map { it.delete() }
        .toList()
        .isNotEmpty()
    }
  }

}
