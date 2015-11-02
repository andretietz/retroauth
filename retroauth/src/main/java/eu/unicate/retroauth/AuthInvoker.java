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

import eu.unicate.retroauth.interfaces.BaseAccountManager;
import eu.unicate.retroauth.strategies.LockingStrategy;
import eu.unicate.retroauth.strategies.RequestStrategy;
import rx.Observable;
import rx.Observable.OnSubscribe;
import rx.Subscriber;
import rx.functions.Func1;
import rx.schedulers.Schedulers;

/**
 * This class invokes authenticated requests
 */
final class AuthInvoker {

	private final ServiceInfo serviceInfo;
	private final BaseAccountManager authAccountManager;
	private final RequestStrategy strategy;

	/**
	 * Creates an instance of this class
	 *
	 * @param serviceInfo        contains the information for all the methods of this class
	 * @param authAccountManager the authAccountManager to invoke some of it's methods
	 * @param strategy           request strategy you want to use
	 */
	public AuthInvoker(ServiceInfo serviceInfo, BaseAccountManager authAccountManager, RequestStrategy strategy) {
		this.serviceInfo = serviceInfo;
		this.authAccountManager = authAccountManager;
		if (strategy == null) {
			strategy = new LockingStrategy(serviceInfo.accountType, serviceInfo.tokenType, authAccountManager);
		}
		this.strategy = strategy;

	}

	/**
	 * Invokes the actual request
	 *
	 * @param request request to execute after checking for account data
	 * @param <T>     type which you expect the observable to emit
	 * @return an observable that wraps the actual request and does account handling before
	 */
	public <T> Observable<T> invoke(final Observable<T> request) {
		return strategy.execute(
				authenticate()
						.flatMap(new Func1<Object, Observable<T>>() {
							@Override
							public Observable<T> call(Object o) {
								return request;
							}
						}));
	}

	/**
	 * Authenticates a request
	 *
	 * @return an Observable that emits one boolean true after the token was added to the request
	 */
	private Observable<Boolean> authenticate() {
		return Observable.create(new OnSubscribe<Boolean>() {
			@Override
			public void call(Subscriber<? super Boolean> subscriber) {
				String name = authAccountManager.getActiveAccountName(serviceInfo.accountType, true);
				Account account = authAccountManager.getAccountByName(name, serviceInfo.accountType);
				String token = authAccountManager.getAuthToken(account, serviceInfo.accountType, serviceInfo.tokenType);
				serviceInfo.tokenSetup(token);
				subscriber.onNext(true);
				subscriber.onCompleted();
			}
		}).subscribeOn(Schedulers.computation());
	}
}
