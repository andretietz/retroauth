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

package eu.unicate.retroauth.strategies;

import android.util.Log;
import android.util.SparseArray;

import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

import eu.unicate.retroauth.exceptions.AuthenticationCanceledException;
import eu.unicate.retroauth.interfaces.BaseAccountManager;
import rx.Observable;
import rx.Subscriber;
import rx.functions.Action1;
import rx.functions.Func1;
import rx.functions.Func2;


/**
 * The locking strategy makes sure only one request at a time is executed. This is important to
 * avoid multiple unauthorized requests.
 *
 * This strategy is chosen as default
 */
public class LockingStrategy extends RetryAndInvalidateStrategy {


	private static final SparseArray<AccountTokenLock> ACCOUNTTOKENLOCKS = new SparseArray<>();

	/**
	 * This object gets created ones for a specific token type of an account
	 */
	private final AccountTokenLock accountTokenLock;

	/**
	 * Creating a locking request strategy object
	 *
	 * @param accountType    Type of account you are using for your API
	 * @param tokenType      Type of Token your API is using
	 * @param cancelPending  if this is set to {@code true}, all pending requests will be canceled
	 *                       when the user cancels the login
	 * @param accountManager an AccountManager to invalidate Tokens if necessary
	 */
	public LockingStrategy(String accountType, String tokenType, boolean cancelPending, BaseAccountManager accountManager) {
		super(accountType, tokenType, accountManager);
		this.accountTokenLock = getAccountTokenLock((accountType + tokenType), cancelPending);
	}

	/**
	 * Creating a locking request strategy object
	 *
	 * {@code cancelPending} is {@code true} by default. this means, that all pending requests
	 * will be canceled, when the user cancels the login
	 *
	 * @param accountType    name of the semaphore to use
	 * @param tokenType      name of the semaphore to use
	 * @param accountManager to be able to invalidate accounts
	 */
	public LockingStrategy(String accountType, String tokenType, BaseAccountManager accountManager) {
		this(accountType, tokenType, true, accountManager);
	}

	/**
	 * @param type          Type of the LockObject. This can be any String, but should be unique for account+token
	 * @param cancelPending if this is set to {@code true}, all pending requests will be canceled
	 *                      when the user cancels the login
	 * @return an {@link AccountTokenLock} Object
	 */
	private synchronized AccountTokenLock getAccountTokenLock(String type, boolean cancelPending) {
		AccountTokenLock tokenLock = ACCOUNTTOKENLOCKS.get(type.hashCode());
		if (tokenLock == null) {
			tokenLock = new AccountTokenLock(cancelPending);
			ACCOUNTTOKENLOCKS.put(type.hashCode(), tokenLock);
		}
		return tokenLock;
	}


	@Override
	public <T> Observable<T> execute(final Observable<T> request) {
		return
				// lock the semaphore
				lockRequest()
						.concatMap(new Func1<Boolean, Observable<?>>() {
							@Override
							public Observable<?> call(Boolean wasWaiting) {
								return cancelIfRequired(wasWaiting);
							}
						})
								// execute the request
						.concatMap(new Func1<Object, Observable<T>>() {
							@Override
							public Observable<T> call(Object o) {
								return request;
							}
						})
								// release the semaphore on success
						.doOnNext(new Action1<Object>() {
							@Override
							public void call(Object o) {
								accountTokenLock.semaphore.release();
							}
						})
								// release the semaphore after the retry method if reached
						.retry(new Func2<Integer, Throwable, Boolean>() {
							@Override
							public Boolean call(Integer count, Throwable error) {
								try {
									//noinspection ThrowableResultOfMethodCallIgnored
									if (null == accountTokenLock.errorContainer.get() && error instanceof AuthenticationCanceledException) {
										accountTokenLock.errorContainer.set(error);
									}
									if (0 == accountTokenLock.waitCounter.get()) {
										accountTokenLock.errorContainer.set(null);
									}
									return retry(count, error);
								} finally {
									accountTokenLock.semaphore.release();
								}
							}
						});
	}

	/**
	 * This method is locking our semaphore to avoid other request for this tokentype
	 * to be executed until this one finishes.
	 *
	 * @return an observable that emits one boolean item that is {@code true} if the request had to wait
	 * in a queue
	 */
	private Observable<Boolean> lockRequest() {
		return Observable.create(new Observable.OnSubscribe<Boolean>() {
			@Override
			public void call(Subscriber<? super Boolean> subscriber) {
				try {
					boolean waiting = !accountTokenLock.semaphore.tryAcquire();
					if (waiting) {
						// and increment a waiting queue counter
						accountTokenLock.waitCounter.incrementAndGet();
						// wait for the next slot
						accountTokenLock.semaphore.acquire();
					}
					if (!subscriber.isUnsubscribed()) {
						// emit if this request had to wait
						subscriber.onNext(waiting);
						subscriber.onCompleted();
					}
				} catch (InterruptedException e) {
					subscriber.onError(e);
				}
			}
		});
	}

	/**
	 * Cancels the pending request if the previous try to login was canceled by the user
	 *
	 * @param wasWaiting if the request was waiting for another one
	 * @return an observable that emits {@code true} or emits an exception to cancel the request
	 */
	private Observable<Object> cancelIfRequired(final boolean wasWaiting) {
		return Observable.create(new Observable.OnSubscribe<Object>() {
			@Override
			public void call(Subscriber<? super Object> subscriber) {
				if (wasWaiting && accountTokenLock.cancelPending) {
					Throwable error = accountTokenLock.errorContainer.get();
					accountTokenLock.waitCounter.decrementAndGet();
					if (error != null) {
						subscriber.onError(error);
						return;
					}
				}
				subscriber.onNext(true);
				subscriber.onCompleted();
			}
		});
	}

	/**
	 * This immutable object contains all account+token dependent objects for a successful locking
	 * strategy
	 */
	private class AccountTokenLock {
		public final Semaphore semaphore;
		public final AtomicReference<Throwable> errorContainer;
		public final AtomicInteger waitCounter;
		public final boolean cancelPending;

		public AccountTokenLock(boolean cancelPending) {
			this.semaphore = new Semaphore(1);
			this.errorContainer = new AtomicReference<>();
			this.waitCounter = new AtomicInteger();
			this.cancelPending = cancelPending;
		}
	}
}
