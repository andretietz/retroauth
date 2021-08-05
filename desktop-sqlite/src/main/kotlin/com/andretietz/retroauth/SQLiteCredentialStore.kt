package com.andretietz.retroauth

import com.andretietz.retroauth.sqlite.CredentialTable
import com.andretietz.retroauth.sqlite.DataTable
import com.andretietz.retroauth.sqlite.DatabaseCredential
import com.andretietz.retroauth.sqlite.DatabaseData
import com.andretietz.retroauth.sqlite.DatabaseUser
import com.andretietz.retroauth.sqlite.data.Account
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.transactions.transaction

class SQLiteCredentialStore(
  private val database: Database
) : CredentialStorage<Account> {

  override fun getCredentials(owner: Account, credentialType: String): Credentials? {
    return transaction(database) {
      DatabaseCredential.find {
        CredentialTable.user eq owner.id and (CredentialTable.type eq credentialType)
      }.toList()
        .firstOrNull()
        ?.let { credential ->
          Credentials(
            credential.value,
            DatabaseData.find { DataTable.user eq owner.id and (DataTable.credential eq credential.id) }
              .associate { it.key to it.value }
          )
        }
    }
  }


  override fun removeCredentials(owner: Account, credentialType: String) {
    transaction(database) {
      DatabaseUser.findById(owner.id)
        ?.let { user ->
          DatabaseCredential.find {
            CredentialTable.user eq owner.id and (CredentialTable.type eq credentialType)
          }.toList().firstOrNull()?.let { credential ->
            DatabaseData
              .find { DataTable.user eq user.id and (DataTable.credential eq credential.id) }
              .forEach { it.delete() }
            DatabaseCredential
              .find { CredentialTable.user eq user.id and (CredentialTable.type eq credentialType) }
              .firstOrNull()?.delete()
          }
        }
    }
  }

  override fun storeCredentials(owner: Account, credentialType: String, credentials: Credentials) {
    transaction(database) {
      DatabaseUser.findById(owner.id)
        ?.let { user ->
          val credential = DatabaseCredential.find {
            CredentialTable.user eq owner.id and (CredentialTable.type eq credentialType)
          }.toList().firstOrNull()
          val c = credential?.also {
            it.value = credentials.token
          } ?: DatabaseCredential.new {
            this.type = credentialType
            this.user = user
            this.value = credentials.token
          }
          DatabaseData
            .find { DataTable.user eq owner.id and (DataTable.credential eq c.id) }
            .toList()
          credentials.data?.map { (key, value) ->

            // TODO: see if we need to override things


            DatabaseData.new {
              this.user = user
              this.credential = c
              this.key = key
              this.value = value
            }
          }
        }
    }
  }
}
