package eu.unicate.retroauth.demo.github;

import retrofit.http.GET;
import retrofit.http.Header;
import rx.Observable;

public interface GithubApi {
	@GET("/user")
	Observable<GithubUser> getUser(@Header("Authorization")String token);
}
