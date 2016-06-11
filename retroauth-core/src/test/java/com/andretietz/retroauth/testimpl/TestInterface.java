package com.andretietz.retroauth.testimpl;

import com.andretietz.retroauth.Authenticated;

import retrofit2.http.GET;
import rx.Observable;

/**
 * Created by andre on 11.06.2016.
 */

public interface TestInterface {

    String TEST_BODY = "{ \"value\" : 1 }";

    @Authenticated({"some", "token", "type"})
    @GET("some/authenticated/path")
    Observable<TestResponse> authenticatedMethod();

    @GET("some/unauthenticated/path")
    Observable<TestResponse> unauthenticatedMethod();
}
