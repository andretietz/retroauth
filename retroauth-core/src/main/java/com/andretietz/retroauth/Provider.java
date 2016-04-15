package com.andretietz.retroauth;

import okhttp3.Request;
import okhttp3.Response;
import retrofit2.Retrofit;

/**
 * Created by andre on 15/04/16.
 */
public interface Provider<OWNER, TOKEN_TYPE, TOKEN> {
    Request modifyRequest(Request request, TOKEN token);
    boolean retryRequired(int count, Retrofit retrofit, Response response, TokenStorage<OWNER, TOKEN_TYPE, TOKEN> tokenStorage, OWNER owner, TOKEN_TYPE type, TOKEN token);
}
