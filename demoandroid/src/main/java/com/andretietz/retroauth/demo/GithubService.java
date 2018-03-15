package com.andretietz.retroauth.demo;

import com.andretietz.retroauth.Authenticated;

import java.util.List;

import io.reactivex.Single;
import retrofit2.http.GET;

public interface GithubService {

    @GET("/user/emails")
    @Authenticated({R.string.com_andretietz_retroauth_authentication_ACCOUNT, R.string.com_andretietz_retroauth_authentication_TOKEN})
    Single<List<Email>> getEmails();


    class Email {
        public String email;
        public boolean verified;
        public boolean primary;
        public String visibility;
    }
}

