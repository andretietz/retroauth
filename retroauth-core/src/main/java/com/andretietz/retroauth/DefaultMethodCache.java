package com.andretietz.retroauth;

import java.util.HashMap;

public final class DefaultMethodCache<S> implements MethodCache<S> {
    private final HashMap<Integer, S> map = new HashMap<>();

    @Override
    public void register(int hash, S type) {
        map.put(hash, type);
    }

    @Override
    public S getTokenType(int hashCode) {
        return map.get(hashCode);
    }
}
