package eu.unicate.retroauth;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.content.Context;
import android.content.SharedPreferences;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class AuthAccountManagerTests {

	@Mock
	AccountManager accountManager;

	@Mock
	Context context;

	private RetroauthAccountManager retroauthAccountManager;

	@Before
	public void setupTest() {
		retroauthAccountManager = RetroauthAccountManager.get(context, accountManager);
	}

	@Test
	public void getActiveAccountName() {
		SharedPreferences sharedPreferences = Mockito.mock(SharedPreferences.class);
		when(context.getSharedPreferences(anyString(), anyInt())).thenReturn(sharedPreferences);

		when(accountManager.getAccountsByType(anyString())).thenReturn(new Account[]{}); // no account stored

		// No account in the account manager, should return null
		String accountName = retroauthAccountManager.getActiveAccountName("testAccountType", false);
		Assert.assertNull(accountName);

		// if there's only one account in the AccountManager, return this one
		when(accountManager.getAccountsByType(anyString())).thenReturn(new Account[]{new Account("testAccountName", "testAccountType")}); // one account available
		accountName = retroauthAccountManager.getActiveAccountName("testAccountType", false);
		Assert.assertNotNull(accountName);
		Assert.assertEquals("testAccountName", accountName);

		// if there's no active account expect null
		when(accountManager.getAccountsByType(anyString())).thenReturn(new Account[]{new Account("testAccountName", "testAccountType"), new Account("testAccountName2", "testAccountType")}); // multiple accounts
		when(sharedPreferences.getString(anyString(), (String) any())).thenReturn(null); // no account active
		accountName = retroauthAccountManager.getActiveAccountName("testAccountType", false);
		Assert.assertNull(accountName);

		// if there's an active account expect it's name
		when(accountManager.getAccountsByType(anyString())).thenReturn(new Account[]{new Account("testAccountName", "testAccountType"), new Account("testAccountName2", "testAccountType")}); // multiple accounts
		when(sharedPreferences.getString(anyString(), (String) any())).thenReturn("testAccountName"); // one account active
		accountName = retroauthAccountManager.getActiveAccountName("testAccountType", false);
		Assert.assertNotNull(accountName);
		Assert.assertEquals("testAccountName", accountName);

		// if there is an unexpected active account expect null
		when(accountManager.getAccountsByType(anyString())).thenReturn(new Account[]{new Account("testAccountName", "testAccountType"), new Account("testAccountName2", "testAccountType")}); // multiple accounts
		when(sharedPreferences.getString(anyString(), (String) any())).thenReturn("some-unknown-account-name"); // one account active
		accountName = retroauthAccountManager.getActiveAccountName("testAccountType", false);
		Assert.assertNull(accountName);
	}

	@Test
	public void getActiveAccountWithTypeAndName() {
		// if no account exists
		when(accountManager.getAccountsByType(anyString())).thenReturn(new Account[]{});
		Account activeAccount = retroauthAccountManager.getAccountByName("testAccountName", "testAccountType");
		Assert.assertNull(activeAccount);

		// for one existing account
		when(accountManager.getAccountsByType(anyString())).thenReturn(new Account[]{new Account("testAccountName", "testAccountType")});
		activeAccount = retroauthAccountManager.getAccountByName("testAccountName", "testAccountType");
		Assert.assertNotNull(activeAccount);
		Assert.assertEquals("testAccountName", activeAccount.name);

		// for multiple account existing
		when(accountManager.getAccountsByType(anyString())).thenReturn(new Account[]{new Account("testAccountName", "testAccountType"), new Account("testAccountName2", "testAccountType")});
		activeAccount = retroauthAccountManager.getAccountByName("testAccountName", "testAccountType");
		Assert.assertNotNull(activeAccount);
		Assert.assertEquals("testAccountName", activeAccount.name);

		// requesting an account that does not exist
		when(accountManager.getAccountsByType(anyString())).thenReturn(new Account[]{new Account("testAccountName", "testAccountType"), new Account("testAccountName2", "testAccountType")});
		// should throw the RuntimeException
		activeAccount = retroauthAccountManager.getAccountByName("nonexistingAccountName", "testAccountType");
		Assert.assertNull(activeAccount);
	}
}
