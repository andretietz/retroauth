package eu.unicate.retroauth.demo.auth.github;

import eu.unicate.retroauth.demo.auth.LoginActivity;
import eu.unicate.retroauth.demo.auth.github.model.AccessToken;
import retrofit.http.GET;
import retrofit.http.Headers;
import retrofit.http.Query;
import rx.Observable;

/**
 * This api defines a method to get the AccessToken. It is used in the {@link LoginActivity}
 */
public interface GithubAuthenticationService {
	@Headers({"Accept: application/json"})
	@GET("/login/oauth/access_token")
	Observable<AccessToken> getAccessToken(@Query("client_id") String clientId, @Query("client_secret") String clientSecret, @Query("redirect_url") String redirectUrl, @Query("code") String code);

}
