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

package eu.unicate.retroauth.interfaces;

import android.accounts.Account;
import android.app.Activity;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import eu.unicate.retroauth.AuthenticationActivity;
import eu.unicate.retroauth.exceptions.AuthenticationCanceledException;

public interface BaseAccountManager {
	/**
	 * Gets the currently active account by the account type. The active account name is determined
	 * by the method {@link #getActiveAccountName(String, boolean)}. If there's only one account
	 * this one will be returned, no matter if you reseted the currently active one
	 *
	 * @param accountType     Account Type you want to retreive
	 * @param userInteraction If there is more than one account and there is no
	 *                        current active account, a user interaction is required to
	 *                        let the user choose one. If you want to do so, set this to <code>true</code>
	 *                        else to <code>false</code>.
	 * @return the active (or only) Account or <code>null</code> in case there is no account
	 */
	@Nullable
	Account getActiveAccount(@NonNull String accountType, boolean userInteraction);

	/**
	 * Gets an account by the name of the account and it's type
	 *
	 * @param accountName Name of the Account you want to get
	 * @param accountType Account Type of which your account is
	 * @return The Account by Name or <code>null</code>
	 */
	@Nullable
	Account getAccountByName(@Nullable String accountName, @NonNull String accountType);

	/**
	 * Get the currently active account name
	 *
	 * @param accountType     Type of the Account you want the usernames from <code>null</code> for
	 *                        all types
	 * @param userInteraction If there is more than one account and there is no
	 *                        current active account, a user interaction is required to
	 *                        let the user choose one. If you want to do so, set this to <code>true</code>
	 *                        else to <code>false</code>.
	 * @return The currently active account name or <code>null</code>
	 */
	@Nullable
	String getActiveAccountName(@NonNull String accountType, boolean userInteraction);

	/**
	 * Returns the Token of the currently active user
	 *
	 * @param accountType Account type of the user you want the token from
	 * @param tokenType   Token type of the token you want to retrieve
	 * @return The Token or <code>null</code>
	 */
	@Nullable
	String getTokenFromActiveUser(@NonNull String accountType, @NonNull String tokenType);

	/**
	 * Returns userdata which has to be setup while calling {@link AuthenticationActivity#finalizeAuthentication(String, String, String, Bundle)}
	 *
	 * @param accountType Account type to get the active account
	 * @param key         Key wiht which you want to request the value
	 * @return The Value or <code>null</code> if the account or the key does not exist
	 */
	@SuppressWarnings("unused")
	@Nullable
	String getUserData(@NonNull String accountType, @NonNull String key);

	/**
	 * Invalidates the Token of the given type for the active user
	 *
	 * @param accountType Account type of the active user
	 * @param tokenType   Token type you want to invalidate
	 */
	void invalidateTokenFromActiveUser(@NonNull String accountType, @NonNull String tokenType);

	/**
	 * Sets an active user. If you handle with multiple accounts you can setup an active user.
	 * The token of the active user will be taken for all future requests
	 *
	 * @param accountName name of the account
	 * @param accountType Account type of the active user
	 * @return the active account or <code>null</code> if the account could not be found
	 */
	@Nullable
	Account setActiveAccount(@NonNull String accountName, @NonNull String accountType);

	/**
	 * Unset the active user.
	 *
	 * @param accountType The account type where you want to unset it's current
	 */
	void resetActiveAccount(@NonNull String accountType);

	/**
	 * Starts the Activity to start the login process which adds the account.
	 *
	 * @param activity    The current active activity
	 * @param accountType The account type you want to create (this account type will be available on {@link AuthenticationActivity#getRequestedAccountType()} then
	 * @param tokenType   The tokentype you want to request. This is an optional parameter and can be <code>null</code> (this token type will be available on {@link AuthenticationActivity#getRequestedTokenType()} then
	 */
	void addAccount(@NonNull Activity activity, @NonNull String accountType, @Nullable String tokenType);

	/**
	 * Returns a Token of the given tokentype from the currently active user. If there is no user,
	 * the user has to create one. This method is blocking until the user created or updated the account
	 *
	 * @param account     Account to get the token from
	 * @param accountType the accountType to create if there's no account
	 * @param tokenType   token type you need the token to be
	 * @return the token
	 * @throws AuthenticationCanceledException thrown when the account creation is canceled
	 */
	@Nullable
	String getAuthToken(@Nullable Account account, @NonNull String accountType, @NonNull String tokenType) throws AuthenticationCanceledException;
}
