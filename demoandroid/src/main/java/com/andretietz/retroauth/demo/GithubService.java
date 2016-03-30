package com.andretietz.retroauth.demo;

import com.andretietz.retroauth.Authenticated;
import com.andretietz.retroauth.TokenInjector;

import java.util.List;

import okhttp3.Request;
import retrofit2.Call;
import retrofit2.http.GET;

public interface GithubService {

    @Authenticated({ACCOUNT_TYPE, TOKEN_TYPE})
    @GET("/user/emails")
    Call<List<Email>> getEmails();

    /**
     * The following definitions don't need to be here
     * i just did this to keep things together
     */

    String ACCOUNT_TYPE = "com.andretietz.retroauth.demo.ACCOUNT";
    String TOKEN_TYPE = "com.andretietz.retroauth.demo.TOKEN";


    TokenInjector INJECTOR = new GithubTokenInjector();

    /**
     * This injector injects the token to the request, when fired
     * so that this request will be authenticated
     */
    class GithubTokenInjector implements TokenInjector {
        @Override
        public Request inject(Request originalRequest, String token) {
            return originalRequest.newBuilder()
                  .header("Authorization", "token " + token)
                  .build();
        }
    }

    /**
     * This is a simple data class which will be parsed, when the list of mails
     * return
     */
    class Email {
        public String email;
    }
}
