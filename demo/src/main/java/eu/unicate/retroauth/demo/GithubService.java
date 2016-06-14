package eu.unicate.retroauth.demo;

import java.util.List;

import eu.unicate.retroauth.annotations.Authenticated;
import eu.unicate.retroauth.annotations.Authentication;
import eu.unicate.retroauth.demo.auth.github.model.Email;
import retrofit.Callback;
import retrofit.http.GET;
import rx.Observable;

@Authentication(accountType = R.string.auth_account_type, tokenType = R.string.auth_token_type)
public interface GithubService {

	@Authenticated
	@GET("/user/emails")
	Observable<List<Email>> getEmails();

	@Authenticated
	@GET("/user/emails")
	List<Email> getEmailsBlocking();

	@Authenticated
	@GET("/user/emails")
	void getEmails(Callback<List<Email>> callback);


}
