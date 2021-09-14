package com.andretietz.retroauth

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.withContext
import okhttp3.internal.wait
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test
import java.util.concurrent.atomic.AtomicInteger
import java.util.concurrent.atomic.AtomicReference

class LockingTest {


  @Test
  fun `locking mechanics`() = runBlocking {
    var c = 0
    val a = ApiEmulator {
//      println("execute: ${Thread.currentThread().id}")
      Thread.sleep(500)
      c++
      error("expected")
    }
    launch {
      repeat(200) {
        launch (Dispatchers.IO){
          a.requestEmulation(it)
        }
      }
    }.join()
    assertThat(c).isEqualTo(1)
    Unit
  }


}

class ApiEmulator(private val task: (i: Int) -> Unit) {
  companion object {
//    val lock = Mutex()
//    var error: Throwable? = null
//    var count: AtomicInteger = AtomicInteger(0)

    private val lock = AtomicReference(AccountTokenLock())
  }

  fun requestEmulation(t: Int) {
    try {
      lock()
      task(t)
    } catch (error: Throwable) {
      lock.get().errorContainer = error
    } finally {
      unlock()
    }
  }

  private fun lock() = runBlocking {
    if (!lock.get().lock.tryLock()) {
//      println("waiting ${Thread.currentThread().id}")
      lock.get().count.incrementAndGet()
      lock.get().lock.lock()
      lock.get().errorContainer?.let {
        throw it
      }
    } else {
      lock.get().count.incrementAndGet()
    }
  }

  private fun unlock() {
    if (lock.get().count.decrementAndGet() <= 0) {
      lock.get().errorContainer = null
    }
    println(lock.get().count)
    lock.get().lock.unlock()
  }
}

internal data class AccountTokenLock(
  val lock: Mutex = Mutex(),
  var errorContainer: Throwable? = null,
  val count: AtomicInteger = AtomicInteger(0)
)
