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

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import eu.unicate.retroauth.annotations.Authenticated;
import eu.unicate.retroauth.annotations.Authentication;
import eu.unicate.retroauth.interceptors.AuthenticationRequestInterceptor;
import eu.unicate.retroauth.interceptors.TokenInterceptor;
import retrofit.Callback;
import retrofit.RequestInterceptor;
import rx.Observable;

import static org.mockito.Matchers.anyInt;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class AuthRestAdapterTests {

	@Mock
	public Context context;

	/**
	 * Since the Builder itself only wraps the retrofit builder except using the
	 * setRequestInterceptor Method, this is the only builder method to be tested
	 */
	@Test
	public void builderTest() throws NoSuchFieldException, IllegalAccessException {
		Field authRequestInterceptor = AuthRestAdapter.class.getDeclaredField("interceptor");
		authRequestInterceptor.setAccessible(true);
		Field requestInterceptor = AuthenticationRequestInterceptor.class.getDeclaredField("adapterInterceptor");
		requestInterceptor.setAccessible(true);

		AuthRestAdapter.Builder builder = new AuthRestAdapter.Builder();
		builder.setEndpoint("http://localhost");
		builder.setRequestInterceptor(new RequestInterceptor() {
			@Override
			public void intercept(RequestFacade request) {
			}
		});
		AuthRestAdapter build = builder.build();
		AuthenticationRequestInterceptor authInterceptor = (AuthenticationRequestInterceptor) authRequestInterceptor.get(build);
		Assert.assertNotNull(authInterceptor);
		RequestInterceptor interceptor = (RequestInterceptor) requestInterceptor.get(authInterceptor);
		Assert.assertNotNull(interceptor);
	}

	@Test
	public void testServiceWithoutAnyAnnotationsTest() throws NoSuchMethodException, InvocationTargetException, IllegalAccessException {
		when(context.getString(anyInt())).thenReturn("testAccountType").thenReturn("testTokenType");
		AuthRestAdapter.Builder builder = new AuthRestAdapter.Builder();
		builder.setEndpoint("http://localhost");
		AuthRestAdapter authRestAdapter = builder.build();
		Method method = getServiceInfoMethod();
		// calling getService info with the context, the service and some TokenInterceptor
		ServiceInfo serviceInfo = (ServiceInfo) method.invoke(authRestAdapter, context, new AuthenticationRequestInterceptor(null), NoAnnotationService.class, TokenInterceptor.BEARER_TOKENINTERCEPTOR);
		Assert.assertNotNull(serviceInfo);
	}


	@Test
	public void testServiceWithMissingAnnotationsTest() throws NoSuchMethodException, IllegalAccessException {
		when(context.getString(anyInt())).thenReturn("testAccountType").thenReturn("testTokenType");
		AuthRestAdapter.Builder builder = new AuthRestAdapter.Builder();
		builder.setEndpoint("http://localhost");
		AuthRestAdapter authRestAdapter = builder.build();
		Method method = getServiceInfoMethod();
		try {
			// calling getService info with the context, the service and some TokenInterceptor
			method.invoke(authRestAdapter, context, new AuthenticationRequestInterceptor(null), MissingAnnotationService.class, TokenInterceptor.BEARER_TOKENINTERCEPTOR);
		} catch (InvocationTargetException e) {
			Assert.assertEquals("MissingAnnotationService.authenticated: The Method authenticated contains the Authenticated " +
							"Annotation, but the interface does not implement the Authentication Annotation",
					e.getCause().getMessage());
		}

	}

	@Test
	public void testServiceWithAnnotationsTest() throws NoSuchMethodException, IllegalAccessException, InvocationTargetException {
		when(context.getString(anyInt())).thenReturn("testAccountType").thenReturn("testTokenType");
		AuthRestAdapter.Builder builder = new AuthRestAdapter.Builder();
		builder.setEndpoint("http://localhost");
		AuthRestAdapter authRestAdapter = builder.build();
		Method getServiceInfoMethod = getServiceInfoMethod();
		ServiceInfo serviceInfo = (ServiceInfo) getServiceInfoMethod.invoke(authRestAdapter, context, new AuthenticationRequestInterceptor(null), AnnotationService.class, TokenInterceptor.BEARER_TOKENINTERCEPTOR);


		Assert.assertNotNull(serviceInfo);
		Assert.assertEquals("testAccountType", serviceInfo.accountType);
		Assert.assertEquals("testTokenType", serviceInfo.tokenType);
		Assert.assertEquals(TokenInterceptor.BEARER_TOKENINTERCEPTOR, serviceInfo.tokenInterceptor);
		Assert.assertNotNull(serviceInfo.methodInfoCache);

		for (Method method : serviceInfo.methodInfoCache.keySet()) {
			if ("unAuthenticated".equals(method.getName())) {
				Assert.assertEquals(ServiceInfo.AuthRequestType.NONE, serviceInfo.methodInfoCache.get(method));
			} else if ("authenticatedAsync".equals(method.getName())) {
				Assert.assertEquals(ServiceInfo.AuthRequestType.ASYNC, serviceInfo.methodInfoCache.get(method));
			} else if ("authenticatedRx".equals(method.getName())) {
				Assert.assertEquals(ServiceInfo.AuthRequestType.RXJAVA, serviceInfo.methodInfoCache.get(method));
			} else if ("authenticatedBlocking".equals(method.getName())) {
				Assert.assertEquals(ServiceInfo.AuthRequestType.BLOCKING, serviceInfo.methodInfoCache.get(method));
			} else {
				Assert.fail();
			}
		}
	}

	interface NoAnnotationService {
		void someMethodWithoutAuthentication();
	}

	// @Authentication is missing
	interface MissingAnnotationService {
		@Authenticated
		void authenticated();
	}

	@SuppressWarnings("ResourceType")
	@Authentication(accountType = 0x71, tokenType = 0x72)
	interface AnnotationService {

		Observable<Object> unAuthenticated();

		@Authenticated
		void authenticatedAsync(Callback<Object> cb);

		@Authenticated
		Observable<Object> authenticatedRx();

		@Authenticated
		Object authenticatedBlocking();
	}


	private Method getServiceInfoMethod() throws NoSuchMethodException {
		Method method = AuthRestAdapter.class.getDeclaredMethod("getServiceInfo", Context.class, AuthenticationRequestInterceptor.class, Class.class, TokenInterceptor.class);
		method.setAccessible(true);
		return method;
	}

}
