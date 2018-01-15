package com.andretietz.retroauth.testimpl;

import com.andretietz.retroauth.Authenticated;

import retrofit2.http.GET;
import rx.Observable;

/**
 * Created by andre on 11.06.2016.
 */

public interface TestInterface {

    int OWNER_TYPE = 1;
    int TOKEN_TYPE = 2;

    String TEST_BODY = "{ \"value\" : 1 }";

    @Authenticated({OWNER_TYPE, TOKEN_TYPE})
    @GET("some/authenticated/path")
    Observable<TestResponse> authenticatedMethod();

    @GET("some/unauthenticated/path")
    Observable<TestResponse> unauthenticatedMethod();
}
