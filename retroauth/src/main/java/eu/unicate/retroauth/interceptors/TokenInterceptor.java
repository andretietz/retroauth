package eu.unicate.retroauth.interceptors;

import retrofit.RequestInterceptor;

public abstract class TokenInterceptor implements RequestInterceptor {

	public static final TokenInterceptor BEARER_TOKENINTERCEPTOR = new TokenInterceptor() {
		@Override
		public void injectToken(RequestFacade facade, String token) {
			facade.addHeader("Authorization", "Bearer " + token);
		}
	};
	private String token;
	private boolean ignore;

	public void setToken(String token) {
		this.token = token;
	}

	public void setIgnore(boolean ignore) {
		this.ignore = ignore;
	}

	@Override
	public void intercept(RequestFacade request) {
		if (!ignore) injectToken(request, token);
	}

	public abstract void injectToken(RequestFacade facade, String token);
}
