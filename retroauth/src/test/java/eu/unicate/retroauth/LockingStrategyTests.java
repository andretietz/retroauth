package eu.unicate.retroauth;

import android.accounts.OperationCanceledException;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import java.lang.reflect.Method;
import java.util.concurrent.atomic.AtomicInteger;

import eu.unicate.retroauth.exceptions.AuthenticationCanceledException;
import eu.unicate.retroauth.strategies.LockingStrategy;
import rx.Observable;
import rx.Observable.OnSubscribe;
import rx.Subscriber;
import rx.observers.TestSubscriber;
import rx.schedulers.Schedulers;

/**
 * Testing the {@link LockingStrategy}
 * With rx java methods and blocking methods.
 * <p/>
 * Since methods using retrofit.Callback are handled the same as
 * rx java methods (see {@link AuthRestHandler#asyncRequest(Method, Object[])})
 * I see no point in testing the as well
 */
@RunWith(JUnit4.class)
public class LockingStrategyTests {

	/**
	 * Time in ms, that a failing request is taking
	 * Since we want to make sure that this requests are queued waiting for the first one
	 * this should be a bigger value. not too big since we want to have quick tests
	 */
	public static final long FAILING_REQUEST_TIME = 100L;
	/**
	 * Time in ms, a successful request is taking
	 * (keep it short, since there are {@link #REQUEST_AMOUNT} of requests executed.)
	 */
	public static final long SUCCESSFUL_REQUEST_TIME = 0L;
	/**
	 * Amount of requests executed
	 */
	private static final int REQUEST_AMOUNT = 100;

	private LockingStrategy strategy;

	@Before
	public void setup() {
		strategy = new LockingStrategy("testAccountType", "testTokenType", null) {
			@Override
			protected boolean retry(int count, Throwable error) {
				return count <= 1 && "unauthorized".equals(error.getMessage());
			}
		};
	}

	/**
	 * Testcase:
	 * {@link #REQUEST_AMOUNT} simultaneously called requests are called. all of them are comparable to
	 * retroauth blocking calls (i.e. {@code @Authenticated Object someCall()})
	 * <p/>
	 * After they are started its checking if all of them returned with a result and completed.
	 * To make sure that there's no lock anymore, there's another request started and
	 * checked for result and completion as well
	 */
	@Test
	public void testBlockingSuccess() throws InterruptedException {
		@SuppressWarnings("unchecked") final
		TestSubscriber<Integer>[] subscriber = new TestSubscriber[REQUEST_AMOUNT];
		final AtomicInteger c = new AtomicInteger(0);
		// execute all requests at once
		for (int i = 0; i < REQUEST_AMOUNT; i++) {
			subscriber[i] = TestSubscriber.create();
			final int finalI = i;
			Observable.create(new OnSubscribe<Integer>() {
				@Override
				public void call(Subscriber<? super Integer> subscriber) {
					subscriber.onNext(blockingCall(strategy, requestSimulationHappyCase(finalI, c)));
					subscriber.onCompleted();
				}
			}).subscribeOn(Schedulers.newThread())
					.subscribe(subscriber[i]);
		}

		// test all requests if they emit one item and complete
		for (int i = 0; i < REQUEST_AMOUNT; i++) {
			subscriber[i].awaitTerminalEvent();
			subscriber[i].assertValueCount(1);
			subscriber[i].assertCompleted();
		}
		Assert.assertEquals(REQUEST_AMOUNT, c.get());
		// nothing should be locked anymore
		TestSubscriber<Object> finalTest = TestSubscriber.create();
		// if anything is still locked, this test fails
		strategy.execute(Observable.create(new OnSubscribe<Object>() {
			@Override
			public void call(Subscriber<? super Object> subscriber) {
				subscriber.onNext(null);
				subscriber.onCompleted();

			}
		})).subscribe(finalTest);

		finalTest.awaitTerminalEvent();
		finalTest.assertValueCount(1);
		finalTest.assertCompleted();

	}


	/**
	 * Testcase:
	 * {@link #REQUEST_AMOUNT} simultaneously called requests are called. all of them are comparable to
	 * retroauth blocking calls (i.e. {@code @Authenticated Object someCall()})
	 * <p/>
	 * The request itself takes {@link #FAILING_REQUEST_TIME}ms as well. This is to make sure
	 * that all following requests will be queued within the {@link LockingStrategy}.
	 * When the first one fails then, all queued requests are supposed to be canceled right
	 * away, since the user decided to cancel the login operation.
	 * To make sure that there's no lock in the end anymore, there's another request started and
	 * checked for result and completion as well
	 */
	@Test
	public void testBlockingFailing() throws InterruptedException {
		@SuppressWarnings("unchecked")
		TestSubscriber<Integer>[] subscriber = new TestSubscriber[REQUEST_AMOUNT];
		final AtomicInteger c = new AtomicInteger(0);
		// execute all requests at once, they should be queued and the 2nd
		// waits for the 1st to finish before executing
		for (int i = 0; i < REQUEST_AMOUNT; i++) {
			subscriber[i] = TestSubscriber.create();
			Observable.create(new OnSubscribe<Integer>() {
				@Override
				public void call(Subscriber<? super Integer> subscriber) {
					subscriber.onNext(blockingCall(strategy, requestSimulationFailingCase(c)));
					subscriber.onCompleted();
				}
			}).subscribeOn(Schedulers.newThread())
					.subscribe(subscriber[i]);
		}
		// test all requests if they have been canceled
		for (int i = 0; i < REQUEST_AMOUNT; i++) {
			subscriber[i].awaitTerminalEvent();
			subscriber[i].assertError(RuntimeException.class);
		}

		// make sure only the one request has been executed (the otherones should be canceled before
		// getting executed IF, they were waiting. This is why there is a sleep in the requestFailure
		// method)
		Assert.assertEquals(1, c.get());

		// to make sure all locks are released again, do another request
		TestSubscriber<Object> finalTest = TestSubscriber.create();
		// if anything is still locked, this test fails
		strategy.execute(Observable.create(new OnSubscribe<Object>() {
			@Override
			public void call(Subscriber<? super Object> subscriber) {
				subscriber.onNext(null);
				subscriber.onCompleted();

			}
		})).subscribe(finalTest);

		// if this request finished successfully we can be sure that
		// all locks have been reset
		finalTest.awaitTerminalEvent();
		finalTest.assertValueCount(1);
		finalTest.assertCompleted();

	}


	/**
	 * Testcase:
	 * {@link #REQUEST_AMOUNT} simultaneously called requests are called. all of them are comparable to
	 * retroauth rx-java calls (i.e. {@code @Authenticated Observable<Object> someCall()})
	 * <p/>
	 * After they are started its checking if all of them returned with a result and completed.
	 * To make sure that there's no lock anymore, there's another request started and
	 * checked for result and completion as well
	 */
	@Test
	public void testRxJavaSuccess() throws InterruptedException {
		@SuppressWarnings("unchecked")
		TestSubscriber<Integer>[] subscriber = new TestSubscriber[REQUEST_AMOUNT];

		AtomicInteger c = new AtomicInteger(0);
		// execute all requests at once
		for (int i = 0; i < REQUEST_AMOUNT; i++) {
			subscriber[i] = TestSubscriber.create();
			rxjavaCall(strategy, requestSimulationHappyCase(i, c)).subscribe(subscriber[i]);
		}

		// test all requests if they emit one item and complete
		for (int i = 0; i < REQUEST_AMOUNT; i++) {
			subscriber[i].awaitTerminalEvent();
			subscriber[i].assertValueCount(1);
			subscriber[i].assertCompleted();
		}
		Assert.assertEquals(REQUEST_AMOUNT, c.get());
		// nothing should be locked anymore
		TestSubscriber<Object> finalTest = TestSubscriber.create();
		// if anything is still locked, this test fails
		strategy.execute(Observable.create(new OnSubscribe<Object>() {
			@Override
			public void call(Subscriber<? super Object> subscriber) {
				subscriber.onNext(null);
				subscriber.onCompleted();

			}
		})).subscribe(finalTest);
		finalTest.awaitTerminalEvent();
		finalTest.assertValueCount(1);
		finalTest.assertCompleted();

	}


	/**
	 * Testcase:
	 * {@link #REQUEST_AMOUNT} simultaneously called requests are called. all of them are comparable to
	 * retroauth blocking calls (i.e. {@code @Authenticated Observable<Object> someCall()})
	 * <p/>
	 * The request itself takes {@link #FAILING_REQUEST_TIME}ms as well. This is to make sure
	 * that all following requests will be queued
	 * within the {@link LockingStrategy}.
	 * When the first one fails then, all queued requests are supposed to be canceled right
	 * away, since the user decided to cancel the login operation.
	 * To make sure that there's no lock in the end anymore, there's another request started and
	 * checked for result and completion as well
	 */
	@Test
	public void testRxJavaFailing() throws InterruptedException {
		@SuppressWarnings("unchecked")
		TestSubscriber<Integer>[] subscriber = new TestSubscriber[REQUEST_AMOUNT];
		AtomicInteger c = new AtomicInteger(0);
		// execute all requests at once, they should be queued and the 2nd
		// waits for the 1st to finish before executing
		for (int i = 0; i < REQUEST_AMOUNT; i++) {
			subscriber[i] = TestSubscriber.create();
			rxjavaCall(strategy, requestSimulationFailingCase(c)).subscribe(subscriber[i]);
		}
		// test all requests if they have been canceled
		for (int i = 0; i < REQUEST_AMOUNT; i++) {
			subscriber[i].awaitTerminalEvent();
			subscriber[i].assertError(AuthenticationCanceledException.class);
		}

		// make sure only the one request has been executed (the otherones should be canceled before
		// getting executed IF, they were waiting. This is why there is a sleep in the requestFailure
		// method)
		Assert.assertEquals(1, c.get());

		// to make sure all locks are released again, do another request
		TestSubscriber<Object> finalTest = TestSubscriber.create();
		// if anything is still locked, this test fails
		strategy.execute(Observable.create(new OnSubscribe<Object>() {
			@Override
			public void call(Subscriber<? super Object> subscriber) {
				subscriber.onNext(null);
				subscriber.onCompleted();

			}
		})).subscribe(finalTest);

		// if this request finished successfully we can be sure that
		// all locks have been reset
		finalTest.awaitTerminalEvent();
		finalTest.assertValueCount(1);
		finalTest.assertCompleted();
	}

	/**
	 * Testcase:
	 * {@link #REQUEST_AMOUNT} simultaneously called requests are called. all of them are comparable to
	 * retroauth rx-java or blocking calls
	 * <p/>
	 * After they are started its checking if all of them returned with a result and completed.
	 * To make sure that there's no lock anymore, there's another request started and
	 * checked for result and completion as well
	 */
	@Test
	public void testMixtureSuccess() throws InterruptedException {
		@SuppressWarnings("unchecked")
		TestSubscriber<Integer>[] subscriber = new TestSubscriber[REQUEST_AMOUNT];

		final AtomicInteger c = new AtomicInteger(0);
		// execute all requests at once
		for (int i = 0; i < REQUEST_AMOUNT; i++) {
			subscriber[i] = TestSubscriber.create();
			Observable<Integer> request;
			if (i % 2 == 0) {
				request = rxjavaCall(strategy, requestSimulationHappyCase(i, c));
			} else {
				final int finalI = i;
				request = Observable.create(new OnSubscribe<Integer>() {
					@Override
					public void call(Subscriber<? super Integer> subscriber) {
						subscriber.onNext(blockingCall(strategy, requestSimulationHappyCase(finalI, c)));
						subscriber.onCompleted();
					}
				}).subscribeOn(Schedulers.newThread());
			}
			request.subscribe(subscriber[i]);
		}

		// test all requests if they emit one item and complete
		for (int i = 0; i < REQUEST_AMOUNT; i++) {
			subscriber[i].awaitTerminalEvent();
			subscriber[i].assertValueCount(1);
			subscriber[i].assertCompleted();
		}
		Assert.assertEquals(REQUEST_AMOUNT, c.get());
		// nothing should be locked anymore
		TestSubscriber<Object> finalTest = TestSubscriber.create();
		// if anything is still locked, this test fails
		strategy.execute(Observable.create(new OnSubscribe<Object>() {
			@Override
			public void call(Subscriber<? super Object> subscriber) {
				subscriber.onNext(null);
				subscriber.onCompleted();

			}
		})).subscribe(finalTest);

		finalTest.awaitTerminalEvent();
		finalTest.assertValueCount(1);
		finalTest.assertCompleted();

	}


	/**
	 * Testcase:
	 * {@link #REQUEST_AMOUNT} simultaneously called requests are called. all of them are comparable to
	 * retroauth rx-java or blocking calls
	 * <p/>
	 * The request
	 * itself takes {@link #FAILING_REQUEST_TIME}ms as well. This is to make sure that all following requests will be queued
	 * within the {@link LockingStrategy}.
	 * When the first one fails then, all queued requests are supposed to be canceled right
	 * away, since the user decided to cancel the login operation.
	 * To make sure that there's no lock in the end anymore, there's another request started and
	 * checked for result and completion as well
	 */
	@Test
	public void testMixtureFailing() throws InterruptedException {
		@SuppressWarnings("unchecked")
		TestSubscriber<Integer>[] subscriber = new TestSubscriber[REQUEST_AMOUNT];
		final AtomicInteger c = new AtomicInteger(0);
		// execute all requests at once, they should be queued and the 2nd
		// waits for the 1st to finish before executing
		for (int i = 0; i < REQUEST_AMOUNT; i++) {
			subscriber[i] = TestSubscriber.create();
			Observable<Integer> request;
			if (i % 2 == 0) {
				request = rxjavaCall(strategy, requestSimulationFailingCase(c));
			} else {
				request = Observable.create(new OnSubscribe<Integer>() {
					@Override
					public void call(Subscriber<? super Integer> subscriber) {
						subscriber.onNext(blockingCall(strategy, requestSimulationFailingCase(c)));
						subscriber.onCompleted();
					}
				}).subscribeOn(Schedulers.newThread());
			}
			request.subscribe(subscriber[i]);
		}
		// test all requests if they have been canceled
		for (int i = 0; i < REQUEST_AMOUNT; i++) {
			subscriber[i].awaitTerminalEvent();
			if (i % 2 == 0) {
				subscriber[i].assertError(AuthenticationCanceledException.class);
			} else {
				subscriber[i].assertError(RuntimeException.class);
			}
		}

		// make sure only the one request has been executed (the otherones should be canceled before
		// getting executed IF, they were waiting. This is why there is a sleep in the requestFailure
		// method)
		Assert.assertEquals(1, c.get());

		// to make sure all locks are released, do another request
		TestSubscriber<Object> finalTest = TestSubscriber.create();
		// if anything is still locked, this test fails
		strategy.execute(Observable.create(new OnSubscribe<Object>() {
			@Override
			public void call(Subscriber<? super Object> subscriber) {
				subscriber.onNext(null);
				subscriber.onCompleted();

			}
		})).subscribe(finalTest);

		// if this request finished successfully we can be sure that
		// all locks have been reset
		finalTest.awaitTerminalEvent();
		finalTest.assertValueCount(1);
		finalTest.assertCompleted();
	}

	/**
	 * Wrapping the requestHappy into an observable
	 * (this is how it's gonna work in retroauth as well)
	 */
	private Observable<Integer> requestSimulationHappyCase(final int id, final AtomicInteger c) {
		return Observable.create(new OnSubscribe<Integer>() {
			@Override
			public void call(Subscriber<? super Integer> subscriber) {
				try {
					subscriber.onNext(requestHappy(id, c));
					subscriber.onCompleted();
				} catch (InterruptedException e) {
					Assert.fail();
				}
			}
		});
	}

	/**
	 * Wrapping the requestHappy into an observable
	 * (this is how it's gonna work in retroauth as well)
	 */
	private Observable<Integer> requestSimulationFailingCase(final AtomicInteger c) {
		return Observable.create(new OnSubscribe<Integer>() {
			@Override
			public void call(Subscriber<? super Integer> subscriber) {
				try {
					subscriber.onNext(requestFailure(c));
					subscriber.onCompleted();
				} catch (InterruptedException e) {
					Assert.fail();
				}
			}
		});
	}

	/**
	 * This is how rxjava calls are executed in the {@link AuthRestHandler#invoke(Object, Method, Object[])} method
	 */
	public <T> Observable<T> rxjavaCall(LockingStrategy strategy, Observable<T> request) {
		return strategy
				.execute(request)
				.subscribeOn(Schedulers.newThread());
	}

	public <T> T blockingCall(LockingStrategy strategy, Observable<T> request) {
		return strategy
				.execute(request)
				.toBlocking().single();
	}

	/**
	 * Emulated request
	 */
	private int requestHappy(int id, AtomicInteger executionCounter) throws InterruptedException {
		executionCounter.incrementAndGet();
		Thread.sleep(SUCCESSFUL_REQUEST_TIME);
		return id;
	}

	private int requestFailure(AtomicInteger executionCounter) throws InterruptedException {
		executionCounter.incrementAndGet();
		// intentionally wait for the other requests to be queued.
		// This is required for the failing tests since they assume that all requests are pending
		Thread.sleep(FAILING_REQUEST_TIME);
		throw new AuthenticationCanceledException(new OperationCanceledException());
	}
}
