/*
 * Copyright (c) 2015 Andre Tietz
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package eu.unicate.retroauth.interceptors;

import retrofit.RequestInterceptor;

/**
 * This RequestInterceptor is used to be able to add userspecific headers and
 * the token headers for the authenticated requests
 */
public final class AuthenticationRequestInterceptor implements RequestInterceptor {

	/**
	 * The user specific request interceptor
	 */
	private final RequestInterceptor adapterInterceptor;

	/**
	 * The TokenInterceptor
	 */
	private TokenInterceptor authenticationInterceptor;

	/**
	 * Creates a the request interceptor
	 */
	public AuthenticationRequestInterceptor(RequestInterceptor requestInterceptor) {
		this.adapterInterceptor = requestInterceptor;
	}

	/**
	 * @param tokenInterceptor the {@link TokenInterceptor} to add the token to the Request header
	 */
	public void setAuthenticationInterceptor(TokenInterceptor tokenInterceptor) {
		this.authenticationInterceptor = tokenInterceptor;
	}

	/**
	 * TODO: link source
	 * Calls the RequestInterceptor#intercept(RequestFacade) on both
	 * Interceptors
	 *
	 * @param facade the request facade from retrofit
	 */
	@Override
	public void intercept(RequestFacade facade) {
		if (null != adapterInterceptor)
			adapterInterceptor.intercept(facade);
		if (null != authenticationInterceptor)
			authenticationInterceptor.intercept(facade);
	}
}
