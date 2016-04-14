package com.andretietz.retroauth;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.accounts.AccountManagerFuture;
import android.accounts.AuthenticatorException;
import android.accounts.OperationCanceledException;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import java.io.IOException;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import okhttp3.Request;
import retrofit2.Retrofit;

/**
 * Created by andre on 13/04/16.
 */
public class AndroidTokenApi implements TokenApi<AndroidTokenType> {

    private final AuthAccountManager accountManager;
    private final TokenProvider applier;
    private final ContextManager contextManager;
    private AndroidTokenType type;

    public AndroidTokenApi(@NonNull Activity activity, @NonNull TokenProvider applier) {
        this.contextManager = ContextManager.get(activity);
        this.accountManager = new AuthAccountManager(contextManager.getContext());
        this.applier = applier;
    }

    @Override
    public Request modifyRequest(String token, Request request) {
        return applier.applyToken(token, request);
    }

    @Override
    public AndroidTokenType convert(String[] annotationValues) {
        type = new AndroidTokenType.Builder()
                .accountType(annotationValues[0])
                .tokenType(annotationValues[1])
                .build();
        return type;
    }

    @Override
    public void receiveToken(final OnTokenReceiveListener listener) throws Exception {
        Account account = requestActiveAccount(type.accountType);
        String token = getAuthToken(account, type.accountType, type.tokenType);
        listener.onTokenReceive(token);
    }

    @Override
    public String refreshToken(Retrofit retrofit, String token) {
        return applier.refreshToken(retrofit, token);
    }

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

    private String createAccountAndGetToken(@Nullable Activity activity, @NonNull String accountType,
                                            @NonNull String tokenType) throws AuthenticatorException, OperationCanceledException, IOException {
        AccountManagerFuture<Bundle> future = accountManager.addAccount(activity, accountType, tokenType);
        Bundle result = future.getResult();
        String accountName = result.getString(AccountManager.KEY_ACCOUNT_NAME);
        if (accountName != null) {
            Account account = new Account(result.getString(AccountManager.KEY_ACCOUNT_NAME),
                    result.getString(AccountManager.KEY_ACCOUNT_TYPE));
            return accountManager.android.peekAuthToken(account, tokenType);
        }
        return null;
    }


    private String getToken(@Nullable Activity activity, @NonNull Account account, @NonNull String tokenType)
            throws AuthenticatorException, OperationCanceledException, IOException {
        // Clear the interrupted flag
        Thread.interrupted();
        AccountManagerFuture<Bundle> future = accountManager.getAuthToken(activity, account, tokenType);
        Bundle result = future.getResult();
        String token = result.getString(AccountManager.KEY_AUTHTOKEN);
        if (token == null) {
            token = accountManager.android.peekAuthToken(account, tokenType);

        }
        return token;
    }

    @Nullable
    private Account requestActiveAccount(@NonNull String accountType) throws ChooseAccountCanceledException {
        // get active account name
        String accountName = accountManager.getActiveAccountName(accountType);
        // if this one exists, try to get the account
        if (accountName != null) return accountManager.getAccountByName(accountType, accountName);
        // if it doesn't, ask the user to pick an account
        accountName = showAccountPickerDialog(accountType, true);
        // if the user has chosen an existing account
        if (accountName != null) {
            accountManager.setActiveAccount(accountType, accountName);
            return accountManager.getAccountByName(accountType, accountName);
        }
        // if the user chose to add an account
        return null;
    }

    /**
     * Shows an account picker for the user to choose an account
     *
     * @param accountType   Account type of the accounts the user can choose
     * @param canAddAccount if <code>true</code> the user has the option to add an account
     * @return the accounts the user chooses from
     */
    public String showAccountPickerDialog(String accountType, boolean canAddAccount) throws ChooseAccountCanceledException {
        Account[] accounts = accountManager.android.getAccountsByType(accountType);
        if (accounts.length == 0) return null;
        String[] accountList = new String[canAddAccount ? accounts.length + 1 : accounts.length];
        for (int i = 0; i < accounts.length; i++) {
            accountList[i] = accounts[i].name;
        }
        if (canAddAccount) {
            accountList[accounts.length] = contextManager.getContext().getString(R.string.add_account_button_label);
        }
        ReentrantLock lock = new ReentrantLock();
        Condition condition = lock.newCondition();
        Activity activity = contextManager.getActivity();
        ShowDialogOnUI showDialog = new ShowDialogOnUI(accountList, lock, condition);
        if (activity != null) {
            activity.runOnUiThread(showDialog);
            lock.lock();
            try {
                condition.await();
            } catch (InterruptedException e) {
                // ignore
            } finally {
                lock.unlock();
            }
        }
        if (showDialog.canceled) {
            throw new ChooseAccountCanceledException("User canceled authentication!");
        }
        return showDialog.selectedOption;
    }

    private class ShowDialogOnUI implements Runnable {

        private final Condition condition;
        private final String[] options;
        private final Lock lock;
        String selectedOption;
        boolean canceled = false;

        ShowDialogOnUI(String[] options, Lock lock, Condition condition) {
            this.options = options;
            this.condition = condition;
            this.lock = lock;
            this.selectedOption = options[0];
        }

        @Override
        public void run() {
            AlertDialog.Builder builder = new AlertDialog.Builder(contextManager.getActivity());
            builder.setTitle(contextManager.getContext().getString(R.string.choose_account_label));
            builder.setCancelable(false);
            builder.setSingleChoiceItems(options, 0, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    if (which < options.length - 1) {
                        selectedOption = options[which];
                    } else {
                        selectedOption = null;
                    }
                }
            });
            builder.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    lock.lock();
                    try {
                        condition.signal();
                    } finally {
                        lock.unlock();
                    }
                }
            });
            builder.setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    canceled = true;
                    lock.lock();
                    try {
                        condition.signal();
                    } finally {
                        lock.unlock();
                    }
                }
            });
            builder.show();
        }
    }
}
