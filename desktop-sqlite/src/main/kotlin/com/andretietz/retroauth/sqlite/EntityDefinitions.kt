package com.andretietz.retroauth.sqlite

import org.jetbrains.exposed.dao.IntEntity
import org.jetbrains.exposed.dao.IntEntityClass
import org.jetbrains.exposed.dao.id.EntityID

internal class User(id: EntityID<Int>) : IntEntity(id) {
  companion object : IntEntityClass<User>(Users)
  var name by Users.name
  var email by Users.email
  var type by Users.type
  var active by Users.active
}

internal class Credential(id: EntityID<Int>) : IntEntity(id) {
  companion object : IntEntityClass<Credential>(Credentials)
  var key by Credentials.key
  var value by Credentials.value
  var user by User referencedOn Credentials.user
}
