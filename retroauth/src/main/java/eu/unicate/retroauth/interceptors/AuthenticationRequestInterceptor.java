package eu.unicate.retroauth.interceptors;

import retrofit.RequestInterceptor;

/**
 * This RequestInterceptor is used to be able to add userspecific headers and
 * the token headers for the authenticated requests
 */
public final class AuthenticationRequestInterceptor implements RequestInterceptor {

	private final RequestInterceptor adapterInterceptor;
	private TokenInterceptor authenticationInterceptor;

	/**
	 * Creates a Composite request interceptor
	 */
	public AuthenticationRequestInterceptor(RequestInterceptor requestInterceptor) {
		this.adapterInterceptor = requestInterceptor;
	}

	public void setAuthenticationInterceptor(TokenInterceptor tokenInterceptor) {
		this.authenticationInterceptor = tokenInterceptor;
	}

	/**
	 * iterates through all request interceptors and intercepts
	 *
	 * @param facade the request facade
	 */
	@Override
	public void intercept(RequestFacade facade) {
		if (null != adapterInterceptor)
			adapterInterceptor.intercept(facade);
		if (null != authenticationInterceptor)
			authenticationInterceptor.intercept(facade);
	}
}
