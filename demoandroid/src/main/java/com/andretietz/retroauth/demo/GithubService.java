package com.andretietz.retroauth.demo;

import com.andretietz.retroauth.Authenticated;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.GET;

public interface GithubService {

	/**
	 * The following definitions don't need to be here
	 * i just did this to keep things together
	 */

	String ACCOUNT_TYPE = "com.andretietz.retroauth.demo.ACCOUNT";
	String TOKEN_TYPE = "com.andretietz.retroauth.demo.TOKEN";

	@Authenticated({ACCOUNT_TYPE, TOKEN_TYPE})
	@GET("/user/emails")
	Call<List<Email>> getEmails();

	/**
	 * This is a simple data class which will be parsed, when the list of mails
	 * return
	 */
	class Email {
		public String email;
	}
}
