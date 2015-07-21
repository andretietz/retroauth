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

import eu.unicate.retroauth.interfaces.MockableAccountManager;
import eu.unicate.retroauth.interfaces.RetryRule;
import rx.Observable;
import rx.Observable.OnSubscribe;
import rx.Subscriber;
import rx.functions.Func1;
import rx.functions.Func2;

/**
 * This class invokes authenticated requests
 */
final class AuthInvoker {

	private final ServiceInfo serviceInfo;
	private final MockableAccountManager authAccountManager;
	private final RetryRule retryRule;

	/**
	 * Creates an instance of this class
	 *
	 * @param serviceInfo        contains the information for all the methods of this class
	 * @param authAccountManager the authAccountManager to invoke some of it's methods
	 * @param retryRule          a rule interface for retrying on request failure
	 */
	public AuthInvoker(ServiceInfo serviceInfo, MockableAccountManager authAccountManager, RetryRule retryRule) {
		this.serviceInfo = serviceInfo;
		this.authAccountManager = authAccountManager;
		this.retryRule = retryRule;
	}

	/**
	 * Invokes the actual request
	 *
	 * @param request request to execute after checking for account data
	 * @param <T>     type which you expect the observable to emit
	 * @return an observable that wraps the actual request and does account handling before
	 */
	public <T> Observable<T> invoke(final Observable<T> request) {
		return getAccountName()
				.flatMap(new Func1<String, Observable<Account>>() {
					@Override
					public Observable<Account> call(String name) {
						return getAccount(name);
					}
				})
				.flatMap(new Func1<Account, Observable<String>>() {
					@Override
					public Observable<String> call(Account account) {
						return getAuthToken(account);
					}
				})
				.flatMap(new Func1<String, Observable<?>>() {
					@Override
					public Observable<?> call(String token) {
						return authenticate(token);
					}
				})
				.flatMap(new Func1<Object, Observable<T>>() {
					@Override
					public Observable<T> call(Object o) {
						return request;
					}
				})
				.retry(new Func2<Integer, Throwable, Boolean>() {
					@Override
					public Boolean call(Integer count, Throwable error) {
						return retryRule.retry(count, error);
					}
				});
	}

	/**
	 * Authenticates a request
	 *
	 * @param token Token to authenticate with
	 * @return an Observable that emits one boolean true after the token was added to the request
	 */
	private Observable<Boolean> authenticate(final String token) {
		return Observable.create(new OnSubscribe<Boolean>() {
			@Override
			public void call(Subscriber<? super Boolean> subscriber) {
				serviceInfo.authenticationInterceptor.setToken(token);
				subscriber.onNext(true);
				subscriber.onCompleted();
			}
		});
	}

	/**
	 * gets the token from the given account
	 *
	 * @param account you want the token from
	 * @return Observable that emits the token as String if successful
	 */
	private Observable<String> getAuthToken(final Account account) {
		return Observable.create(new OnSubscribe<String>() {
			@Override
			public void call(Subscriber<? super String> subscriber) {
				try {
					subscriber.onNext(authAccountManager.getAuthToken(account, serviceInfo.accountType, serviceInfo.tokenType));
					subscriber.onCompleted();
				} catch (Exception e) {
					subscriber.onError(e);
				}
			}
		});
	}


	/**
	 * Gets the account by the given name
	 *
	 * @param name Name of the account you're searching for
	 * @return An Observable that emits the account if it could be found
	 */
	private Observable<Account> getAccount(final String name) {
		return Observable.create(new OnSubscribe<Account>() {
			@Override
			public void call(Subscriber<? super Account> subscriber) {
				subscriber.onNext(authAccountManager.getAccountByName(name, serviceInfo.accountType));
				subscriber.onCompleted();
			}
		});
	}

	/**
	 * Gets the name of the currently active account
	 *
	 * @return an Observable that emits the accountName as String if available
	 */
	private Observable<String> getAccountName() {
		return Observable.create(new OnSubscribe<String>() {
			@Override
			public void call(Subscriber<? super String> subscriber) {
				subscriber.onNext(authAccountManager.getActiveAccountName(serviceInfo.accountType, true));
				subscriber.onCompleted();
			}
		});
	}


}
