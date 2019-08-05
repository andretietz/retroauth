package com.andretietz.retroauth

import com.nhaarman.mockito_kotlin.doAnswer
import com.nhaarman.mockito_kotlin.doThrow
import com.nhaarman.mockito_kotlin.mock
import com.nhaarman.mockito_kotlin.times
import com.nhaarman.mockito_kotlin.verify
import io.reactivex.Scheduler
import io.reactivex.Single
import io.reactivex.schedulers.Schedulers
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.Response
import okhttp3.mockwebserver.MockWebServer
import org.junit.After
import org.junit.Before
import org.junit.Test
import retrofit2.Retrofit
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import java.lang.IllegalStateException
import java.util.HashMap
import java.util.Observable
import java.util.concurrent.atomic.AtomicInteger
import java.util.concurrent.atomic.AtomicReference
import java.util.concurrent.locks.Lock
import java.util.concurrent.locks.ReentrantLock

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
  fun testSimple() {
    val runnable = mock<Runnable> {
      on { run() } doThrow IllegalStateException()
    }

    val api = Retrofit.Builder()
      .baseUrl(server.url("/"))
      .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
      .addConverterFactory(GsonConverterFactory.create())
      .client(OkHttpClient.Builder()
        .addInterceptor(TestCredentialInterceptor(runnable))
        .build()
      )
      .build().create(SomeApi::class.java)

    api.someAuthCall().test()
      .assertError { error -> error is IllegalStateException }

  }

  @Test
  fun testMulti() {
    val runnable = mock<Runnable> {
      on { run() } doAnswer  {
        println("${Thread.currentThread().id} - ${System.currentTimeMillis()} - locked...")
        Thread.sleep(5000)
        throw IllegalStateException()
      }
    }

    val api = Retrofit.Builder()
      .baseUrl(server.url("/"))
      .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
      .addConverterFactory(GsonConverterFactory.create())
      .client(OkHttpClient.Builder()
        .addInterceptor(TestCredentialInterceptor(runnable))
        .build()
      )
      .build().create(SomeApi::class.java)

    val list = mutableListOf<io.reactivex.Observable<Data>>()
    for (i in 1..10) {
      list.add(api.someAuthCall().toObservable().subscribeOn(Schedulers.io()))
    }

    io.reactivex.Observable.merge(list).test()
//      .assertError { error -> error is IllegalStateException }

    verify(runnable, times(1)).run()
  }
}


/**
 * This interceptor intercepts the okhttp requests and checks if authentication is required.
 * If so, it tries to get the owner of the credential, then tries to get the credential and
 * applies the credential to the request
 *
 * @param <OWNER> a type that represents the owner of a credential. Since there could be multiple users on one client.
 * @param <CREDENTIAL_TYPE> type of the credential that should be added to the request
 */
internal class TestCredentialInterceptor(private val doSomething: Runnable) : Interceptor {

  companion object {
    private val TOKEN_TYPE_LOCKERS = HashMap<Any, AccountTokenLock>()
  }

  override fun intercept(chain: Interceptor.Chain): Response? {
    var response: Response? = null
    var request = chain.request()

    // if the request does require authentication
    var pending = false
    var refreshRequested = false
//    var tryCount = 0
    do {
      try {
        // Lock foreach type
//        pending = lock(request.url().toString())

        synchronized(request.url().toString()) {
          println(request.url().toString())
          doSomething.run()
        }
        // do things...
      } catch (error: Exception) {
        storeAndThrowError(request.url().toString(), error)
      } finally {
        // release type lock
//        unlock(request.url().toString(), pending)
      }
      // execute the request
      response = chain.proceed(request)
      if (refreshRequested) response.close()
    } while (refreshRequested)
    return response
  }

  private fun storeAndThrowError(type: String, exception: Exception) {
    val unwrappedException = unwrapThrowable(exception)
    if (getLock(type).errorContainer.get() == null) {
      getLock(type).errorContainer.set(unwrappedException)
    }
    throw unwrappedException
  }

  private fun unwrapThrowable(throwable: Throwable): Throwable {
    if (
      throwable is AuthenticationCanceledException ||
      throwable is AuthenticationRequiredException
    ) return throwable
    throwable.cause?.let {
      return unwrapThrowable(it)
    }
    return throwable
  }

  private fun getLock(type: String): AccountTokenLock {
    synchronized(type) {
      val lock: AccountTokenLock = TOKEN_TYPE_LOCKERS[type] ?: AccountTokenLock()
      TOKEN_TYPE_LOCKERS[type] = lock
      return lock
    }
  }

  @Throws(Exception::class)
  private fun lock(type: String): Boolean {
    val lock = getLock(type)
    if (!lock.lock.tryLock()) {
      lock.lock.lock()
      val exception = lock.errorContainer.get()
      if (exception != null) {
        throw exception
      }
      return true
    }
    return false
  }

  private fun unlock(type: String, wasWaiting: Boolean) {
    val lock = getLock(type)
    if (wasWaiting && lock.waitCounter.decrementAndGet() <= 0) {
      lock.errorContainer.set(null)
    }
    lock.lock.unlock()
  }

  internal data class AccountTokenLock(
    val lock: Lock = ReentrantLock(true),
    val errorContainer: AtomicReference<Throwable> = AtomicReference(),
    val waitCounter: AtomicInteger = AtomicInteger()
  )
}


data class Data(val data: String)

interface SomeApi {
  @Authenticated
  @GET("/some/path")
  fun someAuthCall(): Single<Data>
}