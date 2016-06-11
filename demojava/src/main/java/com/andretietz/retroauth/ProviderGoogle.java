package com.andretietz.retroauth;

import com.github.scribejava.core.model.OAuth2AccessToken;

import java.io.IOException;

import okhttp3.Request;
import okhttp3.Response;
import retrofit2.Retrofit;

/**
 * Created by andre on 15.04.2016.
 */
public class ProviderGoogle implements Provider<String, String, OAuth2AccessToken> {

    private Retrofit retrofit;

    @Override
    public Request authenticateRequest(Request request, OAuth2AccessToken accessToken) {
        return request.newBuilder()
                .header("Authorization", "Bearer " + accessToken.getAccessToken())
                .build();
    }

    @Override
    public boolean retryRequired(int count, Response response, TokenStorage<String, String, OAuth2AccessToken> tokenStorage, String account, String androidTokenType, OAuth2AccessToken androidToken) {
        if (!response.isSuccessful()) {
            if (response.code() == 401) {
                tokenStorage.removeToken(account, androidTokenType, androidToken);
                if (androidToken.getRefreshToken() != null) {
                    Google googleService = retrofit.create(Google.class);
                    try {
                        retrofit2.Response<Google.RefreshToken> refreshResponse = googleService.refreshToken(
                                androidToken.getRefreshToken(),
                                // as soon as there will be any trouble I will deactivate this demo project
                                "329078189044-q3g29v14uhnrbb5vsaj8d34j26vh4fb4.apps.googleusercontent.com",
                                "HOePqkgIemKIcNhfRt8_jpfF"

                        ).execute();
                        if (refreshResponse.isSuccessful()) {
                            Google.RefreshToken token = refreshResponse.body();
                            tokenStorage.storeToken(account, androidTokenType, new OAuth2AccessToken(token.accessToken, token.tokenType, token.expiresIn, androidToken.getRefreshToken(), androidToken.getScope(),refreshResponse.raw().message()));
                            return true;
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
        return false;
    }

    public void setRetrofit(Retrofit retrofit) {
        this.retrofit = retrofit;
    }
}
