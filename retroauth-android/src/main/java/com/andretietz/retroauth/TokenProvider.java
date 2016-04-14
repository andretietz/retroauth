package com.andretietz.retroauth;

import okhttp3.Request;
import retrofit2.Retrofit;

/**
 * Created by andre on 13.04.2016.
 */
public interface TokenProvider {
    Request applyToken(String token, Request request);
    String refreshToken(Retrofit retrofit, String token);
}
