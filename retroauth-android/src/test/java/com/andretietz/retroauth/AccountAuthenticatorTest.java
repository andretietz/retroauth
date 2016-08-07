package com.andretietz.retroauth;

import android.accounts.Account;
import android.accounts.AccountAuthenticatorResponse;
import android.accounts.NetworkErrorException;
import android.content.Context;
import android.os.Bundle;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.annotation.Config;

import static com.andretietz.retroauth.testhelper.Helper.getMember;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.mockito.Mockito.mock;

@RunWith(RobolectricTestRunner.class)
@Config(constants = BuildConfig.class, sdk = 23)
public class AccountAuthenticatorTest {

    private AccountAuthenticator authenticator;

    @Before
    public void setup() {
        ContextManager.get(RuntimeEnvironment.application);
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
}
