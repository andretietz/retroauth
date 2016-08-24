package com.andretietz.retroauth;

import android.accounts.Account;
import android.accounts.AccountManager;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.annotation.Config;

import static com.andretietz.retroauth.testhelper.Helper.setMember;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@RunWith(RobolectricTestRunner.class)
@Config(constants = BuildConfig.class, sdk = 23)
public class AuthAccountManagerTest {


    private AuthAccountManager authAccountManager;
    private AccountManager accountManager;

    @Before
    public void setup() throws NoSuchFieldException, IllegalAccessException {
        ContextManager.get(RuntimeEnvironment.application);
        accountManager = mock(AccountManager.class);
        authAccountManager = new AuthAccountManager();
        setMember(authAccountManager, "accountManager", accountManager);
    }


    @SuppressWarnings("MissingPermission")
    @Test
    public void accountAmount() throws NoSuchFieldException, IllegalAccessException {
        AccountManager accountManager = mock(AccountManager.class);
        setMember(this.authAccountManager, "accountManager", accountManager);

        when(accountManager.getAccountsByType(anyString()))
                .thenReturn(new Account[]{mock(Account.class)});

        assertEquals(this.authAccountManager.accountAmount("asd"), 1);
    }

    @Test
    public void getActiveAccountFailing() {
        authAccountManager.resetActiveAccount("accountType");
        Account account = authAccountManager.getActiveAccount("accountType");
        assertNull(account);
    }

    @SuppressWarnings("MissingPermission")
    @Test
    public void getActiveAccountSuccess() {
        when(accountManager.getAccountsByType(anyString()))
                .thenReturn(new Account[]{new Account("accountName", "accountType")});
        Account activeAccount = authAccountManager
                .setActiveAccount("accountType", "accountName");

        Account account = authAccountManager.getActiveAccount("accountType");


        assertNotNull(account);
        assertEquals("accountType", account.type);
        assertEquals("accountName", account.name);
        assertEquals(activeAccount, account);
    }

    @SuppressWarnings("MissingPermission")
    @Test
    public void getAccountByNameSuccess() {
        Account testAccount = new Account("accountName", "accountType");
        when(accountManager.getAccountsByType(anyString()))
                .thenReturn(new Account[]{testAccount});
        Account account = authAccountManager.getAccountByName("accountType", "accountName");

        assertNotNull(account);
        assertEquals(testAccount, account);
    }

    @SuppressWarnings("MissingPermission")
    @Test
    public void getAccountByNameFail() {
        Account testAccount = new Account("accountName", "accountType");
        when(accountManager.getAccountsByType(anyString()))
                .thenReturn(new Account[]{});
        Account account = authAccountManager.getAccountByName("accountType", "accountName");

        assertNull(account);
    }

    @SuppressWarnings("MissingPermission")
    @Test
    public void getActiveUserData() {
        Account testAccount = new Account("accountName", "accountType");
        when(accountManager.getAccountsByType(anyString()))
                .thenReturn(new Account[]{testAccount});
        when(accountManager.getUserData(any(Account.class), anyString()))
                .thenReturn("value");
        authAccountManager
                .setActiveAccount("accountType", "accountName");

        String data = authAccountManager.getActiveUserData("accountType", "key");
        assertNotNull(data);
        assertEquals("value", data);
    }
}
