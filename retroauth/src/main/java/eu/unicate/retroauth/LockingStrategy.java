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

import java.util.HashMap;
import java.util.concurrent.Semaphore;
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
 * <p/>
 * This strategy is chosen as default
 */
public class LockingStrategy extends RetryAndInvalidateStrategy {

	private static final AtomicReference<HashMap<String, PerTypeObject>> PER_TYPE = new AtomicReference<>(new HashMap<String, PerTypeObject>());

	// TODO
	private final PerTypeObject perTypeObject;

	/**
	 * Creating a locking request strategy object
	 *
	 * @param serviceInfo    service infos
	 * @param cancelPending  if this is set to {@code true}, all pending requests will be canceled
	 *                       when the user cancels the login
	 * @param accountManager to be able to invalidate accounts
	 */
	public LockingStrategy(ServiceInfo serviceInfo, boolean cancelPending, BaseAccountManager accountManager) {
		super(serviceInfo, accountManager);
		this.perTypeObject = getPerTypeObject(serviceInfo.tokenType, cancelPending);
	}

	/**
	 * Creating a locking request strategy object
	 * <p/>
	 * {@code cancelPending} is {@code true} by default. this means, that all pending requests
	 * will be canceled, when the user cancels the login
	 *
	 * @param serviceInfo    name of the semaphore to use
	 * @param accountManager to be able to invalidate accounts
	 */
	public LockingStrategy(ServiceInfo serviceInfo, BaseAccountManager accountManager) {
		this(serviceInfo, true, accountManager);
	}

	/**
	 * TODO
	 */
	private PerTypeObject getPerTypeObject(String type, boolean cancelPending) {
		synchronized (PER_TYPE) {
			PerTypeObject perType = PER_TYPE.get().get(type);
			if (perType == null) {
				perType = new PerTypeObject(cancelPending);
				PER_TYPE.get().put(type, perType);
			}
			return perType;
		}
	}


	@Override
	public <T> Observable<T> execute(final Observable<T> request) {
		return
				// lock the semaphore
				lockRequest()
						.flatMap(new Func1<Boolean, Observable<?>>() {
							@Override
							public Observable<?> call(Boolean wasWaiting) {
								return cancelIfRequired(wasWaiting);
							}
						})
								// execute the request
						.flatMap(new Func1<Object, Observable<T>>() {
							@Override
							public Observable<T> call(Object o) {
								return request;
							}
						})
								// release the semaphore on success
						.doOnNext(new Action1<Object>() {
							@Override
							public void call(Object o) {
								perTypeObject.semaphore.release();
							}
						})
								// release the semaphore after the retry method if reached
						.retry(new Func2<Integer, Throwable, Boolean>() {
							@Override
							public Boolean call(Integer count, Throwable error) {
								try {
									//noinspection ThrowableResultOfMethodCallIgnored
									if (null == perTypeObject.errorContainer.get() && error instanceof AuthenticationCanceledException) {
										System.out.println("set error!");
										perTypeObject.errorContainer.set(error);
									}
									if (0 == perTypeObject.waitCounter.get()) {
										System.out.println("reset error!");
										perTypeObject.errorContainer.set(null);
									}
									return retry(count, error);
								} finally {
									perTypeObject.semaphore.release();
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
					boolean waiting = !perTypeObject.semaphore.tryAcquire();
					if (waiting) {
						// and increment a waiting queue counter
						perTypeObject.waitCounter.incrementAndGet();
						// wait for the next slot
						perTypeObject.semaphore.acquire();
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
				if (wasWaiting && perTypeObject.cancelPending) {
					Throwable error = perTypeObject.errorContainer.get();
					perTypeObject.waitCounter.decrementAndGet();
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
	 * TODO
	 */
	private class PerTypeObject {
		public final Semaphore semaphore;
		public final AtomicReference<Throwable> errorContainer;
		public final AtomicInteger waitCounter;
		public final boolean cancelPending;

		public PerTypeObject(boolean cancelPending) {
			this.semaphore = new Semaphore(1);
			this.errorContainer = new AtomicReference<>();
			this.waitCounter = new AtomicInteger();
			this.cancelPending = cancelPending;
		}
	}
}
