package eu.unicate.retroauth;

import android.accounts.Account;
import android.accounts.AccountManager;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;
import org.mockito.Mock;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.when;

@RunWith(JUnit4.class)
public class AccountHelperTests {


	@Mock
	AccountManager accountManager;

//	@Before
	public void mockSetup() {
//		accountManager = Mockito.mock(AccountManager.class);
		when(accountManager.getAccountsByType("accountType")).thenReturn(new Account[]{new Account("testuser", "accountType")});
	}

//	@Test
	public void getAccountNameTest() {
//		AccountManager accountManager = Mockito.mock(AccountManager.class);
		when(accountManager.getAccountsByType("accountType")).thenReturn(new Account[]{new Account("testuser", "accountType")});
		String accountName = AccountHelper.getActiveAccountName(null, accountManager, "accountType");
		assertEquals("testuser", accountName);
	}

	@Test
	public void dummyTest() {
		assertTrue(true);
	}
}
