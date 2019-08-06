package com.andretietz.retroauth

/**
 * required
testImplementation "org.jetbrains.kotlin:kotlin-stdlib:1.3.40"
testImplementation "junit:junit:4.12"
testImplementation "io.reactivex.rxjava2:rxjava:2.1.12"
testImplementation "com.nhaarman:mockito-kotlin:1.5.0"
 */

import com.nhaarman.mockito_kotlin.doAnswer
import com.nhaarman.mockito_kotlin.mock
import com.nhaarman.mockito_kotlin.times
import com.nhaarman.mockito_kotlin.verify
import io.reactivex.Single
import io.reactivex.observers.TestObserver
import io.reactivex.schedulers.Schedulers
import org.junit.Test
import java.util.concurrent.atomic.AtomicInteger
import java.util.concurrent.atomic.AtomicReference
import java.util.concurrent.locks.Lock
import java.util.concurrent.locks.ReentrantLock

class RefreshLockerTest {
  @Test
  fun testMulti() {
    /**
     * This runnable is simulating a token refresh & get token.
     * While this, something goes wrong and the refresh throws an exception.
     */
    val runnable = mock<Runnable> {
      on { run() } doAnswer {
        // refresh...
        Thread.sleep(200) // make sure it takes longer than 200 request to also try
        // something goes wrong
        throw IllegalStateException()
      }
    }

    // parallel outgoing requests
    val requestCount = 200
    // token types
    val tokenTypes = 1 // TODO: when increasing this number, test fails.
    // list of observers to test
    val list = mutableListOf<TestObserver<String>>()
    // this is a fake request interceptor
    val interceptor = FakeInterceptor(runnable)

    for (i in 0..requestCount) {
      list.add(
        Single.create<String> { emitter ->
          emitter.onSuccess(interceptor
            // when a string is dynamically created it's instance isn't the same
            // this is why I use a mapping here 'getLockingObject'
            .doRequest(getLockingObject("lock-${i % tokenTypes}"))
          )
        }
          // push to new thread (io scheduler creates more threads if required)
          .subscribeOn(Schedulers.io())
          .test() // subscribe
      )
    }

    list.map {
      // waiting until the observable (single) completes
      it.awaitTerminalEvent()
      // make sure it was throwing an IllegalStateException
      it.assertError { error -> error is IllegalStateException }
    }

    // each token type should be only refreshing the token once.
    verify(runnable, times(tokenTypes)).run()
  }

  private val lockingObjects = HashMap<String, String>()

  private fun getLockingObject(key: String): String {
    return if (lockingObjects.containsKey(key)) {
      lockingObjects[key]!!
    } else {
      lockingObjects[key] = key
      lockingObjects[key]!!
    }
  }
}

/**
 * This class is a fake, where in real life this would be our RequestInterceptor
 */
internal class FakeInterceptor(private val refreshEmulator: Runnable) {

  companion object {
    private val TOKEN_TYPE_LOCKERS = HashMap<Any, AccountTokenLock>()
  }

  fun doRequest(lock: String): String {
    try {
      lock(lock)
      try {
        refreshEmulator.run()
      } catch (error: Exception) {
        getLock(lock).errorContainer.set(error)
        throw error
      }
    } finally {
      unlock(lock)
    }
    // here is where the request would happen
    // do request...

    // it actually doesn't need to return anything in the test. this is just to have the api
    // as close to the RL one as possible
    return lock
  }


  private fun getLock(type: String): AccountTokenLock {
    synchronized(type) {
      val lock: AccountTokenLock = TOKEN_TYPE_LOCKERS[type] ?: AccountTokenLock()
      TOKEN_TYPE_LOCKERS[type] = lock
      return lock
    }
  }

  @Throws(Exception::class)
  private fun lock(type: String) {
    val lock = getLock(type)
    if (!lock.lock.tryLock()) {
      println("Thread ${Thread.currentThread().id} is waiting.")
      lock.waitCounter.incrementAndGet()
      lock.lock.lock()
      println("Thread ${Thread.currentThread().id} is locked.")
      val exception = lock.errorContainer.get()
      if (exception != null) {
        println(exception.toString())
        throw exception
      }
    } else {
      println("Thread ${Thread.currentThread().id} is locked.")
    }
  }

  private fun unlock(type: String) {
    val lock = getLock(type)
    println("unlocking Thread ${Thread.currentThread().id} waiting threads left: ${lock.waitCounter.get()}")
    if (lock.waitCounter.getAndDecrement() <= 0) {
      lock.errorContainer.set(null)
    }
    lock.lock.unlock()
  }

  internal data class AccountTokenLock(
    val lock: Lock = ReentrantLock(false),
    // atomic reference required?
    val errorContainer: AtomicReference<Throwable> = AtomicReference(),
    // atomic integer required?
    val waitCounter: AtomicInteger = AtomicInteger()
  )
}
