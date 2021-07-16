package com.andretietz.retroauth

import com.andretietz.retroauth.sqlite.Credentials
import com.andretietz.retroauth.sqlite.Users
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.transactions.transaction

fun main() {
  val database = Database.connect(
    "jdbc:sqlite:accountdb.db",
    user = "someuser",
    password = "somepassword",
    driver = "org.sqlite.JDBC"
  )

  transaction(database) {
//    SchemaUtils.create(Users, Credentials)

    Users.insert {
      it[name] =  "Some Name"
      it[email] = "some@mail.com"
    }

    SchemaUtils.drop(Users, Credentials)
  }
}


