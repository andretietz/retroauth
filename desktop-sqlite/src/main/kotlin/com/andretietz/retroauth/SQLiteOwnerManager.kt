package com.andretietz.retroauth

import com.andretietz.retroauth.sqlite.Users
import org.jetbrains.exposed.sql.ResultRow
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.deleteWhere
import org.jetbrains.exposed.sql.select
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.transactions.transaction
import org.jetbrains.exposed.sql.update

class SQLiteOwnerManager(

) : OwnerStorage<String, Account, String> {

  override fun createOwner(ownerType: String, credentialType: String): Account {
    TODO("Not yet implemented")
  }

  override fun getOwner(ownerType: String, ownerName: String): Account? {
    return Users
      .select { Users.name.eq(ownerName) }
      .firstOrNull()
      ?.let { transform(it) }
  }

  override fun getActiveOwner(ownerType: String): Account? {
    return Users
      .select { Users.active.eq(true) }
      .firstOrNull()
      ?.let { transform(it) }
  }

  override fun getOwners(ownerType: String): List<Account> {
    return Users.selectAll()
      .map { transform(it) }
  }

  override fun switchActiveOwner(ownerType: String, owner: Account?) {
    transaction {
      Users.update({ Users.type.eq(ownerType) }) {
        it[active] = false
      }
      owner?.let {
        Users.update({ Users.type.eq(ownerType).and { Users.name.eq(it.name) } }) {
          it[active] = true
        }
      }
    }
  }

  override fun removeOwner(ownerType: String, owner: Account): Boolean {
    return transaction {
//      Credentials.deleteWhere {  }
      Users.deleteWhere { Users.type.eq(ownerType).and { Users.name.eq(owner.name) } }
    } > 0
  }

  private fun transform(result: ResultRow) = Account(result[Users.name], result[Users.email])

}
