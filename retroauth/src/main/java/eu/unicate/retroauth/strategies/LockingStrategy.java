package eu.unicate.retroauth.strategies;

import android.accounts.OperationCanceledException;

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

	public LockingStrategy(String type) {
		this.semaphore = getSemaphore(type);
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
									if(error instanceof OperationCanceledException) {
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
						waitCounter.incrementAndGet();
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

	private Observable<Object> cancelIfRequired(final boolean wasWaiting) {
		return Observable.create(new Observable.OnSubscribe<Object>() {
			@Override
			public void call(Subscriber<? super Object> subscriber) {
				if(wasWaiting) {
					boolean cancel = hasBeenCanceled.get();
					if(cancel) {
						if(0 == waitCounter.decrementAndGet())
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
