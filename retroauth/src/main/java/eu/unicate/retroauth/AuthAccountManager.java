/*
 * Copyright (c) 2015 Andre Tietz
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package eu.unicate.retroauth;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.accounts.AccountManagerFuture;
import android.accounts.AuthenticatorException;
import android.accounts.OperationCanceledException;
import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AlertDialog;
import android.util.Log;

import java.io.IOException;
import java.util.ArrayList;

import eu.unicate.retroauth.exceptions.AuthenticationCanceledException;
import eu.unicate.retroauth.interfaces.BaseAccountManager;
import rx.Observable;
import rx.Subscriber;

/**
 * This class wraps the Android AccountManager and adds some retroauth specific
 * functionality. This is the main helper class, when working with retroauth.
 */
public final class AuthAccountManager implements BaseAccountManager {

	static final String RETROAUTH_ACCOUNTNAME_KEY = "retroauthActiveAccount";
	private Context context;
	private AccountManager accountManager;

	/**
	 * initializes the class with a context and an AccountManager
	 *
	 * @param context the Android Context
	 */
	public AuthAccountManager(Context context) {
		this.context = context;
		this.accountManager = AccountManager.get(context);
	}

	/**
	 * initializes the class with a context and an AccountManager
	 *
	 * @param context        the Android Context
	 * @param accountManager an AccountManager to use
	 */
	public AuthAccountManager(Context context, AccountManager accountManager) {
		this.context = context;
		this.accountManager = accountManager;
	}

	/**
	 * {@inheritDoc}
	 */
	@Nullable
	@Override
	public Account getActiveAccount(@NonNull String accountType, boolean showDialog) {
		return getAccountByName(getActiveAccountName(accountType, showDialog), accountType);
	}

	/**
	 * {@inheritDoc}
	 */
	@Nullable
	@Override
	public Account getAccountByName(@Nullable String accountName, @NonNull String accountType) {
		// if there's no name, there's no account
		if (accountName == null) return null;
		Account[] accounts = accountManager.getAccountsByType(accountType);
		if (accounts.length == 0) return null;
		if (accounts.length > 1) {
			for (Account account : accounts) {
				if (accountName.equals(account.name)) return account;
			}
			return null;
		}
		return accounts[0];
	}

	/**
	 * {@inheritDoc}
	 */
	@Nullable
	@Override
	public String getActiveAccountName(@NonNull String accountType, boolean showDialog) {
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
		return showDialog ? showAccountPickerDialog(accountType).toBlocking().first() : null;
	}

	/**
	 * {@inheritDoc}
	 */
	@Nullable
	@Override
	public String getTokenFromActiveUser(@NonNull String accountType, @NonNull String tokenType) {
		Account activeAccount = getActiveAccount(accountType, false);
		if (activeAccount == null) return null;
		return accountManager.peekAuthToken(activeAccount, tokenType);
	}

	/**
	 * {@inheritDoc}
	 */
	@Nullable
	@Override
	public String getUserData(@NonNull String accountType, @NonNull String key) {
		return accountManager.getUserData(getActiveAccount(accountType, false), key);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void invalidateTokenFromActiveUser(@NonNull String accountType, @NonNull String tokenType) {
		String token = getTokenFromActiveUser(accountType, tokenType);
		if (token == null) return;
		accountManager.invalidateAuthToken(accountType, token);
	}

	/**
	 * {@inheritDoc}
	 */
	@Nullable
	@Override
	public Account setActiveAccount(@NonNull String accountName, @NonNull String accountType) {
		SharedPreferences preferences = context.getSharedPreferences(accountType, Context.MODE_PRIVATE);
		preferences.edit().putString(RETROAUTH_ACCOUNTNAME_KEY, accountName).apply();
		return getAccountByName(accountName, accountType);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void resetActiveAccount(@NonNull String accountType) {
		SharedPreferences preferences = context.getSharedPreferences(accountType, Context.MODE_PRIVATE);
		preferences.edit().remove(RETROAUTH_ACCOUNTNAME_KEY).apply();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void addAccount(@NonNull Activity activity, @NonNull String accountType, @Nullable String tokenType) {
		accountManager.addAccount(accountType, tokenType, null, null, activity, null, null);
	}


	/**
	 * Shows an account picker for the user to choose an account
	 *
	 * @param accountType     Account type of the accounts the user can choose
	 * @param onItemSelected  a listener to get a callback when the user selects on item
	 * @param onOkClicked     a listener for the click on the ok button
	 * @param onCancelClicked a listener for the click on the cancel button
	 * @param canAddAccount   if <code>true</code> the user has the option to add an account
	 * @return the accounts the user chooses from
	 */
	public Account[] showAccountPickerDialog(String accountType, DialogInterface.OnClickListener onItemSelected, DialogInterface.OnClickListener onOkClicked, DialogInterface.OnClickListener onCancelClicked, boolean canAddAccount) {
		final Account[] accounts = accountManager.getAccountsByType(accountType);
		AlertDialog.Builder builder = new AlertDialog.Builder(context);
		final ArrayList<String> accountList = new ArrayList<>();
		for (Account account : accounts) {
			accountList.add(account.name);
		}
		if (canAddAccount)
			accountList.add(context.getString(R.string.add_account_button_label));
		builder.setTitle(context.getString(R.string.choose_account_label));
		builder.setSingleChoiceItems(accountList.toArray(new String[accountList.size()]), 0, onItemSelected);
		builder.setPositiveButton(android.R.string.ok, onOkClicked);
		builder.setNegativeButton(android.R.string.cancel, onCancelClicked);
		builder.show();
		return accounts;
	}

	/**
	 * Shows an account picker dialog to let the user choose an account
	 *
	 * @param accountType Account type of the accounts the user can choose
	 * @return an observable that emmits a string with the name of the account, the user chose or
	 * <code>null</code> if the current context was not an activity
	 */
	private Observable<String> showAccountPickerDialog(final String accountType) {
		final Account[] accounts = accountManager.getAccountsByType(accountType);
		return Observable.create(new Observable.OnSubscribe<String>() {
			int choosenAccount = 0;

			@Override
			public void call(final Subscriber<? super String> subscriber) {
				// make sure the context is an activity. in case of a service
				// this can and should not work
				if (context instanceof Activity) {
					showAccountPickerDialog(accountType,
							new DialogInterface.OnClickListener() {
								@Override
								public void onClick(DialogInterface dialog, int which) {
									choosenAccount = which;
								}
							},
							new DialogInterface.OnClickListener() {
								@Override
								public void onClick(DialogInterface dialog, int which) {
									if (choosenAccount >= accounts.length) {
										subscriber.onNext(null);
									} else {
										setActiveAccount(accounts[choosenAccount].name, accountType);
										SharedPreferences preferences = context.getSharedPreferences(accountType, Context.MODE_PRIVATE);
										preferences.edit().putString(RETROAUTH_ACCOUNTNAME_KEY, accounts[choosenAccount].name).apply();
										subscriber.onNext(accounts[choosenAccount].name);
									}
									subscriber.onCompleted();
								}
							},
							new DialogInterface.OnClickListener() {
								@Override
								public void onClick(DialogInterface dialog, int which) {
									subscriber.onError(new OperationCanceledException());
								}
							}, true);
				} else {
					subscriber.onNext(null);
				}
			}
		}).subscribeOn(AndroidScheduler.mainThread()); // dialogs have to run on the main thread
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String getAuthToken(@Nullable Account account, @NonNull String accountType, @NonNull String tokenType) throws AuthenticationCanceledException {
		try {
			String token;
			Activity activity = (context instanceof Activity) ? (Activity) context : null;
			if (account == null) {
				token = createAccountAndGetToken(activity, accountType, tokenType);
			} else {
				token = getToken(activity, account, tokenType);
			}
			if(token == null)
				throw new OperationCanceledException("user canceled the login!");
			return token;
		} catch (AuthenticatorException | OperationCanceledException | IOException e) {
			throw new AuthenticationCanceledException(e);
		}
	}

	private String createAccountAndGetToken(@Nullable Activity activity, @NonNull String accountType, @NonNull String tokenType) throws AuthenticatorException, OperationCanceledException, IOException {
		AccountManagerFuture<Bundle> future = accountManager.addAccount(accountType, tokenType, null, null, activity, null, null);
		Bundle result = future.getResult();
		String accountName = result.getString(AccountManager.KEY_ACCOUNT_NAME);
		if(accountName != null) {
			Account account = new Account(result.getString(AccountManager.KEY_ACCOUNT_NAME), result.getString(AccountManager.KEY_ACCOUNT_TYPE));
			return accountManager.peekAuthToken(account, tokenType);
		}
		return null;
	}

	private String getToken(@Nullable Activity activity, @NonNull Account account, @NonNull String tokenType) throws AuthenticatorException, OperationCanceledException, IOException {
		// Clear the interrupted flag
		Thread.interrupted();
		AccountManagerFuture<Bundle> future = accountManager.getAuthToken(account, tokenType, null, activity, null, null);
		Bundle result = future.getResult();
		String token = result.getString(AccountManager.KEY_AUTHTOKEN);
		if(token == null) {
			token = accountManager.peekAuthToken(account, tokenType);
		}
		return token;
	}
}
