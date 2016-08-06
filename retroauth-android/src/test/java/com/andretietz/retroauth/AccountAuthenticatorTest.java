package com.andretietz.retroauth;

import android.accounts.Account;
import android.accounts.AccountManager;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.annotation.Config;

import java.lang.reflect.Field;

import static com.andretietz.retroauth.testhelper.Helper.setMember;
import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@RunWith(RobolectricTestRunner.class)
@Config(constants = BuildConfig.class, sdk = 23)
public class AccountAuthenticatorTest {

    private AuthAccountManager accountManager;

    @Before
    public void setup() {
        ContextManager.get(RuntimeEnvironment.application);
        accountManager = new AuthAccountManager();
    }

    @SuppressWarnings("MissingPermission")
    @Test
    public void accountAmount() throws NoSuchFieldException, IllegalAccessException {
        AccountManager accountManager = mock(AccountManager.class);
        setMember(this.accountManager, "accountManager", accountManager);

        when(accountManager.getAccountsByType(anyString()))
                .thenReturn(new Account[]{mock(Account.class)});

        assertEquals(this.accountManager.accountAmount("asd"), 1);
    }
}
