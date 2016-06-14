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

import android.accounts.Account;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.lang.reflect.Method;
import java.util.HashMap;

import eu.unicate.retroauth.interceptors.AuthenticationRequestInterceptor;
import eu.unicate.retroauth.interceptors.TokenInterceptor;
import eu.unicate.retroauth.interfaces.BaseAccountManager;
import eu.unicate.retroauth.strategies.LockingStrategy;
import eu.unicate.retroauth.strategies.RequestStrategy;
import rx.Observable;
import rx.functions.Func0;
import rx.observers.TestSubscriber;

import static org.mockito.Matchers.anyBoolean;
import static org.mockito.Matchers.anyObject;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class AuthInvokerTests {

	@Mock
	BaseAccountManager authAccountManager;

	@Mock
	TestService service;

	private AuthInvoker invoker;

	@Before
	public void setupTest() {
		HashMap<Method, ServiceInfo.AuthRequestType> map = new HashMap<>();
		ServiceInfo info = new ServiceInfo(map, "testAccountType", "testTokenType", new AuthenticationRequestInterceptor(null), TokenInterceptor.BEARER_TOKENINTERCEPTOR);
		RequestStrategy strategy = new LockingStrategy(info.accountType, info.tokenType, authAccountManager) {
			@Override
			protected boolean retry(int count, Throwable error) {
				return count <= 1 && "unauthorized".equals(error.getMessage());
			}
		};
		invoker = new AuthInvoker(info, authAccountManager, strategy);

	}

	/**
	 * This test covers the happy case.
	 * <p/>
	 * request gets a usual response
	 */
	@Test
	public void invokeHappyCaseTest() throws Exception {
		TestSubscriber<String> testSubscriber = new TestSubscriber<>();
		// the following methods are tested already by the AuthAccountManagerTests
		when(authAccountManager.getActiveAccountName(anyString(), anyBoolean())).thenReturn("testAccountName");
		when(authAccountManager.getAccountByName(anyString(), anyString())).thenReturn(new Account("testAccountName", "testAccountType"));
		when(authAccountManager.getAuthToken((Account) anyObject(), anyString(), anyString())).thenReturn("auth-token");
		when(service.request()).thenReturn(Observable.just("mocked-result"));

		invoker.invoke(service.request()).subscribe(testSubscriber);

		testSubscriber.awaitTerminalEvent();
		testSubscriber.assertValue("mocked-result");
		testSubscriber.assertCompleted();
	}


	/**
	 * This tests covers the case that if there is an unauthorized error
	 * it retries the whole authorization stuff
	 * The second try returns with a usual response
	 * <p/>
	 * request gets an unauthorized exception, retries and gets a usual response
	 */
	@Test
	public void invokeFailingRequestTest() throws Exception {
		TestSubscriber<String> testSubscriber = new TestSubscriber<>();
		// the following methods are tested already by the AuthAccountManagerTests
		when(authAccountManager.getActiveAccountName(anyString(), anyBoolean())).thenReturn("testAccountName").thenReturn("testAccountName");
		when(authAccountManager.getAccountByName(anyString(), anyString())).thenReturn(new Account("testAccountName", "testAccountType")).thenReturn(new Account("testAccountName", "testAccountType"));
		when(authAccountManager.getAuthToken((Account) anyObject(), anyString(), anyString())).thenReturn("auth-token").thenReturn("auth-token");
		when(service.request())
				.thenReturn(Observable.<String>error(new Exception("unauthorized")))
				.thenReturn(Observable.just("mocked-result"));

		invoker.invoke(request()).subscribe(testSubscriber);
		testSubscriber.awaitTerminalEvent();
		testSubscriber.assertValue("mocked-result");
		testSubscriber.assertCompleted();
	}

	/**
	 * This test covers the case that, if there are more than one unauthorized exceptions, that
	 * it does not try to retry a second time
	 * <p/>
	 * request gets an unauthorized exception twice in a row
	 */
	@Test
	public void invokeFailingRetryTest() throws Exception {
		TestSubscriber<String> testSubscriber = new TestSubscriber<>();
		// the following methods are tested already by the AuthAccountManagerTests
		when(authAccountManager.getActiveAccountName(anyString(), anyBoolean())).thenReturn("testAccountName").thenReturn("testAccountName");
		when(authAccountManager.getAccountByName(anyString(), anyString())).thenReturn(new Account("testAccountName", "testAccountType")).thenReturn(new Account("testAccountName", "testAccountType"));
		when(authAccountManager.getAuthToken((Account) anyObject(), anyString(), anyString())).thenReturn("auth-token").thenReturn("auth-token");
		when(service.request())
				.thenReturn(Observable.<String>error(new Exception("unauthorized")))
				.thenReturn(Observable.<String>error(new Exception("unauthorized")));

		invoker.invoke(request()).subscribe(testSubscriber);

		testSubscriber.awaitTerminalEvent();
		testSubscriber.assertNoValues();
		testSubscriber.assertError(Exception.class);
		testSubscriber.assertNotCompleted();
	}

	/**
	 * This test covers the case that, if some other exception than unauthorized appears, it does
	 * not try to retry a second time
	 * <p/>
	 * request gets any exception
	 */
	@Test
	public void invokeFailingTest() throws Exception {
		TestSubscriber<String> testSubscriber = new TestSubscriber<>();
		// the following methods are tested already by the AuthAccountManagerTests
		when(authAccountManager.getActiveAccountName(anyString(), anyBoolean())).thenReturn("testAccountName");
		when(authAccountManager.getAccountByName(anyString(), anyString())).thenReturn(new Account("testAccountName", "testAccountType"));
		when(authAccountManager.getAuthToken((Account) anyObject(), anyString(), anyString())).thenReturn("auth-token");
		when(service.request())
				.thenReturn(Observable.<String>error(new Exception("some other exception")));

		invoker.invoke(request()).subscribe(testSubscriber);

		testSubscriber.awaitTerminalEvent();
		testSubscriber.assertNoValues();
		testSubscriber.assertError(Exception.class);
		testSubscriber.assertNotCompleted();
	}

	private Observable<String> request() {
		return Observable.defer(new Func0<Observable<String>>() {
			@Override
			public Observable<String> call() {
				return service.request();
			}
		});
	}

	public interface TestService {
		Observable<String> request();
	}
}
