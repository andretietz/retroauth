//package com.andretietz.retroauth
//
//import com.andretietz.retroauth.sqlite.Credential
//import com.andretietz.retroauth.sqlite.Credentials
//import com.andretietz.retroauth.sqlite.Credentials.key
//import com.andretietz.retroauth.sqlite.User
//import com.andretietz.retroauth.sqlite.Users
//import com.andretietz.retroauth.sqlite.Users.email
//import com.andretietz.retroauth.sqlite.Users.name
//import org.jetbrains.exposed.sql.and
//
//class SQLiteCredentialStorage : CredentialStorage<Account, String> {
//
//  override fun getCredentials(owner: Account, type: CredentialType): String? {
//    return User.find { Users.id eq owner.id }
//      .firstOrNull()
//      ?.let { user ->
//        Credential
//          .find { Credentials.user eq user and (key eq type.type) }
//          .firstOrNull()?.value
//      }
//  }
//
//  override fun removeCredentials(owner: Account, type: CredentialType) {
//    return User.find { name eq owner.name and (email eq owner.email) }
//      .firstOrNull()
//      ?.let {
//
//      }
//  }
//
//  override fun storeCredentials(owner: Account, type: CredentialType, credentials: String) {
//    return User.find { name eq owner.name and (email eq owner.email) }
//      .firstOrNull()
//      ?.let {
//  }
//}
