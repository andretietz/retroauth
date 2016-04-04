package com.andretietz.retroauth;

import android.accounts.Account;
import android.app.Application;
import android.content.Context;
import android.support.annotation.NonNull;

import okhttp3.Request;
import okhttp3.Response;

public final class AndroidAuthenticationHandler implements AuthenticationHandler<AndroidTokenType> {

    private final TokenInjector tokenInjector;
    private final AuthAccountManager authAccountManager;

    public AndroidAuthenticationHandler(@NonNull Context context, @NonNull TokenInjector tokenInjector) {
        this.tokenInjector = tokenInjector;
        Context applicationContext = context.getApplicationContext();
        if (applicationContext instanceof Application) {
            authAccountManager = new AuthAccountManager(ContextManager.get((Application) applicationContext));
        } else {
            throw new RuntimeException("Invalid Context!");
        }
    }

    public Request modifyRequest(@NonNull Request request, @NonNull String token) {
        return tokenInjector.inject(request, token);
    }

    @Override
    public AndroidTokenType convert(String[] annotationValues) {
        return new AndroidTokenType.Builder()
                .accountType(annotationValues[0])
                .tokenType(annotationValues[1])
                .build();
    }

    @Override
    public Request handleAuthentication(Request request, AndroidTokenType type) throws Exception {
        String name = authAccountManager.getActiveAccountName(type.accountType, true);
        Account account = authAccountManager.getAccountByName(name, type.accountType);
        String token = authAccountManager.getAuthToken(account, type.accountType, type.tokenType);
        return modifyRequest(request, token);
    }

    @Override
    public boolean retryRequired(int count, Response response, AndroidTokenType type) {
        if (!response.isSuccessful()) {
            if (response.code() == 401) {
                authAccountManager.invalidateTokenFromActiveUser(type.accountType, type.tokenType);
                return (count < 2);
            }
        }
        return false;
    }
}
