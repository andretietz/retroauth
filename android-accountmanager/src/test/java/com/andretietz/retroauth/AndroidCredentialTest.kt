package com.andretietz.retroauth

import org.junit.Assert.assertEquals
import org.junit.Test


class AndroidCredentialTest {

  @Test
  fun dataCheck() {
    val androidCredentials = AndroidCredentials("token", mapOf("refresh" to "refresh"))
    assertEquals("token", androidCredentials.token)
    assertEquals("refresh", requireNotNull(androidCredentials.data)["refresh"])
  }
}
