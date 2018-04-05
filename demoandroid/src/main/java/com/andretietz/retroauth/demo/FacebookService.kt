package com.andretietz.retroauth.demo

import com.andretietz.retroauth.Authenticated

import io.reactivex.Single
import retrofit2.http.GET

interface FacebookService {

    @Authenticated
    @GET("v2.11/me?fields=name,email")
    fun getUserDetails(): Single<User>


    class User {
        var name: String? = null
        var email: String? = null

        override fun toString(): String {
            return String.format("%s (%s)", name, email)
        }
    }
}

