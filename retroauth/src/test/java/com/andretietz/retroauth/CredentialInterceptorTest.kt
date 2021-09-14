package com.andretietz.retroauth

import com.nhaarman.mockitokotlin2.any
import com.nhaarman.mockitokotlin2.doAnswer
import com.nhaarman.mockitokotlin2.doReturn
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.never
import com.nhaarman.mockitokotlin2.times
import com.nhaarman.mockitokotlin2.verify
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import okhttp3.Dispatcher
import okhttp3.HttpUrl
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import okhttp3.mockwebserver.MockResponse
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.fail
import org.junit.Rule
import org.junit.Test
import org.mockito.ArgumentMatchers.anyInt
import org.mockito.ArgumentMatchers.anyString
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET

@ExperimentalCoroutinesApi
@Suppress("Detekt.LargeClass")
class CredentialInterceptorTest {
  @get:Rule
  internal val serverRule = MockServerRule()

  @Test
  @ExperimentalCoroutinesApi
  fun `unauthenticated call with successful response`() = runBlocking {
    serverRule.server.enqueue(MockResponse().setBody("{\"data\": \"testdata\"}"))
    val ownerStorage = mock<OwnerStorage<String>>()
    val credentialStorage = mock<CredentialStorage<String>>()
    val authenticator = mock<Authenticator<String>>()
    val interceptor = CredentialInterceptor(
      authenticator,
      ownerStorage,
      credentialStorage
    )

    val api = createSomeApi(serverRule.server.url("/"), interceptor)

    val data = api.someCall()

    assertThat(data).isEqualTo(Data("testdata"))

    verify(authenticator, never()).authenticateRequest(any(), any())
    Unit
  }

  @Test
  fun `authenticated call, no owner existing`() = runBlocking {
    // setup ownerstore, without any owner existing
    val ownerStorage = mock<OwnerStorage<String>> {
      on { getActiveOwner() } doReturn null as String?
      on { getOwners() } doReturn emptyList()
    }

    val credentialStorage = mock<CredentialStorage<String>>()
    val authenticator = mock<Authenticator<String>> {
      on { getCredentialType(any()) } doReturn CREDENTIAL_TYPE
    }

    val interceptor = CredentialInterceptor(
      authenticator,
      ownerStorage,
      credentialStorage
    )

    val api = createSomeApi(serverRule.server.url("/"), interceptor)

    try {
      api.someAuthenticatedCall()
    } catch (error: AuthenticationRequiredException) {
      // This is an expected error!
    }
    // FIXME
    //verify(ownerStorage, times(1)).createOwner(anyString(), any())
    Unit
  }

  @Test
  fun `authenticated call with successful response`() = runBlocking {
    serverRule.server.enqueue(MockResponse().setBody("{\"data\": \"testdata\"}"))

    val ownerStorage = mock<OwnerStorage<String>> {
      on { getActiveOwner() } doReturn "owner"
    }
    val credentialStorage = mock<CredentialStorage<String>> {
      on {
        getCredentials(any(), any())
      } doReturn Credentials("credential")
    }

    val authenticator = mock<Authenticator<String>> {
      on { getCredentialType(any()) } doReturn CREDENTIAL_TYPE
      on { isCredentialValid(any()) } doReturn true
      on { authenticateRequest(any(), any()) } doAnswer { invocationOnMock ->
        (invocationOnMock.arguments[0] as Request)
          .newBuilder()
          .addHeader("auth-header", "auth-token")
          .build()
      }
    }

    val interceptor = CredentialInterceptor(
      authenticator,
      ownerStorage,
      credentialStorage
    )

    val api = createSomeApi(serverRule.server.url("/"), interceptor)

    val data = api.someAuthenticatedCall()

    assertThat(data).isEqualTo(Data("testdata"))
    verify(authenticator, times(1)).authenticateRequest(any(), any())
    assertThat(serverRule.server.takeRequest().headers["auth-header"]).isEqualTo("auth-token")
    Unit
  }

  @Test
  fun `Invalid token, refreshes token`() = runBlocking {
    serverRule.server.enqueue(MockResponse().setBody("{\"data\": \"testdata\"}"))

    val ownerStorage = mock<OwnerStorage<String>> {
      on { getActiveOwner() } doReturn "owner"
    }
    val credentialStorage = mock<CredentialStorage<String>> {
      on {
        getCredentials(any(), any())
      } doReturn Credentials("credential")
    }
    val authenticator = mock<Authenticator<String>> {
      on { getCredentialType(any()) } doReturn CREDENTIAL_TYPE
      on { isCredentialValid(any()) } doReturn false // token is invalid
      on { authenticateRequest(any(), any()) } doAnswer { invocationOnMock ->
        (invocationOnMock.arguments[0] as Request)
          .newBuilder()
          .addHeader("auth-header", "auth-token")
          .build()
      }
      on {
        refreshCredentials(anyString(), any(), any())
      } doReturn Credentials("credential")
    }

    val interceptor = CredentialInterceptor(
      authenticator,
      ownerStorage,
      credentialStorage
    )

    val api = createSomeApi(serverRule.server.url("/"), interceptor)

    val data = api.someAuthenticatedCall()

    assertThat(data).isEqualTo(Data("testdata"))
    verify(authenticator, times(1)).authenticateRequest(any(), any())
    assertThat(serverRule.server.takeRequest().headers["auth-header"]).isEqualTo("auth-token")
    // refresh credentials successfully
    verify(authenticator, times(1)).refreshCredentials(anyString(), any(), any())
    // store new token
    verify(credentialStorage, times(1)).storeCredentials(anyString(), any(), any())
  }

  @Test
  fun `Invalid token, refresh token fails`() = runBlocking {
    serverRule.server.enqueue(MockResponse().setBody("{\"data\": \"testdata\"}"))

    val ownerStorage = mock<OwnerStorage<String>> {
      on { getActiveOwner() } doReturn "owner"
    }
    val credentialStorage = mock<CredentialStorage<String>> {
      on {
        getCredentials(any(), any())
      } doReturn Credentials("credential")
    }
    val authenticator = mock<Authenticator<String>> {
      on { getCredentialType(any()) } doReturn CREDENTIAL_TYPE
      on { isCredentialValid(any()) } doReturn false // token is invalid
      on { authenticateRequest(any(), any()) } doAnswer { invocationOnMock ->
        (invocationOnMock.arguments[0] as Request)
          .newBuilder()
          .addHeader("auth-header", "auth-token")
          .build()
      }
      on {
        refreshCredentials(anyString(), any(), any())
      } doReturn null
    }

    val interceptor = CredentialInterceptor(
      authenticator,
      ownerStorage,
      credentialStorage
    )

    val api = createSomeApi(serverRule.server.url("/"), interceptor)

    val data = api.someAuthenticatedCall()

    assertThat(data).isEqualTo(Data("testdata"))
    verify(authenticator, times(1)).authenticateRequest(any(), any())
    assertThat(serverRule.server.takeRequest().headers["auth-header"]).isEqualTo("auth-token")
    // old credentials removed, ONCE
    verify(credentialStorage, times(1)).removeCredentials(anyString(), any())
    // refresh credentials successfully
    verify(authenticator, times(1)).refreshCredentials(anyString(), any(), any())

    Unit
  }

  @Test
  fun `Refresh required after failing call`() = runBlocking {
    serverRule.server.enqueue(MockResponse().setResponseCode(401))
    serverRule.server.enqueue(MockResponse().setBody("{\"data\": \"testdata\"}"))

    val ownerStorage = mock<OwnerStorage<String>> {
      on { getActiveOwner() } doReturn "owner"
    }
    val credentialStorage = mock<CredentialStorage<String>> {
      on {
        getCredentials(any(), any())
      } doReturn Credentials("credential")
    }
    val authenticator = mock<Authenticator<String>> {
      on { getCredentialType(any()) } doReturn CREDENTIAL_TYPE
      on { isCredentialValid(any()) } doReturn true
      on { authenticateRequest(any(), any()) } doAnswer { invocationOnMock ->
        (invocationOnMock.arguments[0] as Request)
          .newBuilder()
          .addHeader("auth-header", "auth-token")
          .build()
      }
      on {
        refreshCredentials(anyString(), any(), any())
      } doReturn Credentials("credential")
      on {
        refreshRequired(anyInt(), any())
      } doAnswer { invocationOnMock ->
        (invocationOnMock.arguments[1] as Response).code != 200
      }
    }

    val interceptor = CredentialInterceptor(
      authenticator,
      ownerStorage,
      credentialStorage
    )

    val api = createSomeApi(serverRule.server.url("/"), interceptor)

    val data = api.someAuthenticatedCall()

    assertThat(data).isEqualTo(Data("testdata"))
    verify(authenticator, times(2)).authenticateRequest(any(), any())
    assertThat(serverRule.server.takeRequest().headers["auth-header"]).isEqualTo("auth-token")

    Unit
  }

  /**
   * This test has been added in order to verify that multiple requests at the same time
   * don't trigger multiple refreshes. This has been a big issue when using this in production.
   * Intentionally I am using an absurd high number of simultaneous requests here to test the
   * robustness.
   *
   * @see [EXTREME_REQUEST_COUNT] the amount of requests for this test case
   */
  @Test
  fun `Invalid token, refreshes token, EXTREME_REQUEST_COUNT calls`() = runBlocking {
    val ownerStorage = mock<OwnerStorage<String>> {
      on { getActiveOwner() } doReturn "owner"
    }
    var credential = Credentials("old-credential")
    val credentialStorage = mock<CredentialStorage<String>> {
      on {
        getCredentials(any(), any())
      } doAnswer { credential }
    }
    val authenticator = mock<Authenticator<String>> {
      on { getCredentialType(any()) } doReturn CREDENTIAL_TYPE
      on { isCredentialValid(any()) } doAnswer { invocationOnMock ->
        (invocationOnMock.arguments[0] as Credentials).token == "credential"
      }
      on { authenticateRequest(any(), any()) } doAnswer { invocationOnMock ->
        (invocationOnMock.arguments[0] as Request)
          .newBuilder()
          .addHeader("auth-header", "auth-token")
          .build()
      }
      on {
        refreshCredentials(anyString(), any(), any())
      } doAnswer {
        Thread.sleep(200)
        credential = Credentials("credential")
        credential
      }
    }

    val interceptor = CredentialInterceptor(
      authenticator,
      ownerStorage,
      credentialStorage
    )

    val api = createSomeApi(serverRule.server.url("/"), interceptor)

    launch {
      repeat(EXTREME_REQUEST_COUNT) {
        launch {
          serverRule.server.enqueue(MockResponse().setBody("{\"data\": \"testdata\"}"))
          api.someAuthenticatedCall()
        }
      }
    }.join()

    // refresh credentials successfully, ONCE
    verify(authenticator, times(1)).refreshCredentials(anyString(), any(), any())
    // store new token, ONCE
    verify(credentialStorage, times(1)).storeCredentials(anyString(), any(), any())
    // authenticate request -> 200 times (or how much range includes)
    verify(authenticator, times(EXTREME_REQUEST_COUNT)).authenticateRequest(any(), any())

    Unit
  }

  /**
   * Same problem as above only that in here we verify that if the refresh throws an error,
   * all requests waiting to do the refresh as well are skipped and rethrow the error
   * produced by the first one.
   */
  @Test
  fun `Invalid token, refreshes token an error occurs, 200 calls`() = runBlocking {
    val ownerStorage = mock<OwnerStorage<String>> {
      on { getActiveOwner() } doReturn "owner"
    }
    val credentialStorage = mock<CredentialStorage<String>> {
      on {
        getCredentials(any(), any())
      } doReturn Credentials("old-credential")
    }
    val authenticator = mock<Authenticator<String>> {
      on { getCredentialType(any()) } doReturn CREDENTIAL_TYPE
      on { isCredentialValid(any()) } doReturn false
      on {
        refreshCredentials(anyString(), any(), any())
      } doAnswer {
        // with this we make sure that the first request within the lock
        // takes a bit longer, so that all other 199 requests queue up before the first call
        // unlocks the mutex in the CredentialInterceptor
        Thread.sleep(500)
        error("some error was thrown")
      }
    }

    val interceptor = CredentialInterceptor(
      authenticator,
      ownerStorage,
      credentialStorage
    )
    val api = createSomeApi(serverRule.server.url("/"), interceptor)

    launch {
      repeat(200) {
        serverRule.server.enqueue(MockResponse().setBody("{\"data\": \"testdata\"}"))
        launch(Dispatchers.IO) {
          try {
            api.someAuthenticatedCall()
            fail("This part of the code should never be reached")
          } catch (error: Throwable) {
            assertThat(error.message).contains("some error was thrown")
          }
        }
      }
    }.join()

    // get the active owner ONCE
    verify(ownerStorage, times(1)).getActiveOwner()
    // try to refresh credentials, ONCE
    // this is throwing an exception
    verify(authenticator, times(1)).refreshCredentials(anyString(), any(), any())
    // store new token, ONCE
    verify(credentialStorage, never()).storeCredentials(anyString(), any(), any())
    // authenticate request -> never
    verify(authenticator, never()).authenticateRequest(any(), any())

    Unit
  }

  companion object {
    private const val CREDENTIAL_TYPE = "credential_type"

    private const val EXTREME_REQUEST_COUNT = 200

    private fun createSomeApi(url: HttpUrl, interceptor: Interceptor): SomeApi {
      return Retrofit.Builder()
        .baseUrl(url)
        .addConverterFactory(GsonConverterFactory.create())
        .client(
          OkHttpClient.Builder()
            /**
             * Since okhttp supports 5 connections at a time. in order to test what we want to test we need
             * to increase that limit.
             * https://square.github.io/okhttp/4.x/okhttp/okhttp3/-dispatcher/
             * https://stackoverflow.com/questions/42299791/okhttpclient-limit-number-of-connections
             */
            .dispatcher(Dispatcher().also {
              it.maxRequests = EXTREME_REQUEST_COUNT
              it.maxRequestsPerHost = EXTREME_REQUEST_COUNT
            })
            .addInterceptor(interceptor)
            .build()
        )
        .build().create(SomeApi::class.java)
    }
  }
}

data class Data(val data: String)

interface SomeApi {
  @GET("/some/path")
  suspend fun someCall(): Data

  @Authenticated
  @GET("/some/other/path")
  suspend fun someAuthenticatedCall(): Data
}
