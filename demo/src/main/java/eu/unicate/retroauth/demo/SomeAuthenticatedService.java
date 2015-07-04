package eu.unicate.retroauth.demo;

import com.google.gson.JsonElement;

import eu.unicate.retroauth.annotations.Authenticated;
import eu.unicate.retroauth.annotations.Authentication;
import retrofit.Callback;
import retrofit.http.GET;
import retrofit.http.Path;
import rx.Observable;

@Authentication(accountType = R.string.auth_account_type, tokenType = R.string.auth_token_type)
public interface SomeAuthenticatedService {

	@Authenticated
	@GET("/users/{user}/repos")
	Observable<JsonElement> listReposRxJava(@Path("user") String user);

	@Authenticated
	@GET("/users/{user}/repos")
	JsonElement listReposBlocking(@Path("user") String user);

	@Authenticated
	@GET("/users/{user}/repos")
	void listReposAsync(@Path("user") String user, Callback<JsonElement> result);
}
