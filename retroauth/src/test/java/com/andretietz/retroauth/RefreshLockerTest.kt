package com.andretietz.retroauth

import com.nhaarman.mockito_kotlin.doAnswer
import com.nhaarman.mockito_kotlin.mock
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
    val runnable = mock<Runnable> {
      on { run() } doAnswer {
        println("Entering ${Thread.currentThread().id} - ${System.currentTimeMillis()} ")
        Thread.sleep(2000)
        println("Leaving ${Thread.currentThread().id} - ${System.currentTimeMillis()} ")
        throw IllegalStateException()
      }
    }
    val amount = 200
    val list = mutableListOf<TestObserver<String>>()
    for (i in 0..amount) {
      list.add(createSingle(runnable).subscribeOn(Schedulers.io()).test())
    }

    for (i in 0..amount) {
      list[i]
        .awaitTerminalEvent()
//        .assertError { error -> error is IllegalStateException }
    }
  }

  private fun createSingle(runnable: Runnable): Single<String> {
    return Single.create<String> { emitter ->
      emitter.onSuccess(TestLocker(runnable).method("asd"))
    }
  }
}

internal class TestLocker(private val doSomething: Runnable) {

  companion object {
    private val TOKEN_TYPE_LOCKERS = HashMap<Any, AccountTokenLock>()
  }

  fun method(lock: String): String {
    try {
      lock(lock)
      try {
        doSomething.run()
      } catch (error: Exception) {
        storeAndThrowError(lock, error)
      }
    } finally {
      unlock(lock)
    }
    return lock
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
  private fun lock(type: String) {
    val lock = getLock(type)
    if (!lock.lock.tryLock()) {
      println("waiting nr: ${lock.waitCounter.incrementAndGet()}")

      lock.lock.lock()
      println("reenter")
      val exception = lock.errorContainer.get()
      if (exception != null) {
        println(exception.toString())
        throw exception
      }
      println("no exception, go on")
    }
  }

  private fun unlock(type: String) {
    val lock = getLock(type)
    println("unlocking: ${lock.waitCounter.get()}")
    if (lock.waitCounter.decrementAndGet() < 0) {
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
