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

package eu.unicate.retroauth;

import java.lang.reflect.Method;
import java.util.Map;

import eu.unicate.retroauth.interceptors.AuthenticationRequestInterceptor;
import eu.unicate.retroauth.interceptors.TokenInterceptor;

/**
 * This is a pojo, containing all the usefull information and classes, that are
 * needed to handle the authenticated requests
 */
final class ServiceInfo {
	public final Map<Method, AuthRequestType> methodInfoCache;
	public final String accountType;
	public final String tokenType;
	public final TokenInterceptor tokenInterceptor;
	public final AuthenticationRequestInterceptor authenticationRequestInterceptor;

	public ServiceInfo(Map<Method, AuthRequestType> methodInfoCache, String accountType, String tokenType, AuthenticationRequestInterceptor baseRequestInterceptor, TokenInterceptor requestInterceptor) {
		this.methodInfoCache = methodInfoCache;
		this.accountType = accountType;
		this.tokenType = tokenType;
		this.tokenInterceptor = requestInterceptor;
		this.authenticationRequestInterceptor = baseRequestInterceptor;
	}

	public void tokenSetup(String token) {
		if(null != tokenInterceptor)
			tokenInterceptor.setToken(token);
		authenticationRequestInterceptor.setAuthenticationInterceptor(tokenInterceptor);
	}

	/**
	 * This enum defines the different request types
	 * handeled by retroauth
	 */
	public enum AuthRequestType {
		// rxjava calls
		RXJAVA,
		// asynchronous calls using the retrofit Callback interface
		ASYNC,
		// synchronous calls returning the result object
		BLOCKING,
		// non, means that the original retrofit request will be executed
		NONE
	}
}
