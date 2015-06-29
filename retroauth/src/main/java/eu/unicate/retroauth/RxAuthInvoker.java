package eu.unicate.retroauth;

import android.accounts.Account;
import android.os.Handler;
import android.os.Looper;

import rx.Observable;
import rx.Observable.OnSubscribe;
import rx.Scheduler;
import rx.Subscriber;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Func1;
import rx.functions.Func2;

/**
 * This is being used when a request is authenticated and it returns an Observable.
 * I separated the code since I would like to be able to use the {@link eu.unicate.retroauth.annotations.Authenticated}
 * Annotation later on, without using necessarily rxjava
 */
public class RxAuthInvoker<T> {

	private final AuthInvoker<T> authInvoker;

	public RxAuthInvoker(AuthInvoker<T> authInvoker) {
		this.authInvoker = authInvoker;
	}


	public Observable invoke() {
		return
				getAccountName()
						.flatMap(new Func1<String, Observable<Account>>() {
							@Override
							public Observable<Account> call(String name) {
								return getAccount(name);
							}
						})
						.flatMap(new Func1<Account, Observable<String>>() {
							@Override
							public Observable<String> call(Account account) {
								return getAuthToken(account);
							}
						})
						.flatMap(new Func1<String, Observable<?>>() {
							@Override
							public Observable<?> call(String token) {
								return authenticate(token);
							}
						})
						.flatMap(new Func1<Object, Observable<?>>() {
							@Override
							public Observable<?> call(Object o) {
								return request();
							}
						})
						.retry(new Func2<Integer, Throwable, Boolean>() {
							@Override
							public Boolean call(Integer count, Throwable error) {
								return authInvoker.retry(count, error);
							}
						});
	}

	private Observable<?> request() {
		try {
			return (Observable<?>) authInvoker.request();
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	private Observable<Boolean> authenticate(final String token) {
		return Observable.create(new OnSubscribe<Boolean>() {
			@Override
			public void call(Subscriber<? super Boolean> subscriber) {
				subscriber.onNext(authInvoker.authenticate(token));
				subscriber.onCompleted();
			}
		});
	}

	private Observable<String> getAuthToken(final Account account) {
		return Observable.create(new OnSubscribe<String>() {
			@Override
			public void call(Subscriber<? super String> subscriber) {
				try {
					subscriber.onNext(authInvoker.getAuthToken(account));
				} catch (Exception e) {
					subscriber.onError(e);
				}
				subscriber.onCompleted();
			}
		});
	}


	private Observable<Account> getAccount(final String name) {
		return Observable.create(new OnSubscribe<Account>() {
			@Override
			public void call(Subscriber<? super Account> subscriber) {
				subscriber.onNext(authInvoker.getAccount(name));
				subscriber.onCompleted();
			}
		});
	}


	public Observable<String> getAccountName() {
		return Observable.create(new OnSubscribe<String>() {
			@Override
			public void call(Subscriber<? super String> subscriber) {
				subscriber.onNext(authInvoker.getAccountName());
				subscriber.onCompleted();
			}
		});
	}
}
