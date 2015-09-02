package eu.unicate.retroauth.demo.auth.github;

import eu.unicate.retroauth.demo.auth.LoginActivity;
import eu.unicate.retroauth.demo.auth.github.model.GithubUser;
import retrofit.http.GET;
import retrofit.http.Header;
import rx.Observable;

/**
 * This API provides a method to get the current authorized {@link GithubUser}. It is only used in the
 * {@link LoginActivity}, meaning that there's no account stored on the
 * device yet. That's why we have to add the authorization in the request as parameter.
 */
public interface GithubApi {
	@GET("/user")
	Observable<GithubUser> getUser(@Header("Authorization")String token);
}
