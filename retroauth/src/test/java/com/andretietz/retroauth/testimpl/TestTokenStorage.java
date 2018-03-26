package com.andretietz.retroauth.testimpl;

import com.andretietz.retroauth.TokenStorage;

public class TestTokenStorage implements TokenStorage<String, String, String> {

    public static final String TEST_TOKEN = "token";
    private TestBehaviour behaviour;

    @Override
    public String getToken(String owner, String tokenType) {
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
        String getToken(String owner, String tokenType);
    }
}
