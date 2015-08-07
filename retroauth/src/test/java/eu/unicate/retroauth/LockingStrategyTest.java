package eu.unicate.retroauth;

import android.accounts.OperationCanceledException;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import java.lang.reflect.Method;
import java.util.concurrent.atomic.AtomicInteger;

import eu.unicate.retroauth.interfaces.BaseAccountManager;
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
public class LockingStrategyTest {

	private static final int REQUEST_AMOUNT = 100;

	/**
	 * Testcase:
	 * 100 simultaneously called requests are called. all of them are comparable to
	 * retroauth blocking calls (i.e. {@code @Authenticated Object someCall()})
	 * <p/>
	 * After they are started it waits 100ms, to make sure all of the are executed
	 * then checking if all of them returned with a result and completed.
	 * To make sure that there's no lock anymore, there's another request started and
	 * checked for result and completion as well
	 */
	@Test
	public void testBlockingSuccess() throws InterruptedException {
		final LockingStrategy strategy = new LockingStrategy("success-blocking");
		@SuppressWarnings("unchecked")
		TestSubscriber<Integer>[] subscriber = new TestSubscriber[REQUEST_AMOUNT];

		AtomicInteger c = new AtomicInteger(0);
		// execute 100 requests at once
		for (int i = 0; i < REQUEST_AMOUNT; i++) {
			subscriber[i] = TestSubscriber.create();
			executeAsBlocking(strategy, requestSimulationHappyCase(i, c))
					.subscribeOn(Schedulers.newThread())
					.subscribe(subscriber[i]);
		}

		// wait a bit to make sure all of them are executed
		Thread.sleep(100L);
		// test all 100 if they emit one item and complete
		for (int i = 0; i < REQUEST_AMOUNT; i++) {
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

		finalTest.assertValueCount(1);
		finalTest.assertCompleted();

	}


	/**
	 * Testcase:
	 * 100 simultaneously called requests are called. all of them are comparable to
	 * retroauth blocking calls (i.e. {@code @Authenticated Object someCall()})
	 * <p/>
	 * After they are started it waits 100ms, to make sure all of the are executed. The request
	 * itself takes 80ms as well. This is to make sure that all other 99 requests will be queued
	 * within the {@link LockingStrategy}.
	 * When the first one fails then, all queued requests are supposed to be canceled right
	 * away, since the user decided to cancel the login operation.
	 * To make sure that there's no lock in the end anymore, there's another request started and
	 * checked for result and completion as well
	 */
	@Test
	public void testBlockingFailing() throws InterruptedException {
		final LockingStrategy strategy = new LockingStrategy("failing-blocking");
		@SuppressWarnings("unchecked")
		TestSubscriber<Integer>[] subscriber = new TestSubscriber[REQUEST_AMOUNT];
		AtomicInteger c = new AtomicInteger(0);
		// execute 100 requests at once, they should be queued and the 2nd
		// waits for the 1st to finish before executing
		for (int i = 0; i < REQUEST_AMOUNT; i++) {
			subscriber[i] = TestSubscriber.create();
			executeAsBlocking(strategy, requestSimulationFailingCase(i, c))
					.subscribeOn(Schedulers.newThread())
					.subscribe(subscriber[i]);
		}
		// wait a bit to make sure all of them are executed before testing
		Thread.sleep(100L);
		// test all 100 if they have been canceled
		for (int i = 0; i < REQUEST_AMOUNT; i++) {
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

		Thread.sleep(10L);
		// if this request finished successfully we can be sure that
		// all locks have been reset
		finalTest.assertValueCount(1);
		finalTest.assertCompleted();

	}


	/**
	 * Testcase:
	 * 100 simultaneously called requests are called. all of them are comparable to
	 * retroauth rx-java calls (i.e. {@code @Authenticated Observable<Object> someCall()})
	 * <p/>
	 * After they are started it waits 100ms, to make sure all of the are executed
	 * then checking if all of them returned with a result and completed.
	 * To make sure that there's no lock anymore, there's another request started and
	 * checked for result and completion as well
	 */
	@Test
	public void testRxJavaSuccess() throws InterruptedException {
		final LockingStrategy strategy = new LockingStrategy("success-rx");
		@SuppressWarnings("unchecked")
		TestSubscriber<Integer>[] subscriber = new TestSubscriber[REQUEST_AMOUNT];

		AtomicInteger c = new AtomicInteger(0);
		// execute 100 requests at once
		for (int i = 0; i < REQUEST_AMOUNT; i++) {
			subscriber[i] = TestSubscriber.create();
			strategy.execute(requestSimulationHappyCase(i, c)).subscribe(subscriber[i]);
		}

		// wait a bit to make sure all of them are executed
		Thread.sleep(100L);
		// test all 100 if they emit one item and complete
		for (int i = 0; i < REQUEST_AMOUNT; i++) {
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

		finalTest.assertValueCount(1);
		finalTest.assertCompleted();

	}


	/**
	 * Testcase:
	 * 100 simultaneously called requests are called. all of them are comparable to
	 * retroauth blocking calls (i.e. {@code @Authenticated Observable<Object> someCall()})
	 * <p/>
	 * After they are started it waits 100ms, to make sure all of the are executed. The request
	 * itself takes 80ms as well. This is to make sure that all other 99 requests will be queued
	 * within the {@link LockingStrategy}.
	 * When the first one fails then, all queued requests are supposed to be canceled right
	 * away, since the user decided to cancel the login operation.
	 * To make sure that there's no lock in the end anymore, there's another request started and
	 * checked for result and completion as well
	 */
	@Test
	public void testRxJavaFailing() throws InterruptedException {
		final LockingStrategy strategy = new LockingStrategy("failing-rx");
		@SuppressWarnings("unchecked")
		TestSubscriber<Integer>[] subscriber = new TestSubscriber[REQUEST_AMOUNT];
		AtomicInteger c = new AtomicInteger(0);
		// execute 100 requests at once, they should be queued and the 2nd
		// waits for the 1st to finish before executing
		for (int i = 0; i < REQUEST_AMOUNT; i++) {
			subscriber[i] = TestSubscriber.create();
			strategy.execute(requestSimulationFailingCase(i, c)).subscribe(subscriber[i]);
		}
		// wait a bit to make sure all of them are executed before testing
		Thread.sleep(100L);
		// test all 100 if they have been canceled
		for (int i = 0; i < REQUEST_AMOUNT; i++) {
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

		Thread.sleep(10L);
		// if this request finished successfully we can be sure that
		// all locks have been reset
		finalTest.assertValueCount(1);
		finalTest.assertCompleted();
	}

	/**
	 * Testcase:
	 * 100 simultaneously called requests are called. all of them are comparable to
	 * retroauth rx-java or blocking calls
	 * <p/>
	 * After they are started it waits 100ms, to make sure all of the are executed
	 * then checking if all of them returned with a result and completed.
	 * To make sure that there's no lock anymore, there's another request started and
	 * checked for result and completion as well
	 */
	@Test
	public void testMixtureSuccess() throws InterruptedException {
		final LockingStrategy strategy = new LockingStrategy("success-mixture");
		@SuppressWarnings("unchecked")
		TestSubscriber<Integer>[] subscriber = new TestSubscriber[REQUEST_AMOUNT];

		AtomicInteger c = new AtomicInteger(0);
		// execute 100 requests at once
		for (int i = 0; i < REQUEST_AMOUNT; i++) {
			subscriber[i] = TestSubscriber.create();
			Observable<Integer> request;
			if (i % 2 == 0) {
				request = strategy.execute(requestSimulationHappyCase(i, c));
			} else {
				request = executeAsBlocking(strategy, requestSimulationHappyCase(i, c));
			}
			request.subscribe(subscriber[i]);
		}

		// wait a bit to make sure all of them are executed
		Thread.sleep(100L);
		// test all 100 if they emit one item and complete
		for (int i = 0; i < REQUEST_AMOUNT; i++) {
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

		finalTest.assertValueCount(1);
		finalTest.assertCompleted();

	}


	/**
	 * Testcase:
	 * 100 simultaneously called requests are called. all of them are comparable to
	 * retroauth rx-java or blocking calls
	 * <p/>
	 * After they are started it waits 100ms, to make sure all of the are executed. The request
	 * itself takes 80ms as well. This is to make sure that all other 99 requests will be queued
	 * within the {@link LockingStrategy}.
	 * When the first one fails then, all queued requests are supposed to be canceled right
	 * away, since the user decided to cancel the login operation.
	 * To make sure that there's no lock in the end anymore, there's another request started and
	 * checked for result and completion as well
	 */
	@Test
	public void testMixtureFailing() throws InterruptedException {
		final LockingStrategy strategy = new LockingStrategy("failing-mixture");
		@SuppressWarnings("unchecked")
		TestSubscriber<Integer>[] subscriber = new TestSubscriber[REQUEST_AMOUNT];
		AtomicInteger c = new AtomicInteger(0);
		// execute 100 requests at once, they should be queued and the 2nd
		// waits for the 1st to finish before executing
		for (int i = 0; i < REQUEST_AMOUNT; i++) {
			subscriber[i] = TestSubscriber.create();
			Observable<Integer> request;
			if (i % 2 == 0) {
				request = strategy.execute(requestSimulationFailingCase(i, c));
			} else {
				request = executeAsBlocking(strategy, requestSimulationFailingCase(i, c));
			}
			request.subscribe(subscriber[i]);
		}
		// wait a bit to make sure all of them are executed before testing
		Thread.sleep(100L);
		// test all 100 if they have been canceled
		for (int i = 0; i < REQUEST_AMOUNT; i++) {
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

		Thread.sleep(10L);
		// if this request finished successfully we can be sure that
		// all locks have been reset
		finalTest.assertValueCount(1);
		finalTest.assertCompleted();
	}


	private Observable<Integer> executeAsBlocking(final LockingStrategy strategy, final Observable<Integer> request) {
		return Observable.create(new OnSubscribe<Integer>() {
			@Override
			public void call(Subscriber<? super Integer> subscriber) {
				try {
					subscriber.onNext(strategy.execute(request).toBlocking().single());
					subscriber.onCompleted();
				} catch (Throwable e) {
					subscriber.onError(e);
				}
			}
		});
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
				} catch (Throwable e) {
					subscriber.onError(e);
				}
			}
		});
	}

	/**
	 * Wrapping the requestHappy into an observable
	 * (this is how it's gonna work in retroauth as well)
	 */
	private Observable<Integer> requestSimulationFailingCase(final int id, final AtomicInteger c) {
		return Observable.create(new OnSubscribe<Integer>() {
			@Override
			public void call(Subscriber<? super Integer> subscriber) {
				try {
					subscriber.onNext(requestFailure(id, c));
					subscriber.onCompleted();
				} catch (Throwable e) {
					subscriber.onError(e);
				}
			}
		});
	}

	/**
	 * Emulated request
	 */
	private int requestHappy(int id, AtomicInteger executionCounter) throws InterruptedException {
		executionCounter.incrementAndGet();
		return id;
	}

	private int requestFailure(int id, AtomicInteger executionCounter) throws BaseAccountManager.UserCancelException, InterruptedException {
		executionCounter.incrementAndGet();
		// intentionally wait for the other requests to be queued.
		Thread.sleep(80L);
		throw new BaseAccountManager.UserCancelException(new OperationCanceledException());
	}
}
