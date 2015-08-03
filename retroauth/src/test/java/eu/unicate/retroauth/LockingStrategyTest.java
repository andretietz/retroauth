package eu.unicate.retroauth;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

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
	@Test
	public void testBlocking() throws InterruptedException {
		final LockingStrategy strategy = new LockingStrategy("test");
		@SuppressWarnings("unchecked")
		TestSubscriber<Integer>[] subscriber = new TestSubscriber[REQUEST_AMOUNT];

		// execute 100 requests at once
		for (int i = 0; i < REQUEST_AMOUNT; i++) {
			subscriber[i] = TestSubscriber.create();
			blockingWrapper(strategy, i)
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
	private Observable<Integer> blockingWrapper(final LockingStrategy strategy, final int id) {
		return Observable.create(new OnSubscribe<Integer>() {
			@Override
			public void call(Subscriber<? super Integer> subscriber) {
				try {
					subscriber.onNext(strategy.execute(blockingRequestSimul(id)).toBlocking().single());
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
	private Observable<Integer> blockingRequestSimul(final int id) {
		return Observable.create(new OnSubscribe<Integer>() {
			@Override
			public void call(Subscriber<? super Integer> subscriber) {
				try {
					subscriber.onNext(requestHappy(id));
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
	private Integer requestHappy(int id) {
		System.out.println("Executing emulated request " + id);
		return id;
	}
}
