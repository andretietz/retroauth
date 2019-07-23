package com.andretietz.retroauth

data class RequestType<out OWNER_TYPE : Any, out CREDENTIAL_TYPE : Any>(
  val credentialType: CREDENTIAL_TYPE,
  val ownerType: OWNER_TYPE
)
