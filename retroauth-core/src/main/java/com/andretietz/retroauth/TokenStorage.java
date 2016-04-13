package com.andretietz.retroauth;

/**
 * Created by andre on 23.03.2016.
 */
public interface TokenStorage<TOKEN_TYPE, TOKEN> {
    void removeToken(TOKEN_TYPE type);
    void saveToken(TOKEN_TYPE type, TOKEN token);
    TOKEN getToken(TOKEN_TYPE type);
}
