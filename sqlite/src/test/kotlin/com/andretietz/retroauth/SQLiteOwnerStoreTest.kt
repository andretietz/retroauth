package com.andretietz.retroauth

import com.andretietz.retroauth.sqlite.CredentialTable
import com.andretietz.retroauth.sqlite.DataTable
import com.andretietz.retroauth.sqlite.DatabaseUser
import com.andretietz.retroauth.sqlite.UserTable
import com.andretietz.retroauth.sqlite.data.Account
import com.nhaarman.mockitokotlin2.doAnswer
import com.nhaarman.mockitokotlin2.mock
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.fail
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.StdOutSqlLogger
import org.jetbrains.exposed.sql.addLogger
import org.jetbrains.exposed.sql.transactions.transaction
import org.junit.Test
import org.mockito.ArgumentMatchers.anyString


class SQLiteOwnerStoreTest {
  private val database = Database.connect("jdbc:h2:mem:test", driver = "org.h2.Driver")
  private val ownerStore: OwnerStorage<Account> = SQLiteOwnerStore(database) {
    DatabaseUser.new {
      name = "some name"
      email = "some@name.com"
    }
      .let { Account(it.id.value, it.name, it.email) }
  }

  @Test
  fun `calls the closure to create an owner`() {
    transaction(database) {
      addLogger(StdOutSqlLogger)
      SchemaUtils.create(UserTable, CredentialTable, DataTable)
      val owner = ownerStore.createOwner("type")
      assertThat(owner!!.name).isEqualTo("some name")
      assertThat(owner.email).isEqualTo("some@name.com")
      SchemaUtils.drop(UserTable, CredentialTable, DataTable)
    }
  }

  @Test
  fun `cannot create the same user twice`() {
    transaction(database) {
      addLogger(StdOutSqlLogger)
      SchemaUtils.create(UserTable, CredentialTable, DataTable)
      try {
        ownerStore.createOwner("type")
        ownerStore.createOwner("type")
        fail("You shouldn't be able to create the same user twice!")
      } catch (_: Throwable) {
      } finally {
        SchemaUtils.drop(UserTable, CredentialTable, DataTable)
      }
    }
  }

  @Test
  fun `newly created user is autmatically the active one`() {
    transaction(database) {
      addLogger(StdOutSqlLogger)
      SchemaUtils.create(UserTable, CredentialTable, DataTable)
      val owner = ownerStore.createOwner("type")
      val activeOwner = ownerStore.getActiveOwner()

      assertThat(activeOwner).isEqualTo(owner)
      SchemaUtils.drop(UserTable, CredentialTable, DataTable)
    }
  }

  @Test
  fun `getting the owner requires the correct name`() {
    transaction(database) {
      addLogger(StdOutSqlLogger)
      SchemaUtils.create(UserTable, CredentialTable, DataTable)
      ownerStore.createOwner("type")

      val nonExistent = ownerStore.getOwner("nonexistent")
      val owner = ownerStore.getOwner("some name")

      assertThat(nonExistent).isNull()
      assertThat(owner).isNotNull

      SchemaUtils.drop(UserTable, CredentialTable, DataTable)
    }
  }

  @Test
  fun `getOwners returns all created users`() {
    val accounts = listOf(
      Account(1, "one", "one"),
      Account(2, "two", "two")
    )
    var ac = 0
    val mockCreateUser = mock<(credentialType: String) -> Account?> {
      on { it(anyString()) } doAnswer {
        val account = accounts[(ac++) % 2]
        DatabaseUser.new {
          name = account.name
          email = account.email
        }
        account
      }
    }
    val ownerStore = SQLiteOwnerStore(database, mockCreateUser)
    transaction(database) {
      addLogger(StdOutSqlLogger)
      SchemaUtils.create(UserTable, CredentialTable, DataTable)
      ownerStore.createOwner("type")
      ownerStore.createOwner("type")

      assertThat(ownerStore.getOwners().size).isEqualTo(2)

      SchemaUtils.drop(UserTable, CredentialTable, DataTable)
    }
  }


  @Test
  fun `remove an owner actually removes it`() {
    transaction(database) {
      addLogger(StdOutSqlLogger)
      SchemaUtils.create(UserTable, CredentialTable, DataTable)
      val owner = ownerStore.createOwner("type")!!

      assertThat(ownerStore.removeOwner(owner)).isEqualTo(true)
      assertThat(ownerStore.getActiveOwner()).isNull()
      assertThat(ownerStore.getOwners().size).isEqualTo(0)

      SchemaUtils.drop(UserTable, CredentialTable, DataTable)
    }
  }

  @Test
  fun `switching the owner works`() {
    val accounts = listOf(
      Account(1, "one", "one"),
      Account(2, "two", "two")
    )
    var ac = 0
    val mockCreateUser = mock<(credentialType: String) -> Account?> {
      on { it(anyString()) } doAnswer {
        val account = accounts[(ac++) % 2]
        DatabaseUser.new {
          name = account.name
          email = account.email
        }
        account
      }
    }
    val ownerStore = SQLiteOwnerStore(database, mockCreateUser)
    transaction(database) {
      addLogger(StdOutSqlLogger)
      SchemaUtils.create(UserTable, CredentialTable, DataTable)

      val owner1 = ownerStore.createOwner("type")!!
      val owner2 = ownerStore.createOwner("type")!!

      assertThat(owner1).isNotNull
      assertThat(owner2).isNotNull
      assertThat(ownerStore.getOwners().size).isEqualTo(2)

      assertThat(ownerStore.getActiveOwner()).isEqualTo(owner2)

      ownerStore.switchActiveOwner(owner1)

      assertThat(ownerStore.getActiveOwner()).isEqualTo(owner1)

      SchemaUtils.drop(UserTable, CredentialTable, DataTable)
    }
  }

}
