package com.andretietz.retroauth;

import java.util.HashMap;

public class DefaultMethodCache<T> implements MethodCache<T> {
    private final HashMap<Integer, T> map = new HashMap<>();

    @Override
    public void register(int hash, T type) {
        map.put(hash, type);
    }

    @Override
    public T getTokenType(int hashCode) {
        return map.get(hashCode);
    }
}
