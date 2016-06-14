package com.andretietz.retroauth;


import org.simpleframework.xml.Default;

@Default
class XMLAuthToken {
    public String token;
    public String refreshToken;

    public XMLAuthToken() {}

    XMLAuthToken(String token, String refreshToken) {
        this.token = token;
        this.refreshToken = refreshToken;
    }
}