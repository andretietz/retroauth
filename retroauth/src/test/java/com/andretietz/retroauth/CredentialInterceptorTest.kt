package com.andretietz.retroauth

import com.nhaarman.mockitokotlin2.any
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.never
import com.nhaarman.mockitokotlin2.verify
import com.nhaarman.mockitokotlin2.doReturn
import com.nhaarman.mockitokotlin2.times
import com.nhaarman.mockitokotlin2.anyOrNull
import com.nhaarman.mockitokotlin2.doAnswer
import io.reactivex.Single
import io.reactivex.observers.TestObserver
import io.reactivex.schedulers.Schedulers
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import okhttp3.mockwebserver.MockResponse
import org.junit.Rule
import org.junit.Test
import org.mockito.ArgumentMatchers.anyInt
import org.mockito.ArgumentMatchers.anyString
import retrofit2.Retrofit
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import java.util.concurrent.Future
import java.util.concurrent.TimeUnit

@Suppress("Detekt.LargeClass")
class CredentialInterceptorTest {
  @get:Rule
  internal val serverRule = MockServerRule()

  @Test
  fun `unauthenticated call with successful response`() {
    serverRule.server.enqueue(MockResponse().setBody("{\"data\": \"testdata\"}"))
    val ownerStorage = mock<OwnerStorage<String, String, String>>()
    val credentialStorage = mock<CredentialStorage<String, String, String>>()
    val authenticator = mock<Authenticator<String, String, String, String>>()
    val interceptor = CredentialInterceptor(
      authenticator,
      ownerStorage,
      credentialStorage
    )

    val api = Retrofit.Builder()
      .baseUrl(serverRule.server.url("/"))
      .addConverterFactory(GsonConverterFactory.create())
      .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
      .client(
        OkHttpClient.Builder()
          .addInterceptor(interceptor)
          .build()
      )
      .build().create(SomeApi::class.java)

    val testObserver = api.someCall()
      .subscribeOn(Schedulers.io())
      .test()
    testObserver.awaitTerminalEvent()
    testObserver.assertResult(Data("testdata"))

    verify(authenticator, never()).authenticateRequest(any(), anyString())
  }

  @Test
  fun `authenticated call, no owner existing`() {
    // setup ownerstore, without any owner existing
    val ownerStorage = mock<OwnerStorage<String, String, String>> {
      on { getActiveOwner(anyString()) } doReturn null as String?
      on { getOwners(anyString()) } doReturn emptyList<String>()
    }

    val credentialStorage = mock<CredentialStorage<String, String, String>>()
    val authenticator = mock<Authenticator<String, String, String, String>> {
      on { getCredentialType(any()) } doReturn CREDENTIAL_TYPE
      on { getOwnerType(any()) } doReturn OWNER_TYPE
    }

    val interceptor = CredentialInterceptor(
      authenticator,
      ownerStorage,
      credentialStorage
    )

    val api = Retrofit.Builder()
      .baseUrl(serverRule.server.url("/"))
      .addConverterFactory(GsonConverterFactory.create())
      .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
      .client(
        OkHttpClient.Builder()
          .addInterceptor(interceptor)
          .build()
      )
      .build().create(SomeApi::class.java)

    val testObserver = api.someAuthenticatedCall()
      .subscribeOn(Schedulers.io())
      .test()

    testObserver.awaitTerminalEvent()
    verify(ownerStorage, times(1)).createOwner(anyString(), anyString(), anyOrNull())
    testObserver.assertError { error -> error is AuthenticationRequiredException }
  }

  @Test
  fun `authenticated call with successful response`() {
    serverRule.server.enqueue(MockResponse().setBody("{\"data\": \"testdata\"}"))

    val ownerStorage = mock<OwnerStorage<String, String, String>> {
      on { getActiveOwner(anyString()) } doReturn "owner"
    }
    val credentialStorage = mock<CredentialStorage<String, String, String>> {
      on {
        getCredentials(any(), any(), anyOrNull())
      } doReturn object : Future<String> {
        override fun isDone() = false
        override fun get() = "credential"
        override fun get(p0: Long, p1: TimeUnit) = get()
        override fun cancel(p0: Boolean) = false
        override fun isCancelled() = false
      }
    }

    val authenticator = mock<Authenticator<String, String, String, String>> {
      on { getCredentialType(any()) } doReturn CREDENTIAL_TYPE
      on { getOwnerType(any()) } doReturn OWNER_TYPE
      on { isCredentialValid(anyString()) } doReturn true
      on { authenticateRequest(any(), anyString()) } doAnswer { invocationOnMock ->
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

    val api = Retrofit.Builder()
      .baseUrl(serverRule.server.url("/"))
      .addConverterFactory(GsonConverterFactory.create())
      .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
      .client(
        OkHttpClient.Builder()
          .addInterceptor(interceptor)
          .build()
      )
      .build().create(SomeApi::class.java)

    val testObserver = api.someAuthenticatedCall()
      .subscribeOn(Schedulers.io())
      .test()

    testObserver.awaitTerminalEvent()
    testObserver.assertResult(Data("testdata"))
    verify(authenticator, times(1)).authenticateRequest(any(), anyString())
    assert(serverRule.server.takeRequest().headers["auth-header"] == "auth-token")
  }

  @Test
  fun `Invalid token, refreshes token`() {
    serverRule.server.enqueue(MockResponse().setBody("{\"data\": \"testdata\"}"))

    val ownerStorage = mock<OwnerStorage<String, String, String>> {
      on { getActiveOwner(anyString()) } doReturn "owner"
    }
    val credentialStorage = mock<CredentialStorage<String, String, String>> {
      on {
        getCredentials(any(), any(), anyOrNull())
      } doReturn object : Future<String> {
        override fun isDone() = false
        override fun get() = "credential"
        override fun get(p0: Long, p1: TimeUnit) = get()
        override fun cancel(p0: Boolean) = false
        override fun isCancelled() = false
      }
    }
    val authenticator = mock<Authenticator<String, String, String, String>> {
      on { getCredentialType(any()) } doReturn CREDENTIAL_TYPE
      on { getOwnerType(any()) } doReturn OWNER_TYPE
      on { isCredentialValid(anyString()) } doReturn false // token is invalid
      on { authenticateRequest(any(), anyString()) } doAnswer { invocationOnMock ->
        (invocationOnMock.arguments[0] as Request)
          .newBuilder()
          .addHeader("auth-header", "auth-token")
          .build()
      }
      on {
        refreshCredentials(anyString(), anyString(), anyString())
      } doReturn "credential"
    }

    val interceptor = CredentialInterceptor(
      authenticator,
      ownerStorage,
      credentialStorage
    )

    val api = Retrofit.Builder()
      .baseUrl(serverRule.server.url("/"))
      .addConverterFactory(GsonConverterFactory.create())
      .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
      .client(
        OkHttpClient.Builder()
          .addInterceptor(interceptor)
          .build()
      )
      .build().create(SomeApi::class.java)

    val testObserver = api.someAuthenticatedCall()
      .subscribeOn(Schedulers.io())
      .test()

    testObserver.awaitTerminalEvent()
    testObserver.assertResult(Data("testdata"))
    // refresh credentials successfully
    verify(authenticator, times(1)).refreshCredentials(anyString(), anyString(), anyString())
    // store new token
    verify(credentialStorage, times(1)).storeCredentials(anyString(), anyString(), anyString())
    // authenticate request
    verify(authenticator, times(1)).authenticateRequest(any(), anyString())
    assert(serverRule.server.takeRequest().headers["auth-header"] == "auth-token")
  }

  @Test
  fun `Invalid token, refresh token fails`() {
    serverRule.server.enqueue(MockResponse().setBody("{\"data\": \"testdata\"}"))

    val ownerStorage = mock<OwnerStorage<String, String, String>> {
      on { getActiveOwner(anyString()) } doReturn "owner"
    }
    val credentialStorage = mock<CredentialStorage<String, String, String>> {
      on {
        getCredentials(any(), any(), anyOrNull())
      } doReturn object : Future<String> {
        override fun isDone() = false
        override fun get() = "credential"
        override fun get(p0: Long, p1: TimeUnit) = get()
        override fun cancel(p0: Boolean) = false
        override fun isCancelled() = false
      }
    }
    val authenticator = mock<Authenticator<String, String, String, String>> {
      on { getCredentialType(any()) } doReturn CREDENTIAL_TYPE
      on { getOwnerType(any()) } doReturn OWNER_TYPE
      on { isCredentialValid(anyString()) } doReturn false // token is invalid
      on { authenticateRequest(any(), anyString()) } doAnswer { invocationOnMock ->
        (invocationOnMock.arguments[0] as Request)
          .newBuilder()
          .addHeader("auth-header", "auth-token")
          .build()
      }
      on {
        refreshCredentials(anyString(), anyString(), anyString())
      } doReturn null
    }

    val interceptor = CredentialInterceptor(
      authenticator,
      ownerStorage,
      credentialStorage
    )

    val api = Retrofit.Builder()
      .baseUrl(serverRule.server.url("/"))
      .addConverterFactory(GsonConverterFactory.create())
      .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
      .client(
        OkHttpClient.Builder()
          .addInterceptor(interceptor)
          .build()
      )
      .build().create(SomeApi::class.java)

    val testObserver = api.someAuthenticatedCall()
      .subscribeOn(Schedulers.io())
      .test()

    testObserver.awaitTerminalEvent()
    testObserver.assertResult(Data("testdata"))

    // old credentials removed, ONCE
    verify(credentialStorage, times(1)).removeCredentials(anyString(), anyString(), anyString())
    // refresh credentials successfully
    verify(authenticator, times(1)).refreshCredentials(anyString(), anyString(), anyString())
    // authenticate request
    verify(authenticator, times(1)).authenticateRequest(any(), anyString())
    assert(serverRule.server.takeRequest().headers["auth-header"] == "auth-token")
  }

  @Test
  fun `Refresh required after failing call`() {
    serverRule.server.enqueue(MockResponse().setResponseCode(401))
    serverRule.server.enqueue(MockResponse().setBody("{\"data\": \"testdata\"}"))

    val ownerStorage = mock<OwnerStorage<String, String, String>> {
      on { getActiveOwner(anyString()) } doReturn "owner"
    }
    val credentialStorage = mock<CredentialStorage<String, String, String>> {
      on {
        getCredentials(any(), any(), anyOrNull())
      } doReturn object : Future<String> {
        override fun isDone() = false
        override fun get() = "credential"
        override fun get(p0: Long, p1: TimeUnit) = get()
        override fun cancel(p0: Boolean) = false
        override fun isCancelled() = false
      }
    }
    val authenticator = mock<Authenticator<String, String, String, String>> {
      on { getCredentialType(any()) } doReturn CREDENTIAL_TYPE
      on { getOwnerType(any()) } doReturn OWNER_TYPE
      on { isCredentialValid(anyString()) } doReturn true
      on { authenticateRequest(any(), anyString()) } doAnswer { invocationOnMock ->
        (invocationOnMock.arguments[0] as Request)
          .newBuilder()
          .addHeader("auth-header", "auth-token")
          .build()
      }
      on {
        refreshCredentials(anyString(), anyString(), anyString())
      } doReturn "credential"
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

    val api = Retrofit.Builder()
      .baseUrl(serverRule.server.url("/"))
      .addConverterFactory(GsonConverterFactory.create())
      .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
      .client(
        OkHttpClient.Builder()
          .addInterceptor(interceptor)
          .build()
      )
      .build().create(SomeApi::class.java)

    val testObserver = api.someAuthenticatedCall()
      .subscribeOn(Schedulers.io())
      .test()

    testObserver.awaitTerminalEvent()
    testObserver.assertResult(Data("testdata"))
    verify(authenticator, times(2)).authenticateRequest(any(), anyString())
    assert(serverRule.server.takeRequest().headers["auth-header"] == "auth-token")
  }

  @Test
  fun `Invalid token, refreshes token, 200 calls`() {
    val range = IntRange(0, 199)
    val ownerStorage = mock<OwnerStorage<String, String, String>> {
      on { getActiveOwner(anyString()) } doReturn "owner"
    }
    var credential = "old-credential"
    val credentialStorage = mock<CredentialStorage<String, String, String>> {
      on {
        getCredentials(any(), any(), anyOrNull())
      } doReturn object : Future<String> {
        override fun isDone() = false
        override fun get() = credential
        override fun get(p0: Long, p1: TimeUnit) = get()
        override fun cancel(p0: Boolean) = false
        override fun isCancelled() = false
      }
    }
    val authenticator = mock<Authenticator<String, String, String, String>> {
      on { getCredentialType(any()) } doReturn CREDENTIAL_TYPE
      on { getOwnerType(any()) } doReturn OWNER_TYPE
      on { isCredentialValid(anyString()) } doAnswer { invocationOnMock ->
        invocationOnMock.arguments[0] as String == "credential"
      }
      on { authenticateRequest(any(), anyString()) } doAnswer { invocationOnMock ->
        (invocationOnMock.arguments[0] as Request)
          .newBuilder()
          .addHeader("auth-header", "auth-token")
          .build()
      }
      on {
        refreshCredentials(anyString(), anyString(), anyString())
      } doAnswer {
        Thread.sleep(200)
        credential = "credential"
        credential
      }
    }

    val interceptor = CredentialInterceptor(
      authenticator,
      ownerStorage,
      credentialStorage
    )

    val api = Retrofit.Builder()
      .baseUrl(serverRule.server.url("/"))
      .addConverterFactory(GsonConverterFactory.create())
      .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
      .client(
        OkHttpClient.Builder()
          .addInterceptor(interceptor)
          .build()
      )
      .build().create(SomeApi::class.java)

    val calls = mutableListOf<TestObserver<Data>>()
    for (i in range) {
      serverRule.server.enqueue(MockResponse().setBody("{\"data\": \"testdata\"}"))
      calls.add(
        api.someAuthenticatedCall()
          .subscribeOn(Schedulers.io()) // makes sure a new thread is created foreach call
          .test()
      )
    }

    for (i in range) {
      calls[i].awaitTerminalEvent()
      calls[i].assertResult(Data("testdata"))
      assert(serverRule.server.takeRequest().headers["auth-header"] == "auth-token")
    }
    // refresh credentials successfully, ONCE
    verify(authenticator, times(1)).refreshCredentials(anyString(), anyString(), anyString())
    // store new token, ONCE
    verify(credentialStorage, times(1)).storeCredentials(anyString(), anyString(), anyString())
    // authenticate request -> 200 times (or how much range includes)
    verify(authenticator, times(range.count())).authenticateRequest(any(), anyString())
  }

  @Test
  fun `Invalid token, refreshes token an error occurs, 200 calls`() {
    val range = IntRange(0, 199)

    val ownerStorage = mock<OwnerStorage<String, String, String>> {
      on { getActiveOwner(anyString()) } doReturn "owner"
    }
    val credentialStorage = mock<CredentialStorage<String, String, String>> {
      on {
        getCredentials(any(), any(), anyOrNull())
      } doReturn object : Future<String> {
        override fun isDone() = false
        override fun get() = "old-credential"
        override fun get(p0: Long, p1: TimeUnit) = get()
        override fun cancel(p0: Boolean) = false
        override fun isCancelled() = false
      }
    }
    val authenticator = mock<Authenticator<String, String, String, String>> {
      on { getCredentialType(any()) } doReturn CREDENTIAL_TYPE
      on { getOwnerType(any()) } doReturn OWNER_TYPE
      on { isCredentialValid(anyString()) } doReturn false
      on {
        refreshCredentials(anyString(), anyString(), anyString())
      } doAnswer {
        Thread.sleep(400)
        error("whatever error is thrown")
      }
    }

    val interceptor = CredentialInterceptor(
      authenticator,
      ownerStorage,
      credentialStorage
    )

    val api = Retrofit.Builder()
      .baseUrl(serverRule.server.url("/"))
      .addConverterFactory(GsonConverterFactory.create())
      .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
      .client(
        OkHttpClient.Builder()
          .addInterceptor(interceptor)
          .build()
      )
      .build().create(SomeApi::class.java)

    val calls = mutableListOf<TestObserver<Data>>()
    for (i in range) {
      serverRule.server.enqueue(MockResponse().setBody("{\"data\": \"testdata\"}"))
      calls.add(
        api.someAuthenticatedCall()
          .subscribeOn(Schedulers.io()) // makes sure a new thread is created foreach call
          .test()
      )
    }
    for (i in range) {
      calls[i].awaitTerminalEvent()
      calls[i].assertError { error -> error is IllegalStateException && error.message == "whatever error is thrown" }
    }

    verify(ownerStorage, times(1)).getActiveOwner(anyString())
    // refresh credentials successfully, ONCE
    verify(authenticator, times(1)).refreshCredentials(anyString(), anyString(), anyString())
    // store new token, ONCE
    verify(credentialStorage, never()).storeCredentials(anyString(), anyString(), anyString())
    // authenticate request -> never
    verify(authenticator, never()).authenticateRequest(any(), anyString())
  }

  companion object {
    private const val OWNER_TYPE = "owner_type"
    private const val CREDENTIAL_TYPE = "credential_type"
  }
}

data class Data(val data: String)

interface SomeApi {
  @GET("/some/path")
  fun someCall(): Single<Data>

  @Authenticated
  @GET("/some/other/path")
  fun someAuthenticatedCall(): Single<Data>
}
