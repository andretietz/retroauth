package eu.unicate.retroauth;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.accounts.AccountManagerFuture;
import android.accounts.OperationCanceledException;
import android.app.Activity;
import android.app.Dialog;
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
import rx.android.schedulers.AndroidSchedulers;

public class AuthInvoker<T> {

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

//	public String getAccountName(Context context) {
//		AccountManager accountManager = AccountManager.get(context);
//		Account[] accounts = accountManager.getAccountsByType(accountType);
//	}

	public Object request() throws InvocationTargetException, IllegalAccessException {
		return method.invoke(retrofitService, args);
	}

	public boolean authenticate(String token) {
		serviceInfo.authenticationInterceptor.setToken(token);
		return true;
	}

	public boolean retry(int count, Throwable error) {
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
				// token has been invalidated, retry to fetch new token
				return true;
			}
		}
		return false;
	}

	public String getAccountName() {
		Account[] accounts = accountManager.getAccountsByType(serviceInfo.accountType);
		if(accounts.length < 1) {
			return null;
		} else if(accounts.length > 1) {
			// check if there is an account setup as current
			SharedPreferences preferences = context.getSharedPreferences("eu.unicate.retroauth.account", Context.MODE_PRIVATE);
			String accountName = preferences.getString("current", null);
			if (accountName != null) {
				for (Account account : accounts) {
					if (accountName.equals(account.name)) return account.name;
				}
			}
		} else {
			return accounts[0].name;
		}
		return showPicker(context, accounts).subscribeOn(AndroidSchedulers.mainThread()).toBlocking().first();
//		return getAccountName();
	}

	public Account getAccount(String accountName) {
		// if there's no name, there's no account
		if(accountName == null) return null;
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

	public Observable<String> showPicker(final Context context, final Account[] accounts) {

		return
				Observable.create(new Observable.OnSubscribe<String>() {
					@Override
					public void call(final Subscriber<? super String> subscriber) {
						AlertDialog.Builder builder = new AlertDialog.Builder(context);
						final ArrayList<String> accountList = new ArrayList<>();
						for (Account account : accounts) {
							accountList.add(account.name);
						}
						accountList.add("Add Account");
						builder.setTitle("Choose your Account");
						builder.setSingleChoiceItems(accountList.toArray(new String[accountList.size()]), 0, new DialogInterface.OnClickListener() {
							@Override
							public void onClick(DialogInterface dialog, int which) {
							}
						});
		builder.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				dialog.dismiss();
				subscriber.onNext("test");
//				if(null == choosenAccount) {
//					final AccountManager accountManager = AccountManager.get(context);
//					AccountManagerFuture<Bundle> future = accountManager.addAccount(accountType, null, null, null, (context instanceof Activity) ? ((Activity) context) : null, null, null);
//					try {
//						future.getResult();
//					} catch (OperationCanceledException e) {
//						e.printStackTrace();
//					} catch (IOException e) {
//						e.printStackTrace();
//					} catch (AuthenticatorException e) {
//						e.printStackTrace();
//					}
//				} else {
//					Log.e("TAG", "account choosen:" + choosenAccount.name);
//				}

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
//		dialog.setOnCancelListener(new OnCancelListener() {
//			@Override
//			public void onCancel(DialogInterface dialog) {
//				Log.e("TAG", "cancel");
//			}
//		});
//		dialog.setOnDismissListener(new OnDismissListener() {
//			@Override
//			public void onDismiss(DialogInterface dialog) {
//				Log.e("TAG", "dismiss");
//			}
//		});
//						dialog.show();
					}
				}).subscribeOn(AndroidSchedulers.mainThread());

	}


	public AuthInvoker<T> init(Method method, Object[] args) {
		this.method = method;
		this.args = args;
		return this;
	}
}
