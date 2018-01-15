package com.andretietz.retroauth.testimpl;

import com.andretietz.retroauth.TokenTypeFactory;

/**
 * Created by andre on 27/07/16.
 */

public class TestTokenTypeFactory implements TokenTypeFactory<String> {
    @Override
    public String create(int[] annotationValues) {
        return annotationValues.length > 0 ? "tokenType" : null;
    }
}
