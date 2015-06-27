package eu.unicate.retroauth;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.accounts.AccountManagerFuture;
import android.app.Activity;
import android.content.Context;
import android.os.Bundle;

import java.lang.reflect.Method;

import retrofit.RetrofitError;
import rx.Observable;
import rx.Observable.OnSubscribe;
import rx.Subscriber;
import rx.functions.Func1;
import rx.functions.Func2;
import rx.schedulers.Schedulers;

public class RxAuthInvoker {

	private static final int HTTP_UNAUTHORIZED = 401;

	public static Observable invoke(final Object service, final Context context, final ServiceInfo serviceInfo, final Method method, final Object[] args) {
		return
				getAuthToken(context, serviceInfo.accountType, serviceInfo.tokenType)
						.flatMap(new Func1<String, Observable<Object>>() {
							@Override
							public Observable<Object> call(String token) {
								return authenticationSetup(token);
							}
						})
						.flatMap(new Func1<Object, Observable<?>>() {
							@Override
							public Observable<?> call(Object o) {
								return request(service, method, args);
							}
						})
						.retry(new Func2<Integer, Throwable, Boolean>() {
							@Override
							public Boolean call(Integer integer, Throwable error) {
								if (error instanceof RetrofitError) {
									int status = ((RetrofitError) error).getResponse().getStatus();
									if (HTTP_UNAUTHORIZED == status) {
										AccountManager accountManager = AccountManager.get(context);
										Account[] accounts = accountManager.getAccountsByType(serviceInfo.accountType);
										if (accounts.length == 1) {
											String authToken = accountManager.peekAuthToken(accounts[0], serviceInfo.tokenType);
											accountManager.invalidateAuthToken(accounts[0].type, authToken);
										} else {
											// TODO
											throw new RuntimeException("Not implemented for more than one account");
										}

										return true;
									}
								}
								return false;
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

	private static Observable<String> getAuthToken(final Context context, final String accountType, final String tokenType) {
		return Observable.create(new OnSubscribe<String>() {
			@Override
			public void call(Subscriber<? super String> subscriber) {
				AccountManager accountManager = AccountManager.get(context);
				Account[] accounts = accountManager.getAccountsByType(accountType);
				AccountManagerFuture<Bundle> future;
				Activity activity = (context instanceof Activity)?(Activity)context:null;
				if (accounts.length == 0) {
					future = accountManager.addAccount(accountType, tokenType, null, null, activity, null, null);
				} else if (accounts.length == 1) {
					future = accountManager.getAuthToken(accounts[0], tokenType, null, activity, null, null);
				} else {
					// TODO
					throw new RuntimeException("Not implemented for more than one account");
				}
				try {
					Bundle result = future.getResult();
					subscriber.onNext(result.getString(AccountManager.KEY_AUTHTOKEN));
				} catch (Exception e) {
					subscriber.onError(e);
				}
				subscriber.onCompleted();

			}
		}).subscribeOn(Schedulers.computation());
	}

	private static Observable<Object> authenticationSetup(String token) {
		return Observable.create(new OnSubscribe<Object>() {
			@Override
			public void call(Subscriber<? super Object> subscriber) {

			}
		});
	}
}
