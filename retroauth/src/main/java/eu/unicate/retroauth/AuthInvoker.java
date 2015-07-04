package eu.unicate.retroauth;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.accounts.AccountManagerFuture;
import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.support.v4.util.Pair;

import java.lang.reflect.Method;

import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;
import rx.Observable;
import rx.Observable.OnSubscribe;
import rx.Subscriber;
import rx.functions.Action1;
import rx.functions.Func1;
import rx.functions.Func2;
import rx.schedulers.Schedulers;

/**
 * This is being used when a request is authenticated and it returns an Observable.
 * I separated the code since I would like to be able to use the {@link eu.unicate.retroauth.annotations.Authenticated}
 * Annotation later on, without using necessarily rxjava
 */
public class AuthInvoker<T> {

	private static final int HTTP_UNAUTHORIZED = 401;


	private final Context context;
	private final T retrofitService;
	private final ServiceInfo serviceInfo;
	private final AccountHelper accountHelper;

	public AuthInvoker(Context context, T retrofitService, ServiceInfo serviceInfo) {
		this.context = context;
		this.retrofitService = retrofitService;
		this.serviceInfo = serviceInfo;
		this.accountHelper = AccountHelper.get(context);
	}


	public Observable invokeRxJavaCall(final Method method, final Object[] args) {
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
								return getAuthToken(account, AccountManager.get(context));
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
								return request(method, args);
							}
						})
						.retry(new Func2<Integer, Throwable, Boolean>() {
							@Override
							public Boolean call(Integer count, Throwable error) {
								return retry(count, error);
							}
						});
	}

	public Object invokeBlockingCall(final Method method, final Object[] args) {
		return getAccountName()
				.flatMap(new Func1<String, Observable<Account>>() {
					@Override
					public Observable<Account> call(String name) {
						return getAccount(name);
					}
				})
				.flatMap(new Func1<Account, Observable<String>>() {
					@Override
					public Observable<String> call(Account account) {
						return getAuthToken(account, AccountManager.get(context));
					}
				})
				.flatMap(new Func1<String, Observable<?>>() {
					@Override
					public Observable<?> call(String token) {
						return authenticate(token);
					}
				})
				.flatMap(new Func1<Object, Observable<Object>>() {
					@Override
					public Observable<Object> call(Object o) {
						return requestAsRxJava(method, args);
					}
				})
				.retry(new Func2<Integer, Throwable, Boolean>() {
					@Override
					public Boolean call(Integer count, Throwable error) {
						return retry(count, error);
					}
				})
				.toBlocking().first();
	}

	public void invokeAsyncCall(final Method method, final Object[] args) {
		// store original callback
		@SuppressWarnings("unchecked") final
		Callback<Object> originalCallback = (Callback<Object>) args[args.length - 1];
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
						return getAuthToken(account, AccountManager.get(context));
					}
				})
				.flatMap(new Func1<String, Observable<?>>() {
					@Override
					public Observable<?> call(String token) {
						return authenticate(token);
					}
				})
				.flatMap(new Func1<Object, Observable<Pair<Object, Response>>>() {
					@Override
					public Observable<Pair<Object, Response>> call(Object o) {
						return requestAsAsync(method, args);
					}
				})
				.retry(new Func2<Integer, Throwable, Boolean>() {
					@Override
					public Boolean call(Integer count, Throwable error) {
						return retry(count, error);
					}
				})
				.subscribeOn(Schedulers.computation())
				.observeOn(AndroidScheduler.mainThread())
				.subscribe(new Action1<Pair<Object, Response>>() {
							   @Override
							   public void call(Pair<Object, Response> result) {
								   originalCallback.success(result.first, result.second);
							   }
						   },
						new Action1<Throwable>() {
							@Override
							public void call(Throwable throwable) {
								if (throwable instanceof RetrofitError) {
									originalCallback.failure((RetrofitError) throwable);
								} else {
									originalCallback.failure(RetrofitError.unexpectedError(null, throwable));
								}
							}
						});
	}

	private Observable<?> request(Method method, Object[] args) {
		try {
			return (Observable<?>) method.invoke(retrofitService, args);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}


	private Observable<Object> requestAsRxJava(final Method method, final Object[] args) {
		return Observable.create(new OnSubscribe<Object>() {
			@Override
			public void call(Subscriber<? super Object> subscriber) {
				try {
					subscriber.onNext(method.invoke(retrofitService, args));
					subscriber.onCompleted();
				} catch (Throwable e) {
					subscriber.onError(e);
				}
			}
		});
	}

	private Observable<Pair<Object, Response>> requestAsAsync(final Method method, final Object[] args) {
		return Observable.create(new OnSubscribe<Pair<Object, Response>>() {
			@Override
			public void call(final Subscriber<? super Pair<Object, Response>> subscriber) {
				// override the callback which was here before
				args[args.length - 1] = new Callback<Object>() {
					@Override
					public void success(Object o, Response response) {
						subscriber.onNext(new Pair<>(o, response));
						subscriber.onCompleted();
					}

					@Override
					public void failure(RetrofitError error) {
						subscriber.onError(error);
					}
				};
				request(method, args);
			}
		});
	}

	private Observable<Boolean> authenticate(final String token) {
		return Observable.create(new OnSubscribe<Boolean>() {
			@Override
			public void call(Subscriber<? super Boolean> subscriber) {
				serviceInfo.authenticationInterceptor.setToken(token);
				subscriber.onNext(true);
				subscriber.onCompleted();
			}
		});
	}

	private Observable<String> getAuthToken(final Account account, final AccountManager accountManager) {
		return Observable.create(new OnSubscribe<String>() {
			@Override
			public void call(Subscriber<? super String> subscriber) {
				try {
					subscriber.onNext(getAuthTokenBlocking(account, accountManager));
					subscriber.onCompleted();
				} catch (Exception e) {
					subscriber.onError(e);
				}
			}
		});
	}


	private Observable<Account> getAccount(final String name) {
		return Observable.create(new OnSubscribe<Account>() {
			@Override
			public void call(Subscriber<? super Account> subscriber) {
				subscriber.onNext(accountHelper.getActiveAccount(serviceInfo.accountType, name));
				subscriber.onCompleted();
			}
		});
	}


	private Observable<String> getAccountName() {
		return Observable.create(new OnSubscribe<String>() {
			@Override
			public void call(Subscriber<? super String> subscriber) {
				subscriber.onNext(accountHelper.getActiveAccountName(serviceInfo.accountType, true));
				subscriber.onCompleted();
			}
		});
	}

	private boolean retry(@SuppressWarnings("UnusedParameters") int count, Throwable error) {
		if (error instanceof RetrofitError) {
			int status = ((RetrofitError) error).getResponse().getStatus();
			if (HTTP_UNAUTHORIZED == status) {
				accountHelper.invalidateTokenFromActiveUser(serviceInfo.accountType);
				return true;
			}
		}
		return false;
	}

	private String getAuthTokenBlocking(Account account, AccountManager accountManager) throws Exception {
		AccountManagerFuture<Bundle> future;
		Activity activity = (context instanceof Activity) ? (Activity) context : null;
		if (account == null) {
			future = accountManager.addAccount(serviceInfo.accountType, serviceInfo.tokenType, null, null, activity, null, null);
		} else {
			future = accountManager.getAuthToken(account, serviceInfo.tokenType, null, activity, null, null);
		}
		Bundle result = future.getResult();
		return result.getString(AccountManager.KEY_AUTHTOKEN);
	}

}
