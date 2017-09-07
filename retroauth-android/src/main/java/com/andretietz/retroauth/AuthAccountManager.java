/*
 * Copyright (c) 2016 Andre Tietz
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

import android.Manifest;
import android.accounts.Account;
import android.accounts.AccountManager;
import android.accounts.AccountManagerCallback;
import android.accounts.AccountManagerFuture;
import android.app.Application;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.RequiresPermission;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * This class wraps the Android {@link android.accounts.AccountManager} and adds some retroauth specific
 * functionality.
 */
public final class AuthAccountManager {

    static final String RETROAUTH_ACCOUNTNAME_KEY = "com.andretietz.retroauth.ACTIVE_ACCOUNT";
    private final AccountManager accountManager;
    private final ActivityManager activityManager;
    private final Application application;

    public AuthAccountManager(Application application) {
        this.activityManager = ActivityManager.get(application);
        this.accountManager = AccountManager.get(application);
        this.application = application;
    }

    /**
     * This method returns the name of the active account of the chosen accountType.
     *
     * @param accountType of which you want to get the active accountname of
     * @return the name of the currently active account or {@code null}
     */
    @Nullable
    public String getActiveAccountName(@NonNull String accountType) {
        SharedPreferences preferences = application.getSharedPreferences(accountType, Context.MODE_PRIVATE);
        return preferences.getString(RETROAUTH_ACCOUNTNAME_KEY, null);
    }

    /**
     * @param accountType of which you want to get the active account
     * @return the currently active account or {@code null}
     */
    @Nullable
    public Account getActiveAccount(@NonNull String accountType) {
        String accountName = getActiveAccountName(accountType);
        if (accountName != null) {
            return getAccountByName(accountType, accountName);
        }
        return null;
    }

    /**
     * When calling this method make sure you have the correct permission to read this accountType. Since you
     * propably want to read your own account number, no permission is required for this.
     * If not, you need GET_ACCOUNTS permission
     *
     * @param accountType of which you want to get the active account
     * @param accountName account name you're searching for
     * @return the account if found. {@code null} if not
     */
    @Nullable
    @SuppressWarnings("MissingPermission")
    public Account getAccountByName(@NonNull String accountType, @NonNull String accountName) {
        Account[] accounts = accountManager.getAccountsByType(accountType);
        for (Account account : accounts) {
            if (accountName.equals(account.name)) return account;
        }
        return null;
    }

    /**
     * @param accountType of which you want to get the active account
     * @param key         in which you stored userdata using
     *                    {@link AuthenticationActivity#storeUserData(Account, String, String)}
     * @return the userdata stored, using the given key or {@code null} if there's no userdata stored within the key
     */
    @Nullable
    public String getActiveUserData(@NonNull String accountType, @NonNull String key) {
        return accountManager.getUserData(getActiveAccount(accountType), key);
    }

    /**
     * This will store the username of an accountType (different accountTypes can have the same username) in the
     * {@link SharedPreferences}.
     *
     * @param accountType of which you want to get the active account
     * @param accountName account name you want to set as active
     * @return the account which is not the currently active user
     */
    @Nullable
    public Account setActiveAccount(@NonNull String accountType, @NonNull String accountName) {
        Account account = getAccountByName(accountType, accountName);
        if (account == null) return null;
        SharedPreferences preferences = application.getSharedPreferences(accountType, Context.MODE_PRIVATE);
        preferences.edit().putString(RETROAUTH_ACCOUNTNAME_KEY, accountName).apply();
        return account;
    }

    /**
     * Deletes the currently active user from the {@link SharedPreferences}. When the user next time calls an
     * {@link Authenticated} Request, he'll be asked which user to use as active user. This will be saved after choosing
     *
     * @param accountType accountType to reset
     */
    public void resetActiveAccount(@NonNull String accountType) {
        SharedPreferences preferences = application.getSharedPreferences(accountType, Context.MODE_PRIVATE);
        preferences.edit().remove(RETROAUTH_ACCOUNTNAME_KEY).apply();
    }


    /**
     * Adds a new account for the given account type. This method is a shortcut for
     * {@link #addAccount(String, String)}
     *
     * @param accountType the account type you want to create an account for
     */
    public void addAccount(@NonNull String accountType) {
        addAccount(accountType, null);
    }


    /**
     * Adds a new account for the given account type. The tokenType is optional. you can request this type in the login
     * {@link android.support.v7.app.AppCompatActivity} calling {@link AuthenticationActivity#getRequestedTokenType()}.
     * This value will not be available when you're creating an account from Android-Settings-Accounts-Add Account
     *
     * @param accountType the account type you want to create an account for
     * @param tokenType   the type of token you want to create
     * @param callback    which is called when the account has been created or account creation was canceled.
     */
    public void addAccount(@NonNull String accountType, @Nullable String tokenType, @Nullable AccountCallback callback) {
        CreateAccountCallback cac = (callback != null) ? new CreateAccountCallback(callback) : null;
        accountManager.addAccount(accountType, tokenType, null, null, activityManager.getActivity(), cac, null);
    }

    /**
     * Adds a new account for the given account type. The tokenType is optional. you can request this type in the login
     * {@link android.support.v7.app.AppCompatActivity} calling {@link AuthenticationActivity#getRequestedTokenType()}.
     * This value will not be available when you're creating an account from Android-Settings-Accounts-Add Account
     *
     * @param accountType the account type you want to create an account for
     * @param tokenType   the type of token you want to create
     */
    public void addAccount(@NonNull String accountType, @Nullable String tokenType) {
        addAccount(accountType, tokenType, null);
    }

    /**
     * When calling this method make sure you have the correct permission to read this accountType. Since you
     * propably want to read your own account number, no permission is required for this.
     * If not, you need GET_ACCOUNTS permission
     *
     * @param accountType AccountType which you want to know the amount of
     * @return number of existing accounts of this type. Depending on which accountType you're requesting this could
     * require additional permissions
     */
    @SuppressWarnings("MissingPermission")
    public int accountAmount(@NonNull String accountType) {
        return accountManager.getAccountsByType(accountType).length;
    }

    /**
     * Removes the currently active account
     *
     * @param accountType the account type of which you want to delete the active user from
     * @param callback    callback returns, when account was deleted.
     */
    public void removeActiveAccount(@NonNull String accountType, @Nullable AccountCallback callback) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP_MR1) {
            RemoveLollipopAccountCallback rac = (callback != null) ? new RemoveLollipopAccountCallback(callback) : null;
            accountManager.removeAccount(getActiveAccount(accountType), null, rac, null);
        } else {
            RemoveAccountCallback rac = (callback != null) ? new RemoveAccountCallback(callback) : null;
            //noinspection deprecation
            accountManager.removeAccount(getActiveAccount(accountType), rac, null);
        }
        resetActiveAccount(accountType);
    }

    /**
     * Removes the currently active account
     *
     * @param accountType the account type of which you want to delete the active user from
     */
    public void removeActiveAccount(@NonNull String accountType) {
        removeActiveAccount(accountType, null);
    }

    /**
     * Returns an intent to open an account chooser
     *
     * @param accountType Make sure this is your account type or you requested the permission for
     *                    {@link Manifest.permission#GET_ACCOUNTS}, otherwise there will be a {@link SecurityException}
     *                    thrown
     * @return an Intent which you can start for result to open an account chooser.
     */
    @NonNull
    @SuppressWarnings({"MissingPermission", "deprecation"})
    @RequiresPermission(Manifest.permission.GET_ACCOUNTS)
    public Intent newChooseAccountIntent(@NonNull String accountType) {
        List<Account> accounts = Arrays.asList(accountManager.getAccountsByType(accountType));
        Account account = getActiveAccount(accountType);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            return AccountManager.newChooseAccountIntent(account, accounts,
                    new String[]{accountType}, null, null,
                    null, null);
        } else {
            return AccountManager.newChooseAccountIntent(account,
                    (accounts instanceof ArrayList) ? (ArrayList<Account>) accounts : new ArrayList<>(accounts),
                    new String[]{accountType}, false, null, null,
                    null, null);
        }
    }

    public interface AccountCallback {
        void done(boolean success);
    }

    /**
     * Callback wrapper for adding an account
     */
    private static final class CreateAccountCallback implements AccountManagerCallback<Bundle> {
        private final AccountCallback callback;

        CreateAccountCallback(AccountCallback callback) {
            this.callback = callback;
        }

        @Override
        public void run(AccountManagerFuture<Bundle> accountManagerFuture) {
            try {
                String accountName = accountManagerFuture.getResult().getString(AccountManager.KEY_ACCOUNT_NAME);
                callback.done(accountName != null);
            } catch (Exception e) {
                callback.done(false);
            }
        }
    }

    /**
     * Callback wrapper for account removing on >= lollipop (22) devices
     */
    private static final class RemoveLollipopAccountCallback implements AccountManagerCallback<Bundle> {
        private final AccountCallback callback;

        private RemoveLollipopAccountCallback(AccountCallback callback) {
            this.callback = callback;
        }

        @Override
        public void run(AccountManagerFuture<Bundle> accountManagerFuture) {
            try {
                callback.done(accountManagerFuture.getResult().getBoolean(AccountManager.KEY_BOOLEAN_RESULT));
            } catch (Exception e) {
                callback.done(false);
            }
        }
    }

    /**
     * Callback wrapper for account removing on prelollipop (22) devices
     */
    private static final class RemoveAccountCallback implements AccountManagerCallback<Boolean> {
        private final AccountCallback callback;

        private RemoveAccountCallback(AccountCallback callback) {
            this.callback = callback;
        }

        @Override
        public void run(AccountManagerFuture<Boolean> accountManagerFuture) {
            try {
                callback.done(accountManagerFuture.getResult());
            } catch (Exception e) {
                callback.done(false);
            }
        }
    }
}
