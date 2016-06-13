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

import android.accounts.Account;
import android.accounts.AccountManager;
import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

/**
 * This class wraps the Android AccountManager and adds some retroauth specific
 * functionality.
 */
public final class AuthAccountManager {

    static final String RETROAUTH_ACCOUNTNAME_KEY = "com.andretietz.retroauth.ACTIVE_ACCOUNT";
    private final AccountManager accountManager;
    private final ContextManager contextManager;

    public AuthAccountManager() {
        this.contextManager = ContextManager.get();
        this.accountManager = AccountManager.get(contextManager.getContext());
    }

    /**
     * This method returns the name of the active account of the chosen accountType.
     *
     * @param accountType of which you want to get the active accountname of
     * @return the name of the currently active account or {@code null}
     */
    @Nullable
    public String getActiveAccountName(@NonNull String accountType) {
        SharedPreferences preferences = contextManager.getContext().getSharedPreferences(accountType, Context.MODE_PRIVATE);
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
     * @param accountType of which you want to get the active account
     * @param accountName account name you're searching for
     * @return the account if found. {@code null} if not
     */
    @Nullable
    public Account getAccountByName(@NonNull String accountType, @NonNull String accountName) {
        Account[] accounts = accountManager.getAccountsByType(accountType);
        for (Account account : accounts) {
            if (accountName.equals(account.name)) return account;
        }
        return null;
    }

    /**
     * @param accountType of which you want to get the active account
     * @param tokenType   of the token you want to get
     * @return the token of the tokenType of the currently active user
     */
    @Nullable
    public String getActiveUserToken(@NonNull String accountType, @NonNull String tokenType) {
        Account activeAccount = getActiveAccount(accountType);
        if (activeAccount == null) return null;
        return accountManager.peekAuthToken(activeAccount, tokenType);
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
        SharedPreferences preferences = contextManager.getContext().getSharedPreferences(accountType, Context.MODE_PRIVATE);
        preferences.edit().putString(RETROAUTH_ACCOUNTNAME_KEY, accountName).apply();
        return getAccountByName(accountType, accountName);
    }

    /**
     * Deletes the currently active user from the {@link SharedPreferences}. When the user next time calls an
     * {@link Authenticated} Request, he'll be asked which user to use as active user. This will be saved after choosing
     *
     * @param accountType accountType to reset
     */
    public void resetActiveAccount(@NonNull String accountType) {
        SharedPreferences preferences = contextManager.getContext().getSharedPreferences(accountType, Context.MODE_PRIVATE);
        preferences.edit().remove(RETROAUTH_ACCOUNTNAME_KEY).apply();
    }


    /**
     * Adds a new account for the given account type. This method is a shortcut for
     * {@link #addAccount(Activity, String, String)}
     *
     * @param activity    must be provided in order to open the login activity
     * @param accountType the account type you want to create an account for
     */
    public void addAccount(@NonNull Activity activity, @NonNull String accountType) {
        addAccount(activity, accountType, null);
    }

    /**
     * Adds a new account for the given account type. The tokenType is optional. you can request this type in the login
     * {@link Activity} calling {@link AuthenticationActivity#getRequestedTokenType()}. This value will not be available
     * when you're creating an account from Android-Settings-Accounts-Add Account
     *
     * @param activity    must be provided in order to open the login activity
     * @param accountType the account type you want to create an account for
     * @param tokenType   the type of token you want to create
     */
    public void addAccount(@Nullable Activity activity, @NonNull String accountType, @Nullable String tokenType) {
        accountManager.addAccount(accountType, tokenType, null, null, activity, null, null);
    }

    /**
     * Removes the currently active account
     *
     * @param accountType the account type of which you want to delete the active user from
     */
    public void removeActiveAccount(@NonNull String accountType) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP_MR1) {
            accountManager.removeAccount(getActiveAccount(accountType), null, null, null);
        } else {
            accountManager.removeAccount(getActiveAccount(accountType), null, null);
        }
        resetActiveAccount(accountType);
    }
}
