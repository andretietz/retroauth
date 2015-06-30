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
import android.support.v7.app.AlertDialog;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;

import retrofit.RetrofitError;
import rx.Observable;
import rx.Subscriber;

public class AuthInvoker<T> {

	public static final String RETROAUTH_SHARED_PREFERENCES = "eu.unicate.retroauth.account";
	public static final String RETROAUTH_ACCOUNTNAME_KEY = "current";
	private static final int HTTP_UNAUTHORIZED = 401;
	private final Context context;
	private final T retrofitService;
	private final ServiceInfo serviceInfo;
	private final AccountManager accountManager;
	private Method method;
	private Object[] args;

	public AuthInvoker(Context context, T retrofitService, ServiceInfo serviceInfo) {
		this.context = context;
		this.retrofitService = retrofitService;
		this.serviceInfo = serviceInfo;
		this.accountManager = AccountManager.get(context);
	}

	public Object request() throws InvocationTargetException, IllegalAccessException {
		return method.invoke(retrofitService, args);
	}

	public boolean authenticate(String token) {
		serviceInfo.authenticationInterceptor.setToken(token);
		return true;
	}

	public boolean retry(@SuppressWarnings("UnusedParameters") int count, Throwable error) {
		if (error instanceof RetrofitError) {
			int status = ((RetrofitError) error).getResponse().getStatus();
			if (HTTP_UNAUTHORIZED == status) {
				Account account = getAccount(getAccountName());
				String authToken = accountManager.peekAuthToken(account, serviceInfo.tokenType);
				accountManager.invalidateAuthToken(account.type, authToken);
				return true;
			}
		}
		return false;
	}

	public String getAccountName() {
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

	public Account getAccount(String accountName) {
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

	public String getAuthToken(Account account) throws Exception {
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


	public AuthInvoker<T> init(Method method, Object[] args) {
		this.method = method;
		this.args = args;
		return this;
	}
}
