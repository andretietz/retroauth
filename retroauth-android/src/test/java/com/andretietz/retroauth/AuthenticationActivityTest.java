package com.andretietz.retroauth;

import android.accounts.Account;
import android.accounts.AccountAuthenticatorResponse;
import android.accounts.AccountManager;
import android.annotation.SuppressLint;
import android.content.Intent;

import com.andretietz.retroauth.testhelper.RetroauthTestLoginActivity;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.android.controller.ActivityController;
import org.robolectric.annotation.Config;

import static com.andretietz.retroauth.testhelper.Helper.setMember;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(RobolectricTestRunner.class)
@Config(manifest=Config.NONE, constants = BuildConfig.class)
public class AuthenticationActivityTest {

    private ActivityController<RetroauthTestLoginActivity> activityController;

    @Before
    public void setup() {
        Intent intent = AuthenticationActivity.createLoginIntent("action", "account", "token");
        activityController = Robolectric.buildActivity(RetroauthTestLoginActivity.class, intent).setup();
    }


    @Test
    public void createLoginIntent() {
        Intent intent = AuthenticationActivity.createLoginIntent("action", "account", "token");
        assertEquals("action", intent.getAction());
        assertEquals("account", intent.getStringExtra(AccountManager.KEY_ACCOUNT_TYPE));
        assertEquals("token", intent.getStringExtra(AccountAuthenticator.KEY_TOKEN_TYPE));
    }


    @Test(expected = IllegalStateException.class)
    public void startActivityFailing() {
        Robolectric.buildActivity(RetroauthTestLoginActivity.class).setup();
    }

    @Test
    public void startActivitySuccess() {
        Intent intent = AuthenticationActivity.createLoginIntent("action", "account", "token");
        Robolectric.buildActivity(RetroauthTestLoginActivity.class, intent).setup();
    }

    @Test
    public void storeTokenWithoutRefreshToken() throws NoSuchFieldException, IllegalAccessException {
        Account account = mock(Account.class);
        AccountManager accountManager = mock(AccountManager.class);
        AuthenticationActivity activity = activityController.get();
        setMember(activity, "accountManager", accountManager);

        activity.storeToken(account, "token-type", "token");

        verify(accountManager, times(1)).setAuthToken(any(Account.class), anyString(), anyString());
    }

    @Test
    public void storeTokenWithRefreshToken() throws NoSuchFieldException, IllegalAccessException {
        Account account = mock(Account.class);
        AccountManager accountManager = mock(AccountManager.class);
        AuthenticationActivity activity = activityController.get();
        setMember(activity, "accountManager", accountManager);

        activity.storeToken(account, "token-type", "token", "refreshToken");

        verify(accountManager, times(2)).setAuthToken(any(Account.class), anyString(), anyString());
    }

    @Test
    public void setUserData() throws NoSuchFieldException, IllegalAccessException {
        Account account = mock(Account.class);
        AccountManager accountManager = mock(AccountManager.class);
        AuthenticationActivity activity = activityController.get();
        setMember(activity, "accountManager", accountManager);

        activity.storeUserData(account, "data-key", "data-value");

        verify(accountManager, times(1)).setUserData(any(Account.class), anyString(), anyString());
    }

    @SuppressWarnings("MissingPermission")
    @Test
    public void createOrGetAccountWhenNoAccountExists() throws NoSuchFieldException, IllegalAccessException {
        AccountManager accountManager = mock(AccountManager.class);
        AuthenticationActivity activity = activityController.get();
        setMember(activity, "accountManager", accountManager);
        setMember(activity, "accountType", "accountType");
        when(accountManager.getAccountsByType(anyString())).thenReturn(new Account[]{});


        Account account = activity.createOrGetAccount("accountName");
        assertEquals(account.name, "accountName");
        assertEquals(account.type, "accountType");

        verify(accountManager, times(1)).addAccountExplicitly(account, null, null);
    }

    @SuppressWarnings("MissingPermission")
    @Test
    public void createOrGetAccountWhenAccountExists() throws NoSuchFieldException, IllegalAccessException {
        AccountManager accountManager = mock(AccountManager.class);
        AuthenticationActivity activity = activityController.get();
        setMember(activity, "accountManager", accountManager);
        setMember(activity, "accountType", "accountType");
        when(accountManager.getAccountsByType(anyString()))
                .thenReturn(new Account[]{new Account("accountName", "accountType")});


        Account account = activity.createOrGetAccount("accountName");
        assertEquals(account.name, "accountName");
        assertEquals(account.type, "accountType");

        verify(accountManager, never()).addAccountExplicitly(account, null, null);
    }

    @SuppressLint("NewApi")
    @Test
    public void removeAccount() throws NoSuchFieldException, IllegalAccessException {
        AccountManager accountManager = mock(AccountManager.class);
        AuthenticationActivity activity = activityController.get();
        setMember(activity, "accountManager", accountManager);
        Account account = mock(Account.class);

        activity.removeAccount(account);

        verify(accountManager, times(1)).removeAccount(account, null, null, null);
    }

    @Test
    public void finalizeAuthenticationWithClosingActivity() {
        AuthenticationActivity activity = activityController.get();
        Account account = mock(Account.class);
        activity.finalizeAuthentication(account);
    }

    @Test
    public void finalizeAuthenticationWithoutClosingActivity() {
        AuthenticationActivity activity = activityController.get();
        Account account = mock(Account.class);
        activity.finalizeAuthentication(account, false);
    }


    @Test
    public void finish() {
        AuthenticationActivity activity = activityController.get();
        activity.finish();
    }

    @Test
    public void finishFromAuthenticator() throws NoSuchFieldException, IllegalAccessException {
        AuthenticationActivity activity = activityController.get();
        setMember(activity, "accountAuthenticatorResponse", mock(AccountAuthenticatorResponse.class));
        activity.finish();
    }

    @Test
    public void finishFromAuthenticatorUnlikelyErrorCase() throws NoSuchFieldException, IllegalAccessException {
        AuthenticationActivity activity = activityController.get();
        setMember(activity, "resultBundle", null);
        setMember(activity, "accountAuthenticatorResponse", mock(AccountAuthenticatorResponse.class));
        activity.finish();
    }

    @Test
    public void getRequestedAccountType() {
        AuthenticationActivity activity = activityController.get();
        assertNotNull(activity.getRequestedAccountType());
    }

    @Test
    public void getRequestedTokenType() {
        AuthenticationActivity activity = activityController.get();
        // not null since the intent in {@link #setup()} is providing it
        assertNotNull(activity.getRequestedTokenType());
    }


}
