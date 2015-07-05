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

import android.content.Context;

import retrofit.RequestInterceptor;

/**
 * An instance of an implemented version has to be given to {@link eu.unicate.retroauth.AuthRestAdapter#create(Context, TokenInterceptor, Class)}
 * This instance will be used to inject the Token from the {@link android.accounts.AccountManager}
 */
public abstract class TokenInterceptor implements RequestInterceptor {

	/**
	 * Predefined Tokeninterceptor for the <a href="http://oauth2.thephpleague.com/token-types/">Bearer Token used in OAuth 2.0</a>
	 */
	@SuppressWarnings("unused")
	public static final TokenInterceptor BEARER_TOKENINTERCEPTOR = new TokenInterceptor() {
		@Override
		public void injectToken(RequestFacade facade, String token) {
			facade.addHeader("Authorization", "Bearer " + token);
		}
	};

	/**
	 * The token gained from the {@link android.accounts.AccountManager}
	 */
	private String token;

	/**
	 * Still pretty ugly, this is to define if this interceptor is being used or not
	 * in case of an unauthorized call this would be true, otherwise false
	 */
	private boolean ignore;

	/**
	 * Sets the Token to use
	 * @param token Token you want to use
	 */
	public void setToken(String token) {
		this.token = token;
	}

	/**
	 * Sets if the TokenInterceptor will be used within the next call or not
	 * @param ignore <code>false</code> if you don't want to ignore this TokenInterceptor
	 */
	public void setIgnore(boolean ignore) {
		this.ignore = ignore;
	}

	@Override
	public void intercept(RequestFacade request) {
		if (!ignore) injectToken(request, token);
	}

	/**
	 * Sets up the Token into the request
	 * @param facade The facade to manipulate the request
	 * @param token the Token to set up
	 */
	public abstract void injectToken(RequestFacade facade, String token);
}
