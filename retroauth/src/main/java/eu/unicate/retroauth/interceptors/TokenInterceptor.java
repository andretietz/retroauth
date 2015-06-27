package eu.unicate.retroauth.interceptors;

import retrofit.RequestInterceptor;

public abstract class TokenInterceptor implements RequestInterceptor {

	private String token;

	public void setToken(String token) {
		this.token = token;
	}

	@Override
	public void intercept(RequestFacade request) {
		injectToken(request, token);
	}

	public abstract void injectToken(RequestFacade facade, String token);
}
