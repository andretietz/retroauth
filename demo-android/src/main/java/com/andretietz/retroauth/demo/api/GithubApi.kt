package com.andretietz.retroauth.demo.api

import com.andretietz.retroauth.Authenticated
import com.squareup.moshi.JsonClass
import retrofit2.http.GET

interface GithubApi {

  /**
   * https://docs.github.com/en/rest/reference/repos#list-repositories-for-the-authenticated-user
   */
//  @Authenticated
  @GET("user/repos?visibility=private")
  suspend fun getRepositories(): List<Repository>

  @JsonClass(generateAdapter = true)
  data class Repository(
    val id: String,
    val name: String,
    val url: String,
    val private: Boolean
  )
}
