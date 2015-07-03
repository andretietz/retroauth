package eu.unicate.retroauth;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.accounts.AccountManagerFuture;
import android.accounts.OperationCanceledException;
import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v4.util.Pair;
import android.support.v7.app.AlertDialog;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;

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
public class RxAuthInvoker<T> {

	public static final String RETROAUTH_SHARED_PREFERENCES = "eu.unicate.retroauth.account";
	public static final String RETROAUTH_ACCOUNTNAME_KEY = "current";
	private static final int HTTP_UNAUTHORIZED = 401;


	private final Context context;
	private final T retrofitService;
	private final ServiceInfo serviceInfo;
	private final AccountManager accountManager;

	public RxAuthInvoker(Context context, T retrofitService, ServiceInfo serviceInfo) {
		this.context = context;
		this.retrofitService = retrofitService;
		this.serviceInfo = serviceInfo;
		this.accountManager = AccountManager.get(context);
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
		return 				getAccountName()
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
						return getAuthToken(account);
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
								if(throwable instanceof RetrofitError) {
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

	private Observable<String> getAuthToken(final Account account) {
		return Observable.create(new OnSubscribe<String>() {
			@Override
			public void call(Subscriber<? super String> subscriber) {
				try {
					subscriber.onNext(getAuthTokenBlocking(account));
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
				subscriber.onNext(getAccountBlocking(name));
				subscriber.onCompleted();
			}
		});
	}


	public Observable<String> getAccountName() {
		return Observable.create(new OnSubscribe<String>() {
			@Override
			public void call(Subscriber<? super String> subscriber) {
				subscriber.onNext(getAccountNameBlocking());
				subscriber.onCompleted();
			}
		});
	}

	public boolean retry(@SuppressWarnings("UnusedParameters") int count, Throwable error) {
		if (error instanceof RetrofitError) {
			int status = ((RetrofitError) error).getResponse().getStatus();
			if (HTTP_UNAUTHORIZED == status) {
				Account account = getAccountBlocking(getAccountNameBlocking());
				String authToken = accountManager.peekAuthToken(account, serviceInfo.tokenType);
				accountManager.invalidateAuthToken(account.type, authToken);
				return true;
			}
		}
		return false;
	}

	public String getAccountNameBlocking() {
		Account[] accounts = accountManager.getAccountsByType(serviceInfo.accountType);
		if (accounts.length < 1) {
			return null;
		} else if (accounts.length > 1) {
			// check if there is an account setup as current
			SharedPreferences preferences = context.getSharedPreferences(RETROAUTH_SHARED_PREFERENCES, Context.MODE_PRIVATE);
			String accountName = preferences.getString(RETROAUTH_ACCOUNTNAME_KEY, null);
			if (accountName != null) {
				for (Account account : accounts) {
					if (accountName.equals(account.name)) return account.name;
				}
			}
		} else {
			return accounts[0].name;
		}
		return showPicker().toBlocking().first();
	}

	public Account getAccountBlocking(String accountName) {
		// if there's no name, there's no account
		if (accountName == null) return null;
		Account[] accounts = accountManager.getAccountsByType(serviceInfo.accountType);
		if (accounts.length > 1) {
			for (Account account : accounts) {
				if (accountName.equals(account.name)) return account;
			}
			// this should not happen
			throw new RuntimeException("Could not find account with name: " + accountName);
		}
		return accounts[0];
	}

	public String getAuthTokenBlocking(Account account) throws Exception {
		AccountManagerFuture<Bundle> future;
		AccountManager accountManager = AccountManager.get(context);
		Activity activity = (context instanceof Activity) ? (Activity) context : null;
		if (account == null) {
			future = accountManager.addAccount(serviceInfo.accountType, serviceInfo.tokenType, null, null, activity, null, null);
		} else {
			future = accountManager.getAuthToken(account, serviceInfo.tokenType, null, activity, null, null);
		}
		Bundle result = future.getResult();
		return result.getString(AccountManager.KEY_AUTHTOKEN);
	}

	public Observable<String> showPicker() {
		final Account[] accounts = accountManager.getAccountsByType(serviceInfo.accountType);
		return Observable.create(new Observable.OnSubscribe<String>() {
			int choosenAccount = 0;
			@Override
			public void call(final Subscriber<? super String> subscriber) {
				// make sure the context is an activity. in case of a service
				// this can and should not work
				if (context instanceof Activity) {
					AlertDialog.Builder builder = new AlertDialog.Builder(context);
					final ArrayList<String> accountList = new ArrayList<>();
					for (Account account : accounts) {
						accountList.add(account.name);
					}
					accountList.add(context.getString(R.string.add_account_button_label));
					builder.setTitle(context.getString(R.string.choose_account_label));
					builder.setSingleChoiceItems(accountList.toArray(new String[accountList.size()]), 0, new DialogInterface.OnClickListener() {
						@Override
						public void onClick(DialogInterface dialog, int which) {
							choosenAccount = which;
						}
					});
					builder.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
						@Override
						public void onClick(DialogInterface dialog, int which) {
							dialog.dismiss();
							if (choosenAccount >= accounts.length) {
								subscriber.onNext(null);
							} else {
								SharedPreferences preferences = context.getSharedPreferences(RETROAUTH_SHARED_PREFERENCES, Context.MODE_PRIVATE);
								preferences.edit().putString(RETROAUTH_ACCOUNTNAME_KEY, accounts[choosenAccount].name).apply();
								subscriber.onNext(accounts[choosenAccount].name);
							}
							subscriber.onCompleted();
						}
					});
					builder.setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
						@Override
						public void onClick(DialogInterface dialog, int which) {
							dialog.cancel();
							subscriber.onError(new OperationCanceledException());
						}
					});
					builder.show();
				} else {
					subscriber.onNext(null);
				}
			}
		})
				// dialogs have to run on the main thread
				.subscribeOn(AndroidScheduler.mainThread());
	}
}
