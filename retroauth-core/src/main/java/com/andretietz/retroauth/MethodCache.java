package com.andretietz.retroauth;

/**
 * Created by andre on 22.03.2016.
 */
public interface MethodCache<S> {
    void register(int uniqueIdentifier, S type);
    S getTokenType(int uniqueIdentifier);
}
