package com.andretietz.retroauth.testimpl

import com.andretietz.retroauth.Authenticated
import io.reactivex.Single
import retrofit2.http.GET

interface TestInterface {

    companion object {
        const val TEST_BODY = "{ \"value\" : 1 }"
    }

    @Authenticated
    @GET("some/authenticated/path")
    fun authenticatedMethod(): Single<TestResponse>

    @GET("some/unauthenticated/path")
    fun unauthenticatedMethod(): Single<TestResponse>

}
