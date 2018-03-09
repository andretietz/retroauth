package com.andretietz.retroauth.demo;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Header;

public interface GithubService {

//    @Authenticated({R.string.com_andretietz_retroauth_authentication_ACCOUNT,
//            R.string.com_andretietz_retroauth_authentication_TOKEN})
    @GET("/user")
    Call<Info> getUserInfo(@Header(""));

    class Info {
        public String login;
    }
}

