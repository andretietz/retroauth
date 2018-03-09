package com.andretietz.retroauth.demo;

import android.accounts.Account;

import com.andretietz.retroauth.AndroidToken;
import com.andretietz.retroauth.AndroidTokenType;
import com.andretietz.retroauth.Provider;
import com.andretietz.retroauth.TokenStorage;

import okhttp3.Request;
import okhttp3.Response;

public class ProviderGithub implements Provider<Account, AndroidTokenType, AndroidToken> {

    public static final String CLIENT_ID = "405f730d96862da912a8";
    public static final String CLIENT_SECRET = "dce0264a8c9eb94689d4d8ffbe1fadb59c33c4c3";
    public static final String CLIENT_CALLBACK = "http://localhost:8000/accounts/github/login/callback/";

    @Override
    public Request authenticateRequest(Request request, AndroidToken androidToken) {
        return request.newBuilder()
                .header("Authorization", "Bearer " + androidToken.token)
                .build();
    }

    @Override
    public boolean retryRequired(int count,
                                 Response response,
                                 TokenStorage<Account, AndroidTokenType, AndroidToken> tokenStorage,
                                 Account account,
                                 AndroidTokenType androidTokenType,
                                 AndroidToken androidToken) {
        return false;
    }
}
