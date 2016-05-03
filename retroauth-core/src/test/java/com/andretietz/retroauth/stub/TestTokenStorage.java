package com.andretietz.retroauth.stub;

import com.andretietz.retroauth.AuthenticationCanceledException;
import com.andretietz.retroauth.TokenStorage;

/**
 * Created by andre on 02.05.2016.
 */
public class TestTokenStorage implements TokenStorage<String, String, String> {

    public static final String TEST_TOKEN = "token";

    @Override
    public String createType(String[] annotationValues) {
        return null;
    }

    @Override
    public String getToken(String owner, String tokenType) throws AuthenticationCanceledException {
        return TEST_TOKEN;
    }

    @Override
    public void removeToken(String s, String s2, String s3) {

    }

    @Override
    public void storeToken(String s, String s2, String s3) {

    }
}
