package com.andretietz.retroauth;

/**
 * Created by andre on 15/04/16.
 */
public class AndroidToken {

    public String token;
    public String refreshToken;

    public AndroidToken(String token, String refreshToken) {
        this.token = token;
        this.refreshToken = refreshToken;
    }

}
