package com.andretietz.retroauth

import org.junit.Assert.assertEquals
import org.junit.Test


class CredentialTest {
  @Test
  fun dataCheck() {
    val credentials = Credentials("token", mapOf("refresh" to "refresh"))
    assertEquals("token", credentials.token)
    assertEquals("refresh", requireNotNull(credentials.data)["refresh"])
  }
}
