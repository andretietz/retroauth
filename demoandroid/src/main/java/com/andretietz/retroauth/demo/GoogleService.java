package com.andretietz.retroauth.demo;

import com.andretietz.retroauth.Authenticated;
import com.squareup.moshi.Json;

import retrofit2.Call;
import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.GET;
import retrofit2.http.POST;

public interface GoogleService {

    @Authenticated({R.string.com_andretietz_retroauth_authentication_ACCOUNT, R.string.com_andretietz_retroauth_authentication_TOKEN})
    @GET("/oauth2/v1/userinfo")
    Call<Info> getUserInfo();

    @FormUrlEncoded
    @POST("/oauth2/v4/token?grant_type=refresh_token")
    Call<RefreshToken> refreshToken(@Field("refresh_token") String token, @Field("client_id") String clientId,
                                    @Field("client_secret") String clientSecret);

    class Info {
        public String name;
    }

    class RefreshToken {
        @Json(name = "access_token")
        String accessToken;
    }
}

