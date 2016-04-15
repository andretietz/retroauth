package com.andretietz.retroauth;

import java.util.HashMap;

/**
 * Created by andre on 22.03.2016.
 */
public interface MethodCache<S> {

    void register(int uniqueIdentifier, S type);

    S getTokenType(int uniqueIdentifier);

    class DefaultMethodCache<S> implements MethodCache<S> {
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
}
