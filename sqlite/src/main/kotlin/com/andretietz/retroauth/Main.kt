//package com.andretietz.retroauth
//
//import com.andretietz.retroauth.sqlite.CredentialTable
//import com.andretietz.retroauth.sqlite.DataTable
//import com.andretietz.retroauth.sqlite.DatabaseCredential
//import com.andretietz.retroauth.sqlite.DatabaseUser
//import com.andretietz.retroauth.sqlite.UserTable
//import com.andretietz.retroauth.sqlite.data.Account
//import org.jetbrains.exposed.sql.Database
//import org.jetbrains.exposed.sql.SchemaUtils
//import org.jetbrains.exposed.sql.StdOutSqlLogger
//import org.jetbrains.exposed.sql.addLogger
//import org.jetbrains.exposed.sql.selectAll
//import org.jetbrains.exposed.sql.transactions.transaction
//private const val OWNER_TYPE = "ownertype"
//
//fun main() {
//  val database = Database.connect("jdbc:h2:mem:test", driver = "org.h2.Driver")
////  Database.connect(
////    "jdbc:sqlite:accountdb.db",
////    user = "someuser",
////    password = "somepassword",
////    driver = "org.sqlite.JDBC"
////  )
//
//  val credentialStore = SQLiteCredentialStore(database)
//  val ownerStore: OwnerStorage<Account> = SQLiteOwnerStore(database)
//
//  transaction(database) {
//    addLogger(StdOutSqlLogger)
//    SchemaUtils.create(UserTable, CredentialTable, DataTable)
//
//    ownerStore.createOwner(CREDENTIAL_TYPE)?.let {
//      credentialStore.storeCredentials(it, CREDENTIAL_TYPE, Credentials("myfancytoken"))
//
//      val credential = credentialStore.getCredentials(it, CREDENTIAL_TYPE)
//      println(credential)
//    }
//
//    SchemaUtils.drop(UserTable, CredentialTable, DataTable)
//  }
//}
//
//
