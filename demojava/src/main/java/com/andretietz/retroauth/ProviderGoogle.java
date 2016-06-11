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
    public boolean retryRequired(int count, Response response, TokenStorage<String, String, OAuth2AccessToken> tokenStorage, String account, String tokenType, OAuth2AccessToken oauthToken) {
        // if request was not successful
        if (!response.isSuccessful()) {
            // request was unauthorized
            if (response.code() == 401) {
                // remove the token used for this request
                tokenStorage.removeToken(account, tokenType, oauthToken);
                // check if there is a refresh token to use
                if (oauthToken.getRefreshToken() != null) {
                    Google googleService = retrofit.create(Google.class);
                    try {
                        // try refreshing the token
                        retrofit2.Response<Google.RefreshToken> refreshResponse = googleService.refreshToken(
                                oauthToken.getRefreshToken(),
                                // as soon as there will be any trouble I will deactivate this demo project
                                "329078189044-q3g29v14uhnrbb5vsaj8d34j26vh4fb4.apps.googleusercontent.com",
                                "HOePqkgIemKIcNhfRt8_jpfF"
                        ).execute();
                        // if refreshing was successful
                        if (refreshResponse.isSuccessful()) {
                            Google.RefreshToken token = refreshResponse.body();
                            // store new token
                            tokenStorage.storeToken(account, tokenType, new OAuth2AccessToken(token.accessToken, token.tokenType, token.expiresIn, oauthToken.getRefreshToken(), oauthToken.getScope(),refreshResponse.raw().message()));
                            // retry the actual request
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

    /**
     * This is a not too nice implementation but it keeps the example simple
     */
    public void setRetrofit(Retrofit retrofit) {
        this.retrofit = retrofit;
    }
}
