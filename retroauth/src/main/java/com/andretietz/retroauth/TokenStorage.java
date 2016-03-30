package com.andretietz.retroauth;

/**
 * Created by andre on 23.03.2016.
 */
public interface TokenStorage<S, T> {
    void removeToken(S type);
    void saveToken(S type, T token);
    T getToken(S type);
}
