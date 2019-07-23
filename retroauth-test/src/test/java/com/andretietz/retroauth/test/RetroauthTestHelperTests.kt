package com.andretietz.retroauth.test

import com.andretietz.retroauth.Authenticated
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import retrofit2.Call
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET

class RetroauthTestHelperTests {

  private val server = MockWebServer()
  private lateinit var retrofit: Retrofit

  @Before
  fun prepareTest() {
    server.start()
    retrofit = TestRetroauth.createBuilder()
      .baseUrl(server.url("/"))
      .addConverterFactory(GsonConverterFactory.create())
      .build()
  }

  @After
  fun cleanupTest() {
    server.shutdown()
  }

  @Test
  fun authRequestInTest() {
    server.enqueue(MockResponse().setBody("{\"data\": \"testdata\"}"))
    val api = retrofit.create(SomeApi::class.java)

    val response = api.someAuthCall().execute()
    val data = response.body()

    assertTrue(data != null)
    assertEquals(requireNotNull(data).data, "testdata")
    assertTrue(server.takeRequest().headers[TestRetroauth.TEST_AUTH_HEADER_NAME] == TestRetroauth.CREDENTIAL)
  }
}

data class Data(val data: String)

interface SomeApi {
  @Authenticated
  @GET("/some/path")
  fun someAuthCall(): Call<Data>
}
