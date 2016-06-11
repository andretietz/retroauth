package com.andretietz.retroauth.testimpl;

import com.andretietz.retroauth.AuthenticationCanceledException;
import com.andretietz.retroauth.TokenStorage;

/**
 * Created by andre on 02.05.2016.
 */
public class TestTokenStorage implements TokenStorage<String, String, String> {

    public static final String TEST_TOKEN = "token";
    private boolean canceled = false;

    @Override
    public String createType(String[] annotationValues) {
        return "tokenType";
    }

    @Override
    public String getToken(String owner, String tokenType) throws AuthenticationCanceledException {
        if(canceled) {
            throw new AuthenticationCanceledException("foo");
        }
        return TEST_TOKEN;
    }

    @Override
    public void removeToken(String s, String s2, String s3) {

    }

    @Override
    public void storeToken(String s, String s2, String s3) {

    }

    public void userCanceled(boolean error) {
        this.canceled = error;
    }
}
