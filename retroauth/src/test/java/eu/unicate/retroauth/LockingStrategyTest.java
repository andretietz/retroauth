package eu.unicate.retroauth;

import android.accounts.OperationCanceledException;

import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import java.util.concurrent.atomic.AtomicInteger;

import eu.unicate.retroauth.interfaces.BaseAccountManager;
import eu.unicate.retroauth.strategies.LockingStrategy;
import rx.Observable;
import rx.Observable.OnSubscribe;
import rx.Subscriber;
import rx.observers.TestSubscriber;
import rx.schedulers.Schedulers;

@RunWith(JUnit4.class)
public class LockingStrategyTest {

	private static final int REQUEST_AMOUNT = 100;

	/**
	 * Testing the happy case of 100 simultaneously called requests
	 */
	@Ignore
	@Test
	public void testBlockingSuccess() throws InterruptedException {
		final LockingStrategy strategy = new LockingStrategy("test");
		@SuppressWarnings("unchecked")
		TestSubscriber<Integer>[] subscriber = new TestSubscriber[REQUEST_AMOUNT];

		AtomicInteger c = new AtomicInteger(0);
		// execute 100 requests at once
		for (int i = 0; i < REQUEST_AMOUNT; i++) {
			subscriber[i] = TestSubscriber.create();
			blockingWrapper(strategy, blockingRequestSimul(i, c))
					.subscribeOn(Schedulers.newThread())
					.subscribe(subscriber[i]);
		}

		// wait a bit to make sure all of them are executed
		Thread.sleep(100);
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
	 * Testing the failing case of 100 simultaneously called requests
	 */
	@Test
	public void testBlockingFailing() throws InterruptedException {
		final LockingStrategy strategy = new LockingStrategy("test");
		@SuppressWarnings("unchecked")
		TestSubscriber<Integer>[] subscriber = new TestSubscriber[REQUEST_AMOUNT];
		AtomicInteger c = new AtomicInteger(0);
		// execute 100 requests at once
		for (int i = 0; i < REQUEST_AMOUNT; i++) {
			subscriber[i] = TestSubscriber.create();
			blockingWrapper(strategy, blockingFailingRequestSimul(i, c))
					.subscribeOn(Schedulers.newThread())
					.subscribe(subscriber[i]);
		}
		// wait a bit to make sure all of them are executed
		Thread.sleep(100L);
		// test all 100 if they emit one item and complete
		for (int i = 0; i < REQUEST_AMOUNT; i++) {
			subscriber[i].assertError(RuntimeException.class);
		}

		Assert.assertEquals(1, c.get());
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
	 * execute the requestHappy as blocking
	 * using the LockingStrategy
	 */
	private Observable<Integer> blockingWrapper(final LockingStrategy strategy, final Observable<Integer> request) {
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
	private Observable<Integer> blockingRequestSimul(final int id, final AtomicInteger c) {
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
	private Observable<Integer> blockingFailingRequestSimul(final int id, final AtomicInteger c) {
		return Observable.create(new OnSubscribe<Integer>() {
			@Override
			public void call(Subscriber<? super Integer> subscriber) {
				try {
					subscriber.onNext(requestFailure(id, c));
					subscriber.onCompleted();
				} catch (BaseAccountManager.UserCancelException e) {
					subscriber.onError(e);
				}
			}
		});
	}

	/**
	 * Emulated request
	 */
	private int requestHappy(int id, AtomicInteger executionCounter) {
//		System.out.println("Executing emulated request " + id);
		executionCounter.incrementAndGet();
		return id;
	}

	private int requestFailure(int id, AtomicInteger executionCounter) throws BaseAccountManager.UserCancelException {
//		System.out.println("Executing emulated request " + id);
		executionCounter.incrementAndGet();
		throw new BaseAccountManager.UserCancelException(new OperationCanceledException());
	}
}
