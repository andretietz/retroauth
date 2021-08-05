package com.andretietz.retroauth.sqlite

import org.jetbrains.exposed.dao.IntEntity
import org.jetbrains.exposed.dao.IntEntityClass
import org.jetbrains.exposed.dao.id.EntityID

internal class DatabaseUser(id: EntityID<Int>) : IntEntity(id) {
  companion object : IntEntityClass<DatabaseUser>(UserTable)

  var name by UserTable.name
  var email by UserTable.email
  var type by UserTable.type
  var active by UserTable.active
}

/**
 * This table should be encrypted at one point.
 */
internal class DatabaseCredential(id: EntityID<Int>) : IntEntity(id) {
  companion object : IntEntityClass<DatabaseCredential>(CredentialTable)

  var user by DatabaseUser referencedOn CredentialTable.user
  var type by CredentialTable.type
  var value by CredentialTable.value
}

internal class DatabaseData(id: EntityID<Int>) : IntEntity(id) {
  companion object : IntEntityClass<DatabaseData>(DataTable)

  var user by DatabaseUser referencedOn DataTable.user
  var credential by DatabaseCredential referencedOn DataTable.credential
  var key by DataTable.key
  var value by DataTable.value
}
