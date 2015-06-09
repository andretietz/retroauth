package eu.unicate.retroauth;

import java.lang.reflect.Method;

import rx.Observable;
import rx.Observable.OnSubscribe;
import rx.Subscriber;
import rx.functions.Func1;
import rx.functions.Func2;

public class RxAuthInvoker {

	public static Observable invoke(final Object service, final AuthenticationHandler authHandler, final Method method, final Object[] args) {
		return doBeforeRequest(authHandler)
				.flatMap(new Func1<Integer, Observable<?>>() {
					@Override
					public Observable<?> call(Integer integer) {
						return request(service, method, args);
					}
				})
				.retry(new Func2<Integer, Throwable, Boolean>() {
					@Override
					public Boolean call(Integer integer, Throwable throwable) {
						return authHandler.retry(integer, throwable);
					}
				});
	}


	private static Observable<Integer> doBeforeRequest(final AuthenticationHandler authHandler) {
		return Observable.create(new OnSubscribe<Integer>() {
			@Override
			public void call(Subscriber<? super Integer> subscriber) {
				authHandler.checkForAccount();
				subscriber.onNext(0);
				subscriber.onCompleted();
			}
		});
	}


	/**
	 * just wrapping the original request and wrap any exceptions if thrown
	 *
	 * @param method methods that is going to be executed
	 * @param args   arguments to that method
	 * @return the original retrofit observable
	 */
	private static Observable<?> request(Object service, Method method, Object[] args) {
		try {
			return (Observable<?>) method.invoke(service, args);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}
}
