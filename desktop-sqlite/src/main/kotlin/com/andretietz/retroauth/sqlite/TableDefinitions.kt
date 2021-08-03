package com.andretietz.retroauth.sqlite

import org.jetbrains.exposed.dao.id.IntIdTable


object Users : IntIdTable() {
  val active = bool("active").default(false)
  val type = varchar("type", 100)
  val name = varchar("name", 100)
  val email = varchar("email", 100)
}

object Credentials : IntIdTable() {
  val user = reference("user", Users)
  val key = varchar("key", 200)
  val value = text("value")
}
