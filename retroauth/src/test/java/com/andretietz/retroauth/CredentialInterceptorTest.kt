package com.andretietz.retroauth

import com.nhaarman.mockito_kotlin.any
import com.nhaarman.mockito_kotlin.anyOrNull
import com.nhaarman.mockito_kotlin.doAnswer
import com.nhaarman.mockito_kotlin.doReturn
import com.nhaarman.mockito_kotlin.mock
import com.nhaarman.mockito_kotlin.never
import com.nhaarman.mockito_kotlin.times
import com.nhaarman.mockito_kotlin.verify
import io.reactivex.Single
import io.reactivex.schedulers.Schedulers
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.mockito.ArgumentMatchers.anyString
import retrofit2.Retrofit
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import java.util.concurrent.Future
import java.util.concurrent.TimeUnit

class CredentialInterceptorTest {

  private val server = MockWebServer()

  @Before
  fun prepareTest() {
    server.start()
  }

  @After
  fun cleanupTest() {
    server.shutdown()
  }

  @Test
  fun `unauthenticated call with successful response`() {
    server.enqueue(MockResponse().setBody("{\"data\": \"testdata\"}"))
    val methodCache = mock<MethodCache<String, String>> {
      // when this returns null, the call is not recognized as authenticated call.
      on { getCredentialType(any()) } doReturn null as RequestType<String, String>?
    }
    val ownerStorage = mock<OwnerStorage<String, String, String>>()
    val credentialStorage = mock<CredentialStorage<String, String, String>>()
    val authenticator = mock<Authenticator<String, String, String, String>>()
    val interceptor = CredentialInterceptor(
      authenticator,
      ownerStorage,
      credentialStorage,
      methodCache
    )


    val api = Retrofit.Builder()
      .baseUrl(server.url("/"))
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
  fun `authenticated call with successful response`() {
    server.enqueue(MockResponse().setBody("{\"data\": \"testdata\"}"))

    val methodCache = mock<MethodCache<String, String>> {
      // when this returns null, the call is not recognized as authenticated call.
      on { getCredentialType(any()) } doReturn RequestType("credentialType", "ownerType")
    }
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
      credentialStorage,
      methodCache
    )


    val api = Retrofit.Builder()
      .baseUrl(server.url("/"))
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


    testObserver.assertResult(Data("testdata"))
    verify(authenticator, times(1)).authenticateRequest(any(), anyString())
    assert(server.takeRequest().headers["auth-header"] == "auth-token")
  }
}

data class Data(val data: String)

interface SomeApi {
  @GET("/some/path")
  fun someCall(): Single<Data>
}
