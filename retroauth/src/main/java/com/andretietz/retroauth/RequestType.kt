package com.andretietz.retroauth

data class RequestType<out OWNER_TYPE : Any, out TOKEN_TYPE : Any>(
  val tokenType: TOKEN_TYPE,
  val ownerType: OWNER_TYPE
)