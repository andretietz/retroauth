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

import android.content.Context;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;

final class AuthRestHandler<T> implements InvocationHandler {


	private final ServiceInfo serviceInfo;
	private final T retrofitService;
	private final Context context;
	private final AuthInvoker<T> authInvoker;

	public AuthRestHandler(T retrofitService, Context context, ServiceInfo serviceInfo) {
		this.context = context;
		this.retrofitService = retrofitService;
		this.serviceInfo = serviceInfo;
		authInvoker = new AuthInvoker<>(context, retrofitService, serviceInfo);
	}

	@Override
	public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
		AuthRequestType methodInfo = serviceInfo.methodInfoCache.get(method);
		serviceInfo.authenticationInterceptor.setIgnore(false);
		switch (methodInfo) {
			case RXJAVA:
				return authInvoker.invokeRxJavaCall(method, args);
			case SYNC:
				return authInvoker.invokeBlockingCall(method, args);
			case ASYNC:
				authInvoker.invokeAsyncCall(method, args);
				return null;
			case NONE:
			default:
				serviceInfo.authenticationInterceptor.setIgnore(true);
				return method.invoke(retrofitService, args);
		}
	}


}
