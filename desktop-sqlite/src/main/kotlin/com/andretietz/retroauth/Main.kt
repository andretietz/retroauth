package com.andretietz.retroauth

import com.andretietz.retroauth.sqlite.CredentialTable
import com.andretietz.retroauth.sqlite.DataTable
import com.andretietz.retroauth.sqlite.DatabaseCredential
import com.andretietz.retroauth.sqlite.DatabaseUser
import com.andretietz.retroauth.sqlite.UserTable
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.StdOutSqlLogger
import org.jetbrains.exposed.sql.addLogger
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.transactions.transaction
private const val OWNER_TYPE = "ownertype"
private const val CREDENTIAL_TYPE = "credentialtype"
fun main() {
  val database = Database.connect(
    "jdbc:sqlite:accountdb.db",
    user = "someuser",
    password = "somepassword",
    driver = "org.sqlite.JDBC"
  )

//  val credentialStore = SQLiteCredentialStore(database)
  val ownerStore = SQLiteOwnerStore(database)

  transaction(database) {
    addLogger(StdOutSqlLogger)
    SchemaUtils.create(UserTable, CredentialTable, DataTable)
    SchemaUtils.create(UserTable, CredentialTable, DataTable)

    (UserTable innerJoin CredentialTable innerJoin DataTable).selectAll().execute(this)

//    ownerStore.createOwner("type")
//    val databaseUser = DatabaseUser.new {
//      name = "andre"
//      email = "some@mail.com"
//      type = OWNER_TYPE
//    }
//
//    DatabaseCredential.new {
//      user = databaseUser
//      type = CREDENTIAL_TYPE
//      value = "this is some token"
//    }



//    SchemaUtils.drop(UserTable, CredentialTable, DataTable)
  }
}


