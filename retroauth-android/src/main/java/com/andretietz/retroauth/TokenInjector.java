package com.andretietz.retroauth;

import okhttp3.Request;

/**
 * Created by andre.tietz on 30/03/16.
 */
public interface TokenInjector<T> {
    Request inject(Request originalRequest, String token);
    void refreshToken(T refreshApi, String refreshToken, TokenStorage storage);

    interface TokenStorage {
        void storeToken(String token);
        void storeRefreshToken(String token);
    }
}
