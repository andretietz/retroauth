package com.andretietz.retroauth;

import okhttp3.Request;
import retrofit2.Retrofit;

/**
 * @param <TOKEN_TYPE> Token Type Object (in most cases this is a string) which will be created depending on the annotations in
 *                     the {@link Authenticated} annotation.
 */
public interface TokenApi<TOKEN_TYPE> {
    Request modifyRequest(String token, Request request);

    TOKEN_TYPE convert(String[] annotationValues);

    void receiveToken(OnTokenReceiveListener listener) throws Exception;

    String refreshToken(Retrofit retrofit, String token);

    interface OnTokenReceiveListener {
        void onTokenReceive(String token);
    }
}
