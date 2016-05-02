package com.andretietz.retroauth.dummy;

import java.io.IOException;

import okhttp3.Connection;
import okhttp3.Interceptor;
import okhttp3.Request;
import okhttp3.Response;


public class DummyInterceptorChain implements Interceptor.Chain {
    @Override
    public Request request() {
        return new Request.Builder()
                .url("http://www.google.com")
                .build();
    }

    @Override
    public Response proceed(Request request) throws IOException {
        return null;
    }

    @Override
    public Connection connection() {
        return null;
    }
}
