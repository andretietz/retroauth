package com.andretietz.retroauth

import com.andretietz.retroauth.sqlite.Credentials
import com.andretietz.retroauth.sqlite.User
import com.andretietz.retroauth.sqlite.Users
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.transactions.transaction

fun main() {
  val database = Database.connect(
    "jdbc:sqlite:accountdb.db",
    user = "someuser",
    password = "somepassword",
    driver = "org.sqlite.JDBC"
  )

  transaction(database) {
    SchemaUtils.create(Users, Credentials)

    User.new {
      name = "Some Name"
      email = "some@mail.com"
      type = "testtype"
    }

    val user = User[1]

    println(user)
//    User.all().forEach { println(it) }

//    SchemaUtils.drop(Users, Credentials)
  }
}


