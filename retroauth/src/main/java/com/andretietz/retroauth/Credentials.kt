package com.andretietz.retroauth

class Credentials @JvmOverloads constructor(
  val token: String,
  val data: Map<String, String>? = null
)
