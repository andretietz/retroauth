package com.andretietz.retroauth;

import android.accounts.Account;
import android.accounts.AccountAuthenticatorResponse;
import android.accounts.AccountManager;
import android.accounts.NetworkErrorException;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.RuntimeEnvironment;

import static com.andretietz.retroauth.testhelper.Helper.getMember;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.mockito.Mockito.mock;

@RunWith(RobolectricTestRunner.class)
public class AccountAuthenticatorTest {

    private AccountAuthenticator authenticator;

    @Before
    public void setup() {
        ActivityManager.Companion.get(RuntimeEnvironment.application);
        authenticator = new AccountAuthenticator(mock(Context.class), "some-action");
    }

    @Test
    public void constructor() throws NoSuchFieldException, IllegalAccessException {
        AccountAuthenticator authenticator = new AccountAuthenticator(mock(Context.class), "some-action");
        String action = getMember(authenticator, "action", String.class);
        assertNotNull(action);
        assertEquals("some-action", action);
    }

    @Test
    public void addAccount() throws NetworkErrorException {
        AccountAuthenticatorResponse response = mock(AccountAuthenticatorResponse.class);
        Bundle bundle = authenticator.addAccount(response, "accountType", "tokenType",
                new String[]{}, mock(Bundle.class));

        assertNotNull(bundle);
        Intent intent = bundle.getParcelable(AccountManager.KEY_INTENT);
        assertNotNull(intent);

        assertEquals(
                response,
                intent.getParcelableExtra(AccountManager.KEY_ACCOUNT_AUTHENTICATOR_RESPONSE));
        assertEquals("accountType", intent.getStringExtra(AccountManager.KEY_ACCOUNT_TYPE));
        assertEquals("tokenType", intent.getStringExtra(AccountAuthenticator.Companion.getKEY_TOKEN_TYPE()));
    }
    @Test
    public void getAuthToken() throws NetworkErrorException {
        AccountAuthenticatorResponse response = mock(AccountAuthenticatorResponse.class);
        Account account = new Account("accountName", "accountType");
        Bundle bundle = authenticator.getAuthToken(response, account, "tokenType", mock(Bundle.class));

        assertNotNull(bundle);
        Intent intent = bundle.getParcelable(AccountManager.KEY_INTENT);
        assertNotNull(intent);

        assertEquals(
                response,
                intent.getParcelableExtra(AccountManager.KEY_ACCOUNT_AUTHENTICATOR_RESPONSE));
        assertEquals("accountType", intent.getStringExtra(AccountManager.KEY_ACCOUNT_TYPE));
        assertEquals("tokenType", intent.getStringExtra(AccountAuthenticator.Companion.getKEY_TOKEN_TYPE()));
        assertEquals("accountName", intent.getStringExtra(AccountManager.KEY_ACCOUNT_NAME));
    }

    @Test
    public void hasFeatures() throws NetworkErrorException {
        Bundle bundle = authenticator
                .hasFeatures(mock(AccountAuthenticatorResponse.class), mock(Account.class), new String[]{});
        assertNull(bundle);
    }

    @Test
    public void updateCredentials() throws NetworkErrorException {
        Bundle bundle = authenticator
                .updateCredentials(mock(AccountAuthenticatorResponse.class), mock(Account.class), "token-type",
                        mock(Bundle.class));
        assertNull(bundle);
    }

    @Test
    public void getAuthTokenLabel() throws NetworkErrorException {
        String label = authenticator.getAuthTokenLabel("token-type");
        assertNull(label);
    }

    @Test
    public void editProperties() throws NetworkErrorException {
        Bundle bundle = authenticator
                .editProperties(mock(AccountAuthenticatorResponse.class), "accountType");
        assertNull(bundle);
    }

    @Test
    public void confirmCredentials() throws NetworkErrorException {
        Bundle bundle = authenticator
                .confirmCredentials(mock(AccountAuthenticatorResponse.class), mock(Account.class), mock(Bundle.class));
        assertNull(bundle);
    }
}
