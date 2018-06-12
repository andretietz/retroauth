package com.andretietz.retroauth

interface Callback<in T> {
  fun onResult(result: T?)
}