package com.andretietz.retroauth;

/**
 * Created by andre on 23.03.2016.
 */
public interface TokenStorage<TOKEN_TYPE> {
    void removeToken(TOKEN_TYPE type);
    void saveToken(TOKEN_TYPE type, String token);
    String getToken(TOKEN_TYPE type);
}
