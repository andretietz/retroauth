package eu.unicate.retroauth;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import eu.unicate.retroauth.strategies.LockingStrategy;
import rx.Observable;
import rx.Observable.OnSubscribe;
import rx.Subscriber;
import rx.functions.Action1;
import rx.observers.TestSubscriber;
import rx.schedulers.Schedulers;

@RunWith(JUnit4.class)
public class LockingStrategyTest {

	@Test
	public void testBlocking() throws InterruptedException {
		final LockingStrategy strategy = new LockingStrategy("test");
		int requestNum = 10;
		TestSubscriber subscriber[] = new TestSubscriber[requestNum];

		for(int i=0;i<requestNum;i++) {
			subscriber[i] = new TestSubscriber<Integer>();
			blockingWrapper(strategy, i).subscribeOn(Schedulers.io()).subscribe(subscriber[i]);
		}

		Thread.sleep(requestNum * 600);

		for(int i=0;i<requestNum;i++) {
			subscriber[i].assertValueCount(1);
			subscriber[i].assertCompleted();
		}

	}

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


	private Observable<Integer> blockingRequestSimul(final int id) {
		return Observable.create(new OnSubscribe<Integer>() {
			@Override
			public void call(Subscriber<? super Integer> subscriber) {
				try {
					subscriber.onNext(request(id));
					subscriber.onCompleted();
				} catch (InterruptedException e) {
					subscriber.onError(e);
				}
			}
		});
	}

	private Integer request(int id) throws InterruptedException {
		System.out.println("Executing Request " + id);
		return id;
	}
}
