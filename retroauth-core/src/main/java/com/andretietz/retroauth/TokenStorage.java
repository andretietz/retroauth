package com.andretietz.retroauth;

/**
 * Created by andre on 23.03.2016.
 */
public interface TokenStorage<TOKEN_TYPE> {
    String getToken(TOKEN_TYPE type);
    String getRefreshToken(TOKEN_TYPE type);

    void removeToken(TOKEN_TYPE type, String token);
    void removeRefreshToken(TOKEN_TYPE type, String refreshToken);

    void saveToken(TOKEN_TYPE type, String token);
}
