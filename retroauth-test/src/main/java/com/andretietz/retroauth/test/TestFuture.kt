package com.andretietz.retroauth.test

import java.util.concurrent.Future
import java.util.concurrent.TimeUnit

internal object TestFuture {
  @JvmStatic
  fun <T> create(value: T): Future<T> = object : Future<T> {
    override fun isDone() = false

    override fun get() = value

    override fun get(p0: Long, p1: TimeUnit) = value

    override fun cancel(p0: Boolean) = false

    override fun isCancelled() = false
  }
}
