package com.andretietz.retroauth

data class RequestType(
  val credentialType: CredentialType,
  val ownerType: String
)
