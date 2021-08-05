package com.andretietz.retroauth

import com.andretietz.retroauth.sqlite.DatabaseUser
import com.andretietz.retroauth.sqlite.UserTable
import com.andretietz.retroauth.sqlite.data.Account
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.transactions.transaction

class SQLiteOwnerStore(
  private val database: Database,
  private val showCreateOwnerUI: (credentialType: String) -> Account?
) : OwnerStorage<Account> {

  override fun createOwner(credentialType: String): Account? {
    return showCreateOwnerUI(credentialType)
      ?.also { switchActiveOwner(it) }
  }


  override fun getOwner(ownerName: String): Account? = transaction(database) {
    DatabaseUser.find { UserTable.name eq ownerName }
      .firstOrNull()
      ?.let { Account(it.id.value, it.name, it.email) }
  }

  override fun getActiveOwner(): Account? = transaction(database) {
    DatabaseUser.find { UserTable.active eq true }
      .firstOrNull()
      ?.let { Account(it.id.value, it.name, it.email) }
  }


  override fun getOwners(): List<Account> = transaction(database) {
    DatabaseUser.all().map { Account(it.id.value, it.name, it.email) }
  }


  override fun switchActiveOwner(owner: Account?) {
    transaction(database) {
      DatabaseUser.all().map {
        it.active = (owner != null &&
          it.name == owner.name &&
          it.email == owner.email)
      }
    }
  }

  override fun removeOwner(owner: Account): Boolean {
    return transaction(database) {
      DatabaseUser.find { UserTable.name eq owner.name }
        // TODO remove credentials and data also!
        .map { it.delete() }
        .toList()
        .isNotEmpty()
    }
  }

}
