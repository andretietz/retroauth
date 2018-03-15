package com.andretietz.retroauth.demo;

import android.accounts.Account;

import com.andretietz.retroauth.AndroidToken;
import com.andretietz.retroauth.AndroidTokenType;
import com.andretietz.retroauth.Provider;
import com.andretietz.retroauth.TokenStorage;

import java.io.IOException;

import okhttp3.Request;
import okhttp3.Response;
import retrofit2.Retrofit;

/**
 * Created by andre on 15.04.2016.
 */
public class ProviderGoogle implements Provider<Account, AndroidTokenType, AndroidToken> {

    public static final String GOOGLE_CLIENT_ID = "329078189044-q3g29v14uhnrbb5vsaj8d34j26vh4fb4.apps.googleusercontent.com";
    public static final String GOOGLE_CLIENT_SECRET = "HOePqkgIemKIcNhfRt8_jpfF";
    public static final String GOOGLE_CLIENT_CALLBACK = "http://localhost:8000/accounts/google/login/callback/";

    private GoogleService googleService;

    @Override
    public Request authenticateRequest(Request request, AndroidToken androidToken) {
        return request.newBuilder()
                .header("Authorization", "Bearer " + androidToken.getToken())
                .build();
    }

    @Override
    public boolean retryRequired(int count, Response response, TokenStorage<Account, AndroidTokenType, AndroidToken> tokenStorage, Account account, AndroidTokenType androidTokenType, AndroidToken androidToken) {
        if (!response.isSuccessful()) {
            if (response.code() == 401) {
                tokenStorage.removeToken(account, androidTokenType, androidToken);
                if (androidToken.getRefreshToken() != null) {
                    try {
                        retrofit2.Response<GoogleService.RefreshToken> refreshResponse = googleService.refreshToken(
                                androidToken.getRefreshToken(),
                                GOOGLE_CLIENT_ID,
                                GOOGLE_CLIENT_SECRET

                        ).execute();
                        if (refreshResponse.isSuccessful()) {
                            GoogleService.RefreshToken token = refreshResponse.body();
                            tokenStorage.storeToken(account, androidTokenType,
                                    new AndroidToken(token.accessToken, androidToken.getRefreshToken()));
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
            return true;
        }
        return false;
    }

    public void onRetrofitCreated(Retrofit retrofit) {
        this.googleService = retrofit.create(GoogleService.class);
    }
}
