package com.andretietz.retroauth

import okhttp3.mockwebserver.MockWebServer
import org.junit.rules.TestRule
import org.junit.runner.Description
import org.junit.runners.model.Statement

class MockServerRule : TestRule {
  val server = MockWebServer()
  override fun apply(base: Statement, description: Description) = object : Statement() {
    override fun evaluate() {
      server.start()
      base.evaluate()
      server.shutdown()
    }
  }
}
