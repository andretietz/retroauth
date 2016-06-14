package com.andretietz.retroauth.testimpl;

import com.andretietz.retroauth.AuthenticationCanceledException;
import com.andretietz.retroauth.TokenStorage;

/**
 * Created by andre on 02.05.2016.
 */
public class TestTokenStorage implements TokenStorage<String, String, String> {

    public static final String TEST_TOKEN = "token";
    private TestBehaviour behaviour;

    @Override
    public String createType(String[] annotationValues) {
        return annotationValues.length > 0 ? "tokenType" : null;
    }

    @Override
    public String getToken(String owner, String tokenType) throws AuthenticationCanceledException {
        if (behaviour != null) {
            return behaviour.getToken(owner, tokenType);
        }
        return TEST_TOKEN;
    }

    @Override
    public void removeToken(String s, String s2, String s3) {

    }

    @Override
    public void storeToken(String s, String s2, String s3) {

    }

    public void setTestBehaviour(TestBehaviour behaviour) {
        this.behaviour = behaviour;
    }

    public interface TestBehaviour {
        String getToken(String owner, String tokenType) throws AuthenticationCanceledException;
    }
}
