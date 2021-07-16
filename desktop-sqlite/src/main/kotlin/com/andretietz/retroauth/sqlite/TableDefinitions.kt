package com.andretietz.retroauth.sqlite

import org.jetbrains.exposed.sql.Table


object Users : Table() {
  val id = integer("id")
  val active = bool("active")
  val type = varchar("type", 100)
  val name = varchar("name", 100)
  val email = varchar("email", 100)
  override val primaryKey = PrimaryKey(id)
}

object Credentials : Table() {
  val id = integer("id")
  val userId = integer("user_id") references Users.id
  val key = varchar("key", 200)
  val content = text("value")
  override val primaryKey = PrimaryKey(Users.id)
}
