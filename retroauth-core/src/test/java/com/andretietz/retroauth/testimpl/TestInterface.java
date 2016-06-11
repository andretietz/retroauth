package com.andretietz.retroauth.testimpl;

import com.andretietz.retroauth.Authenticated;

import retrofit2.Call;
import retrofit2.http.GET;

/**
 * Created by andre on 11.06.2016.
 */

public interface TestInterface {
    @Authenticated({"some", "token", "type"})
    @GET("some/path")
    Call<TestResponse> authenticatedMethod();

    @GET("some/path")
    Call<TestResponse> unauthenticatedMethod();
}