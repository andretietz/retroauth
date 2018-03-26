package com.andretietz.retroauth.testimpl;

import com.andretietz.retroauth.TokenProvider;

import org.jetbrains.annotations.NotNull;

import okhttp3.Request;
import okhttp3.Response;

public class TestProvider implements TokenProvider<String> {
    @Override
    public Request authenticateRequest(Request request, String s) {
        return request.newBuilder().header("auth", s).build();
    }


    @NotNull
    @Override
    public ResponseStatus validateResponse(int count, @NotNull Response response) {
        if (response.isSuccessful()) return ResponseStatus.OK;
        if (response.code() == 401) return ResponseStatus.RETRY_TOKEN_INVALID;
        return ResponseStatus.NO_RETRY_TOKEN_INVALID;
    }
}
