package com.andretietz.retroauth;

import android.util.SparseArray;

/**
 * Created by andre.tietz on 30/03/16.
 */
public final class AndroidMethodCache implements MethodCache<AndroidTokenType> {

    private final SparseArray<AndroidTokenType> cache = new SparseArray<>();

    @Override
    public void register(int uniqueIdentifier, AndroidTokenType type) {
        cache.append(uniqueIdentifier, type);
    }

    @Override
    public AndroidTokenType getTokenType(int uniqueIdentifier) {
        return cache.get(uniqueIdentifier);
    }
}
