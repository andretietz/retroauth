package eu.unicate.retroauth.demo.auth.github;

import android.text.TextUtils;

import java.util.List;

import eu.unicate.retroauth.demo.auth.github.model.AccessToken;
import eu.unicate.retroauth.demo.auth.github.model.GithubUser;
import retrofit.RestAdapter.Builder;
import retrofit.RestAdapter.LogLevel;
import rx.Observable;

public class GithubHelper {

	private static final String AUTH_URL = "https://github.com/login/oauth/authorize?client_id=%s&redirect_uri=%s";
	private static final String TOKEN_URL = "https://github.com/login/oauth/access_token?client_id=%s&client_secret=%s&redirect_uri=%s&code=%s";
	private static final String API_URL = "https://api.github.com";
	public final String clientID;
	public final String callbackUrl;
	public final String clientSecret;
	private final String authorizationUrl;
	private final GithubAuthenticationService authService;
	private final GithubApi service;

	public GithubHelper(String clientID, String clientSecret, String callbackUrl) {
		this.clientID = clientID;
		this.clientSecret = clientSecret;
		this.callbackUrl = callbackUrl;
		authorizationUrl = String.format(AUTH_URL, clientID, callbackUrl);

		authService = new Builder().setEndpoint("https://github.com/").setLogLevel(LogLevel.FULL).build().create(GithubAuthenticationService.class);
		service = new Builder().setEndpoint("https://api.github.com/").setLogLevel(LogLevel.FULL).build().create(GithubApi.class);

	}

	public String getAuthorizationUrl(List<String> scopes) {
		return authorizationUrl + "&scope=" + TextUtils.join(",", scopes);
	}

	public Observable<AccessToken> getAccessToken(String authCode) {
		return authService.getAccessToken(clientID, clientSecret, callbackUrl, authCode);
	}

	public Observable<GithubUser> getUser(AccessToken token) {
		return service.getUser("token " + token.token);
	}
}
