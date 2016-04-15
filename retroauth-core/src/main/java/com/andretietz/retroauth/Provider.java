package com.andretietz.retroauth;

import okhttp3.Request;
import okhttp3.Response;

/**
 * Created by andre on 15/04/16.
 */
public interface Provider<OWNER, TOKEN_TYPE, TOKEN> {
    Request modifyRequest(Request request, TOKEN token);
    boolean retryRequired(int count, Response response, TokenStorage<OWNER, TOKEN_TYPE, TOKEN> tokenStorage, OWNER owner, TOKEN_TYPE type, TOKEN token);
}
