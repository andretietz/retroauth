package eu.unicate.retroauth.demo;

import java.util.List;

import eu.unicate.retroauth.annotations.Authenticated;
import retrofit.http.GET;
import retrofit.http.Path;

public interface GitHubService {
	@Authenticated
	@GET("/users/{user}/repos")
	List<String> listRepos(@Path("user") String user);
}
