package com.andretietz.retroauth.sqlite

import org.jetbrains.exposed.dao.id.IntIdTable


internal object UserTable : IntIdTable() {
  val active = bool("active").default(false)
  val type = varchar("type", 100)
  val name = varchar("name", 100)
  val email = varchar("email", 100)

  init {
    uniqueIndex("IDX_user", name, email, type)
  }
}

internal object CredentialTable : IntIdTable() {
  val user = reference("user_id", UserTable)
  val type = varchar("key", 200)
  val value = text("value")
}

internal object DataTable : IntIdTable() {
  val credential = reference("credential_id", CredentialTable)
  val user = reference("user_id", UserTable)
  val key = varchar("key", 200)
  val value = text("value")
  override val primaryKey = PrimaryKey(credential, user, name = "PK_data_user_credential")
}
