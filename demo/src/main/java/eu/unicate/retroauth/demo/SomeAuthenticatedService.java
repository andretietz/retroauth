package eu.unicate.retroauth.demo;

import com.google.gson.JsonElement;

import java.util.List;

import eu.unicate.retroauth.annotations.Authenticated;
import eu.unicate.retroauth.annotations.Authentication;
import retrofit.http.GET;
import retrofit.http.Path;
import rx.Observable;

@Authentication(accountType = R.string.auth_account_type, tokenType = R.string.auth_token_type)
public interface SomeAuthenticatedService {
	@Authenticated
	@GET("/users/{user}/repos")
	Observable<List<JsonElement>> listRepos(@Path("user") String user);

}
