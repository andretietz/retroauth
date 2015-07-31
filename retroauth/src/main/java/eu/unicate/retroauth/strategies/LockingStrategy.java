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

import android.accounts.OperationCanceledException;
import android.util.Log;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Semaphore;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

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
public class LockingStrategy extends BasicRetryStrategy {

	private static final Map<String, Semaphore> TOKEN_TYPE_SEMAPHORES = new HashMap<>();
	private static final AtomicInteger waitCounter = new AtomicInteger(0);
	private static final AtomicBoolean hasBeenCanceled = new AtomicBoolean(false);

	private final Semaphore semaphore;
	private final boolean cancelPending;

	/**
	 * Creating a locking request strategy object
	 *
	 * @param type          name of the semaphore to use
	 * @param cancelPending if this is set to {@code true}, all pending requests will be canceled
	 *                      when the user cancels the login
	 */
	public LockingStrategy(String type, boolean cancelPending) {
		this.semaphore = getSemaphore(type);
		this.cancelPending = cancelPending;
	}

	/**
	 * Creating a locking request strategy object
	 * <p/>
	 * {@code cancelPending} is {@code true} by default. this means, that all pending requests
	 * will be canceled, when the user cancels the login
	 *
	 * @param type name of the semaphore to use
	 */
	public LockingStrategy(String type) {
		this(type, true);
	}

	/**
	 * Returns a semaphore which is unique per type
	 *
	 * @param type Type you want the semaphore for
	 * @return a semaphore
	 */
	private Semaphore getSemaphore(String type) {
		synchronized (TOKEN_TYPE_SEMAPHORES) {
			Semaphore semaphore = TOKEN_TYPE_SEMAPHORES.get(type);
			if (semaphore == null) {
				semaphore = new Semaphore(1);
				TOKEN_TYPE_SEMAPHORES.put(type, semaphore);
			}
			return semaphore;
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
								semaphore.release();
							}
						})
								// release the semaphore after the retry method if reached
						.retry(new Func2<Integer, Throwable, Boolean>() {
							@Override
							public Boolean call(Integer count, Throwable error) {
								try {
									if (error instanceof OperationCanceledException) {
										hasBeenCanceled.set(true);
									}
									return retry(count, error);
								} finally {
									semaphore.release();
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
					boolean waiting = !semaphore.tryAcquire();
					if (waiting) {
						// and increment a waiting queue counter
						int w = waitCounter.incrementAndGet();
						Log.e("LOCK", "waiting: " + w);
						// wait for the next slot
						semaphore.acquire();
					}
					// emit if this request had to wait
					subscriber.onNext(waiting);
					subscriber.onCompleted();
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
				if (wasWaiting && cancelPending) {
					boolean cancel = hasBeenCanceled.get();
					int stillWaiting = waitCounter.decrementAndGet();
					if (cancel) {
						Log.e("LOCK", "still waiting: " + stillWaiting);
						if (0 == stillWaiting)
							hasBeenCanceled.set(false);
						subscriber.onError(new IllegalStateException("The Request has been canceled"));
						return;
					}
				}
				subscriber.onNext(true);
				subscriber.onCompleted();
			}
		});
	}
}
