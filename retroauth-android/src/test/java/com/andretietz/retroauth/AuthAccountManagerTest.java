package com.andretietz.retroauth;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.content.Context;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.annotation.Config;

import static com.andretietz.retroauth.testhelper.Helper.setMember;
import static org.junit.Assert.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@RunWith(RobolectricTestRunner.class)
@Config(constants = BuildConfig.class, sdk = 23)
public class AuthAccountManagerTest {


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

    @Test
    public void getActiveAccountName() throws Exception {

    }

    @Test
    public void getActiveAccount() throws Exception {

    }

    @Test
    public void getAccountByName() throws Exception {

    }

    @Test
    public void getActiveUserToken() throws Exception {

    }

    @Test
    public void getActiveUserData() throws Exception {

    }

    @Test
    public void setActiveAccount() throws Exception {

    }

    @Test
    public void resetActiveAccount() throws Exception {

    }

    @Test
    public void addAccount() throws Exception {

    }

    @Test
    public void addAccount1() throws Exception {

    }

    @Test
    public void addAccount2() throws Exception {

    }

    @Test
    public void addAccount3() throws Exception {

    }

    @Test
    public void addAccount4() throws Exception {

    }

    @Test
    public void removeActiveAccount() throws Exception {

    }

    @Test
    public void removeActiveAccount1() throws Exception {

    }

}