package eu.unicate.retroauth;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.accounts.AccountManagerFuture;
import android.accounts.AuthenticatorException;
import android.accounts.OperationCanceledException;
import android.app.Activity;
import android.os.Bundle;
import android.util.Log;

import java.io.IOException;
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

	public static Observable invoke(final Object service, final Activity activity, final ServiceInfo serviceInfo, final Method method, final Object[] args) {
		return
				getAccount(activity, serviceInfo.accountType, serviceInfo.tokenType).flatMap(new Func1<Account, Observable<?>>() {
					@Override
					public Observable<?> call(Account account) {
						return getAuthToken(account, activity, serviceInfo.tokenType);
					}
				})

						.flatMap(new Func1<Object, Observable<?>>() {
							@Override
							public Observable<?> call(Object account) {
								return request(service, method, args);
							}
						})
						.retry(new Func2<Integer, Throwable, Boolean>() {
							@Override
							public Boolean call(Integer integer, Throwable error) {
								if (error instanceof RetrofitError) {
									int status = ((RetrofitError) error).getResponse().getStatus();
									if (HTTP_UNAUTHORIZED == status) {
										AccountManager.get(activity);
										// TODO: some re-authentication work
										return true;
									}
								}
								return false;
							}
						});
	}


	private static Observable<String> getAuthToken(final Account account, final Activity activity, final String tokenType) {
		return Observable.create(new OnSubscribe<String>() {
			@Override
			public void call(Subscriber<? super String> subscriber) {
				AccountManager accountManager = AccountManager.get(activity);
				try {
					subscriber.onNext(accountManager.blockingGetAuthToken(account, tokenType, true));
					subscriber.onCompleted();
				} catch (OperationCanceledException | IOException | AuthenticatorException e) {
					subscriber.onError(e);
				}
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

	private static Observable<Account> getAccount(final Activity activity, final String accountType, final String tokenType) {
		return Observable.create(new OnSubscribe<Account>() {
			@Override
			public void call(Subscriber<? super Account> subscriber) {
				AccountManager accountManager = AccountManager.get(activity);
				Account[] accounts = accountManager.getAccountsByType(accountType);
				if (accounts.length == 0) {
					AccountManagerFuture<Bundle> future = accountManager.addAccount(accountType, tokenType, null, null, activity, null, null);
					try {
						Bundle result = future.getResult();
						Log.e("", result.toString());
					} catch (OperationCanceledException e) {
						e.printStackTrace();
					} catch (IOException e) {
						e.printStackTrace();
					} catch (AuthenticatorException e) {
						e.printStackTrace();
					}
				} else if (accounts.length == 1) {
					subscriber.onNext(accounts[0]);
					subscriber.onCompleted();
				} else {
					// TODO: choose from multiple accounts
				}

			}
		}).subscribeOn(Schedulers.computation());
	}
}
