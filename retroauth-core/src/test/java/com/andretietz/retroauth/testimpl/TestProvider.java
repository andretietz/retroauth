package com.andretietz.retroauth.testimpl;

import com.andretietz.retroauth.Provider;
import com.andretietz.retroauth.TokenStorage;

import okhttp3.Request;
import okhttp3.Response;

/**
 * Created by andre on 02.05.2016.
 */
public class TestProvider implements Provider<String, String, String> {
    @Override
    public Request authenticateRequest(Request request, String s) {
        return request;
    }

    @Override
    public boolean retryRequired(int count, Response response,
                                 TokenStorage<String, String, String> tokenStorage, String s, String s2, String s3) {
        return false;
    }
}
