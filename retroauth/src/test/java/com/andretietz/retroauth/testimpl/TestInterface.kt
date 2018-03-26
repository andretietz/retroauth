package com.andretietz.retroauth.testimpl

import com.andretietz.retroauth.Authenticated

import retrofit2.http.GET
import rx.Observable

interface TestInterface {

    companion object {
        const val TEST_BODY = "{ \"value\" : 1 }"
    }

    @Authenticated
    @GET("some/authenticated/path")
    fun authenticatedMethod(): Observable<TestResponse>

    @GET("some/unauthenticated/path")
    fun unauthenticatedMethod(): Observable<TestResponse>


}
