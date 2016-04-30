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

    @Override
    public Request authenticateRequest(Request request, AndroidToken androidToken) {
        return request.newBuilder()
                .header("Authorization", "Bearer " + androidToken.token)
                .build();
    }

    @Override
    public boolean retryRequired(int count, Retrofit retrofit, Response response, TokenStorage<Account, AndroidTokenType, AndroidToken> tokenStorage, Account account, AndroidTokenType androidTokenType, AndroidToken androidToken) {
        if (!response.isSuccessful()) {
            if (response.code() == 401) {
                tokenStorage.removeToken(account, androidTokenType, androidToken);
                if (androidToken.refreshToken != null) {
                    GoogleService googleService = retrofit.create(GoogleService.class);
                    try {
                        retrofit2.Response<GoogleService.RefreshToken> refreshResponse = googleService.refreshToken(
                                androidToken.refreshToken,
                                "329078189044-q3g29v14uhnrbb5vsaj8d34j26vh4fb4.apps.googleusercontent.com",
                                "HOePqkgIemKIcNhfRt8_jpfF"

                        ).execute();
                        if (refreshResponse.isSuccessful()) {
                            GoogleService.RefreshToken token = refreshResponse.body();
                            tokenStorage.storeToken(account, androidTokenType,
                                    new AndroidToken(token.accessToken, androidToken.refreshToken));
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
}
