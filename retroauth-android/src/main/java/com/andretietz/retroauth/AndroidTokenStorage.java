package com.andretietz.retroauth;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.accounts.AccountManagerFuture;
import android.accounts.AuthenticatorException;
import android.accounts.OperationCanceledException;
import android.app.Activity;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import java.io.IOException;

/**
 * Created by andre on 13/04/16.
 */
public class AndroidTokenStorage implements TokenStorage<Account, AndroidTokenType, AndroidToken> {

    private final AccountManager accountManager;
    private final ContextManager contextManager;

    public AndroidTokenStorage(@NonNull ContextManager contextManager) {
        this.contextManager = contextManager;
        this.accountManager = AccountManager.get(contextManager.getContext());
    }

    @Override
    public AndroidTokenType createType(String[] annotationValues) {
        return new AndroidTokenType.Builder()
                .accountType(annotationValues[0])
                .tokenType(annotationValues[1])
                .build();
    }

    @Override
    public AndroidToken getToken(Account account, AndroidTokenType type) throws AuthenticationCanceledException {
        try {
            AndroidToken token;
            Activity activity = contextManager.getActivity();
            if (account == null) {
                token = createAccountAndGetToken(activity, type);
            } else {
                token = getToken(activity, account, type);
            }
            if (token == null) {
                throw new AuthenticationCanceledException("user canceled the login!");
            }
            return token;
        } catch (AuthenticatorException | OperationCanceledException | IOException e) {
            throw new AuthenticationCanceledException(e);
        }
    }

    private AndroidToken createAccountAndGetToken(@Nullable Activity activity, @NonNull AndroidTokenType type)
            throws AuthenticatorException, OperationCanceledException, IOException {

        AccountManagerFuture<Bundle> future = accountManager
                .addAccount(type.accountType, type.tokenType, null, null, activity, null, null);
        Bundle result = future.getResult();
        String accountName = result.getString(AccountManager.KEY_ACCOUNT_NAME);
        if (accountName != null) {
            Account account = new Account(result.getString(AccountManager.KEY_ACCOUNT_NAME),
                    result.getString(AccountManager.KEY_ACCOUNT_TYPE));
            String token = accountManager.peekAuthToken(account, type.tokenType);
            String refreshToken = accountManager.peekAuthToken(account, getRefreshTokenType(type));
            if (token != null) return new AndroidToken(token, refreshToken);
        }
        return null;
    }

    private AndroidToken getToken(@Nullable Activity activity, @NonNull Account account, @NonNull AndroidTokenType type)
            throws AuthenticatorException, OperationCanceledException, IOException {
        // Clear the interrupted flag
        Thread.interrupted();
        AccountManagerFuture<Bundle> future = accountManager.getAuthToken(account, type.tokenType, null, activity, null, null);
        Bundle result = future.getResult();
        String token = result.getString(AccountManager.KEY_AUTHTOKEN);
        String refreshToken = accountManager.peekAuthToken(account, getRefreshTokenType(type));
        if (token == null) {
            token = accountManager.peekAuthToken(account, type.tokenType);
        }
        if (token != null) return new AndroidToken(token, refreshToken);
        return null;
    }

    private String getRefreshTokenType(AndroidTokenType type) {
        return String.format("%s_refresh", type.tokenType);
    }

    @Override
    public void removeToken(Account account, AndroidTokenType type, AndroidToken androidToken) {
        accountManager.invalidateAuthToken(account.type, androidToken.token);
        accountManager.invalidateAuthToken(account.type, androidToken.refreshToken);
    }

    @Override
    public void saveToken(Account account, AndroidTokenType type, AndroidToken androidToken) {
        accountManager.setAuthToken(account, type.tokenType, androidToken.token);
        accountManager.setAuthToken(account, getRefreshTokenType(type), androidToken.refreshToken);
    }
}
