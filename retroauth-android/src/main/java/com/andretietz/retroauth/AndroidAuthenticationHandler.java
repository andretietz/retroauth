package com.andretietz.retroauth;

import android.accounts.Account;
import android.app.Activity;
import android.os.Parcelable;
import android.support.annotation.NonNull;

import okhttp3.Request;
import okhttp3.Response;

public final class AndroidAuthenticationHandler<T> implements AuthenticationHandler<AndroidTokenType, T> {

    private final TokenInjector<T> tokenInjector;
    private final AuthAccountManager authAccountManager;
    private T refreshApi;

    public AndroidAuthenticationHandler(@NonNull Activity activity, @NonNull TokenInjector<T> tokenInjector) {
        this.tokenInjector = tokenInjector;
        this.authAccountManager = new AuthAccountManager(ContextManager.get(activity));
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
                try {
                    authAccountManager.invalidateTokenFromActiveUser(type.accountType, type.tokenType);
                    String refreshToken = authAccountManager.getRefreshToken(type.accountType, type.tokenType);
                    if(refreshToken != null && refreshApi != null) {
                        tokenInjector.refreshToken(refreshApi, refreshToken, new TokenInjector.TokenStorage() {
                            @Override
                            public void storeToken(String token) {

                            }

                            @Override
                            public void storeRefreshToken(String token) {

                            }
                        });
                    }
                } catch (ChooseAccountCanceledException e) {
                    return false;
                }
                return (count < 2);
            }
        }
        return false;
    }

    @Override
    public void setRefreshApi(T refreshApi) {
        this.refreshApi = refreshApi;
    }
}
