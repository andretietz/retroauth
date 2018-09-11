package com.andretietz.retroauth

import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.JUnit4

@RunWith(JUnit4::class)
class AndroidTokenTest {

  @Test
  fun dataCheck() {
    val androidToken = AndroidToken("token", mapOf("refresh" to "refresh"))
    assertEquals("token", androidToken.token)
    assertEquals("refresh", androidToken.data!!["refresh"])
  }
}
