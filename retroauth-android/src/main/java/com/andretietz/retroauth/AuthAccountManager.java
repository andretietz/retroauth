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

package com.andretietz.retroauth;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.accounts.AccountManagerFuture;
import android.accounts.AuthenticatorException;
import android.accounts.OperationCanceledException;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Application;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.FutureTask;

/**
 * This class wraps the Android AccountManager and adds some retroauth specific
 * functionality. This is the main helper class, when working with retroauth.
 */
public final class AuthAccountManager implements BaseAccountManager {

    static final String RETROAUTH_ACCOUNTNAME_KEY = "com.andretietz.retroauth.ACTIVE_ACCOUNT";
    private final ContextManager contextManager;
    private final AccountManager accountManager;

    AuthAccountManager(@NonNull ContextManager contextManager) {
        this.contextManager = contextManager;
        this.accountManager = AccountManager.get(contextManager.getContext());
    }

    public AuthAccountManager(Context context) {
        this(ContextManager.get((Application) context.getApplicationContext()));
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
        // get all accounts of your account type
        Account[] accounts = accountManager.getAccountsByType(accountType);
        if (accounts.length < 1) {
            return null;
        } else if (accounts.length >= 1) {
            // check if there is an account setup as current
/*            SharedPreferences preferences = contextManager.getContext().getSharedPreferences(accountType, Context.MODE_PRIVATE);
            String accountName = preferences.getString(RETROAUTH_ACCOUNTNAME_KEY, null);
            if (accountName != null) {
                for (Account account : accounts) {
                    if (accountName.equals(account.name)) return account.name;
                }
            }*/
            try {
                return showAccountPickerDialog(accountType, true).get();
            } catch (Exception e) {
                return null;
            }
        }
        return null;
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
        SharedPreferences preferences = contextManager.getContext().getSharedPreferences(accountType, Context.MODE_PRIVATE);
        preferences.edit().putString(RETROAUTH_ACCOUNTNAME_KEY, accountName).apply();
        return getAccountByName(accountName, accountType);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void resetActiveAccount(@NonNull String accountType) {
        SharedPreferences preferences = contextManager.getContext().getSharedPreferences(accountType, Context.MODE_PRIVATE);
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
     * {@inheritDoc}
     */
    @Override
    public String getAuthToken(@Nullable Account account, @NonNull String accountType, @NonNull String tokenType)
            throws AuthenticationCanceledException {
        try {
            String token;
            Activity activity = contextManager.getActivity();
            if (account == null) {
                token = createAccountAndGetToken(activity, accountType, tokenType);
            } else {
                token = getToken(activity, account, tokenType);
            }
            if (token == null) {
                throw new AuthenticationCanceledException("user canceled the login!");
            }
            return token;
        } catch (AuthenticatorException | OperationCanceledException | IOException e) {
            throw new AuthenticationCanceledException(e);
        }
    }

    /**
     * Shows an account picker for the user to choose an account
     *
     * @param accountType   Account type of the accounts the user can choose
     * @param canAddAccount if <code>true</code> the user has the option to add an account
     * @return the accounts the user chooses from
     */
    public Future<String> showAccountPickerDialog(String accountType, boolean canAddAccount) {
        final Account[] accounts = accountManager.getAccountsByType(accountType);
        final ArrayList<String> accountList = new ArrayList<>();
        for (Account account : accounts) {
            accountList.add(account.name);
        }
        if (canAddAccount) {
            accountList.add(contextManager.getContext().getString(R.string.add_account_button_label));
        }
        final AccountChosenCallable chosenCallable = new AccountChosenCallable(accountList);
        FutureTask<String> task = new FutureTask<>(chosenCallable);
        contextManager.getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                AlertDialog.Builder builder = new AlertDialog.Builder(contextManager.getActivity());
                builder.setTitle(contextManager.getContext().getString(R.string.choose_account_label));
                builder.setSingleChoiceItems(accountList.toArray(new String[accountList.size()]), 0, null);
                builder.setPositiveButton(android.R.string.ok, chosenCallable);
                builder.setNegativeButton(android.R.string.cancel, null);
                builder.show();
            }
        });
        //Executors.newCachedThreadPool().submit(task);
        return task;
    }

    private String createAccountAndGetToken(@Nullable Activity activity, @NonNull String accountType,
                                            @NonNull String tokenType) throws AuthenticatorException, OperationCanceledException, IOException {
        AccountManagerFuture<Bundle> future =
                accountManager.addAccount(accountType, tokenType, null, null, activity, null, null);
        Bundle result = future.getResult();
        String accountName = result.getString(AccountManager.KEY_ACCOUNT_NAME);
        if (accountName != null) {
            Account account = new Account(result.getString(AccountManager.KEY_ACCOUNT_NAME),
                    result.getString(AccountManager.KEY_ACCOUNT_TYPE));
            return accountManager.peekAuthToken(account, tokenType);
        }
        return null;
    }

    private String getToken(@Nullable Activity activity, @NonNull Account account, @NonNull String tokenType)
            throws AuthenticatorException, OperationCanceledException, IOException {
        // Clear the interrupted flag
        Thread.interrupted();
        AccountManagerFuture<Bundle> future = accountManager.getAuthToken(account, tokenType, null, activity, null, null);
        Bundle result = future.getResult();
        String token = result.getString(AccountManager.KEY_AUTHTOKEN);
        if (token == null) {
            token = accountManager.peekAuthToken(account, tokenType);
        }
        return token;
    }

    private static class AccountChosenCallable implements DialogInterface.OnClickListener, Callable<String> {

        private final List<String> accounts;
        String choosenAccount;

        AccountChosenCallable(List<String> accounts) {
            this.accounts = accounts;
        }

        @Override
        public String call() throws Exception {
            wait();
            return choosenAccount;
        }

        @Override
        public void onClick(DialogInterface dialog, int which) {
            if (which > 0) choosenAccount = accounts.get(which);
            notify();
        }
    }

    /* public static class AccountChooseDialog extends DialogFragment {

        private final String accountType;
        private final boolean canAddAccount;
        private final DialogInterface.OnClickListener itemSelectedListener;
        private final OnClickListener okClickListener;
        private final OnClickListener cancelClickListener;

        public AccountChooseDialog(String accountType, boolean addAccounts, DialogInterface.OnClickListener itemSelected,
              DialogInterface.OnClickListener onOkClicked, DialogInterface.OnClickListener onCancelClicked) {
            this.accountType = accountType;
            this.canAddAccount = addAccounts;
            this.itemSelectedListener = itemSelected;
            this.okClickListener = onOkClicked;
            this.cancelClickListener = onCancelClicked;
        }

        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            AccountManager accountManager = AccountManager.get(getActivity());
            final Account[] accounts = accountManager.getAccountsByType(accountType);
            AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
            final ArrayList<String> accountList = new ArrayList<>();
            for (Account account : accounts) {
                accountList.add(account.name);
            }
            if (canAddAccount) { accountList.add(getString(R.string.add_account_button_label)); }
            builder.setTitle(getString(R.string.choose_account_label));
            builder.setSingleChoiceItems(accountList.toArray(new String[accountList.size()]), 0, itemSelectedListener);
            builder.setPositiveButton(android.R.string.ok, okClickListener);
            builder.setNegativeButton(android.R.string.cancel, cancelClickListener);
            return builder.create();
        }
    } */
}
