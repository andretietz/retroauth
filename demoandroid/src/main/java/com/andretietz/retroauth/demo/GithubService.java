package com.andretietz.retroauth.demo;

import io.reactivex.Single;
import retrofit2.http.GET;
import retrofit2.http.Header;

public interface GithubService {

//    @Authenticated({R.string.com_andretietz_retroauth_authentication_ACCOUNT,
//            R.string.com_andretietz_retroauth_authentication_TOKEN})
    @GET("/user")
    Single<Info> getUserInfo(@Header("Bearer") String token);

    class Info {
        public String login;
    }
}

