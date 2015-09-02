package eu.unicate.retroauth.demo.github;

import retrofit.http.GET;
import retrofit.http.Headers;
import retrofit.http.Query;
import rx.Observable;

public interface GithubAuthenticationService {
	@Headers({"Accept: application/json"})
	@GET("/login/oauth/access_token")
	Observable<AccessToken> getAccessToken(@Query("client_id") String clientId, @Query("client_secret") String clientSecret, @Query("redirect_url") String redirectUrl, @Query("code") String code);

}
