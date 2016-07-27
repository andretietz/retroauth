package com.andretietz.retroauth;

public interface TokenTypeFactory<TOKEN_TYPE> {
    /**
     * Creates a token type object. This doe
     *
     * @param annotationValues The values from the {@link Authenticated} annotation
     * @return a token type.
     */
    TOKEN_TYPE create(int[] annotationValues);
}
