package eu.unicate.retroauth;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.accounts.OperationCanceledException;
import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.support.v7.app.AlertDialog;

import java.util.ArrayList;

import rx.Observable;
import rx.Subscriber;

/**
 * TODO
 */
public class AccountHelper {

	private static final String RETROAUTH_ACCOUNTNAME_KEY = "current";

	/**
	 * TODO
	 */
	public static Account getActiveAccount(Context context, AccountManager accountManager, String accountType) {
		return getActiveAccount(context, accountType, getActiveAccountName(context, accountManager, accountType));
	}
	/**
	 * TODO
	 */
	public static Account getActiveAccount(Context context, String accountType, String accountName) {
		AccountManager accountManager = AccountManager.get(context);
		// if there's no name, there's no account
		if (accountName == null) return null;
		Account[] accounts = accountManager.getAccountsByType(accountType);
		if (accounts.length > 1) {
			for (Account account : accounts) {
				if (accountName.equals(account.name)) return account;
			}
			throw new RuntimeException("Could not find account with name: " + accountName);
		}
		return accounts[0];
	}

	/**
	 * TODO
	 */
	public static String getActiveAccountName(Context context, AccountManager accountManager, String accountType) {
		Account[] accounts = accountManager.getAccountsByType(accountType);
		if (accounts.length < 1) {
			return null;
		} else if (accounts.length > 1) {
			// check if there is an account setup as current
			SharedPreferences preferences = context.getSharedPreferences(accountType, Context.MODE_PRIVATE);
			String accountName = preferences.getString(RETROAUTH_ACCOUNTNAME_KEY, null);
			if (accountName != null) {
				for (Account account : accounts) {
					if (accountName.equals(account.name)) return account.name;
				}
			}
		} else {
			return accounts[0].name;
		}
		return showAccountPicker(context, accountType).toBlocking().first();
	}

	private static Observable<String> showAccountPicker(final Context context, final String accountTypeParam) {
		AccountManager accountManager = AccountManager.get(context);
		final Account[] accounts = accountManager.getAccountsByType(accountTypeParam);
		return Observable.create(new Observable.OnSubscribe<String>() {
			int choosenAccount = 0;
			String accountType;
			@Override
			public void call(final Subscriber<? super String> subscriber) {
				// make sure the context is an activity. in case of a service
				// this can and should not work
				if (context instanceof Activity) {
					this.accountType = accountTypeParam;
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
								SharedPreferences preferences = context.getSharedPreferences(accountType, Context.MODE_PRIVATE);
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
