package com.andretietz.retroauth

import com.andretietz.retroauth.sqlite.*
import org.assertj.core.api.Assertions.assertThat
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.StdOutSqlLogger
import org.jetbrains.exposed.sql.addLogger
import org.jetbrains.exposed.sql.select
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.transactions.transaction
import org.junit.Test


class SQLiteOwnerStoreTest {
  val database = Database.connect("jdbc:h2:mem:test", driver = "org.h2.Driver")


  @Test
  fun test() {
    transaction(database) {
      addLogger(StdOutSqlLogger)
      SchemaUtils.create(UserTable, CredentialTable, DataTable)
      createCredential(createUser())
      val a = (UserTable innerJoin CredentialTable)
        .slice(UserTable.name, CredentialTable.value)
        .selectAll().execute(this)

      println(a)

    }

    transaction(database) {


      SchemaUtils.drop(UserTable, CredentialTable, DataTable)
    }
  }

  companion object {
    private fun createUser(
      name: String = "Test Dude",
      email: String = "test@dude.com",
      ownerType: String = "testownertype"
    ) = DatabaseUser.new {
      this.name = name
      this.email = email
      this.type = ownerType
    }

    private fun createCredential(
      user: DatabaseUser,
      credentialType: String = "credentialtype",
      token: String = "testtoken"
    ) = DatabaseCredential.new {
      this.user = user
      this.type = credentialType
      this.value = token
    }
  }
}
