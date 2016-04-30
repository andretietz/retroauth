package com.andretietz.retroauth;

import com.andretietz.retroauth.Authenticated;
import com.squareup.moshi.Json;

import retrofit2.Call;
import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.GET;
import retrofit2.http.POST;
import rx.Observable;

/**
 * Created by andre on 14.04.2016.
 */
public interface Google {
    String TOKEN_TYPE = "com.andretietz.retroauth.demo.TOKEN";

    @Authenticated({TOKEN_TYPE})
    @GET("/oauth2/v1/userinfo")
    Observable<Info> getEmails();

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
        @Json(name = "token_type")
        String tokenType;
        @Json(name = "expires_in")
        int expiresIn;
        @Json(name = "id_token")
        String idToken;
    }
}

