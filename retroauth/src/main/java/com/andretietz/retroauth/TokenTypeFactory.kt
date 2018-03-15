package com.andretietz.retroauth

interface TokenTypeFactory<out TOKEN_TYPE> {
    /**
     * Creates a token type object. This doe
     *
     * @param annotationValues The values from the [Authenticated] annotation
     * @return a token type.
     */
    fun create(annotationValues: IntArray): TOKEN_TYPE
}
