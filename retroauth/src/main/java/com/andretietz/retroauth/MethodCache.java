package com.andretietz.retroauth;

/**
 * Created by andre on 22.03.2016.
 */
public interface MethodCache<T> {
    void register(int uniqueIdentifier, T type);
    T getTokenType(int uniqueIdentifier);
}
