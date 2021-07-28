package com.andretietz.retroauth

data class RequestType<out OWNER_TYPE : Any>(
  val credentialType: CredentialType,
  val ownerType: OWNER_TYPE
)
