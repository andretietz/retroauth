package com.andretietz.retroauth.demo

import com.andretietz.retroauth.Authenticated

import retrofit2.http.GET

interface FacebookService {

  @Authenticated
  @GET("v5.0/me?fields=name,email")
  suspend fun getUserDetails(): User

  data class User(
    val name: String,
    val email: String
  )
}
