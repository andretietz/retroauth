package com.andretietz.retroauth.dummy;

import com.andretietz.retroauth.AuthenticationCanceledException;
import com.andretietz.retroauth.TokenStorage;

/**
 * Created by andre on 02.05.2016.
 */
public class DummyTokenStorage implements TokenStorage<String, String, String> {
    @Override
    public String createType(String[] annotationValues) {
        return null;
    }

    @Override
    public String getToken(String s, String s2) throws AuthenticationCanceledException {
        return null;
    }

    @Override
    public void removeToken(String s, String s2, String s3) {

    }

    @Override
    public void storeToken(String s, String s2, String s3) {

    }
}
