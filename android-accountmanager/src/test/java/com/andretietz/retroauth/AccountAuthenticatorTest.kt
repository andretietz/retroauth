package com.andretietz.retroauth

import android.accounts.Account
import android.accounts.AccountAuthenticatorResponse
import android.accounts.AccountManager
import android.app.Application
import android.content.Intent
import android.os.Bundle
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mockito.mock


@RunWith(AndroidJUnit4::class)
class AccountAuthenticatorTest {

  private val application = ApplicationProvider.getApplicationContext<Application>()

  private val authenticator: AccountAuthenticator =
    AccountAuthenticator(application, "some-action") {}

  @Before
  fun setup() {
    ActivityManager[application]
  }

  @Test
  fun addAccount() {
    val response = mock(AccountAuthenticatorResponse::class.java)
    val bundle = authenticator.addAccount(
      response, "accountType", "credentialType",
      arrayOf(), mock(Bundle::class.java)
    )

    assertNotNull(bundle)
    val intent = requireNotNull(bundle).getParcelable<Intent>(AccountManager.KEY_INTENT)
    assertNotNull(intent)

    assertEquals(
      response,
      requireNotNull(intent).getParcelableExtra(AccountManager.KEY_ACCOUNT_AUTHENTICATOR_RESPONSE)
    )
    assertEquals("accountType", intent.getStringExtra(AccountManager.KEY_ACCOUNT_TYPE))
    assertEquals("credentialType", intent.getStringExtra(AccountAuthenticator.KEY_CREDENTIAL_TYPE))
  }

  @Test
  fun getAuthToken() {
    val response = mock(AccountAuthenticatorResponse::class.java)
    val account = Account("accountName", "accountType")
    val bundle =
      authenticator.getAuthToken(response, account, "credentialType", mock(Bundle::class.java))

    assertNotNull(bundle)
    val intent = requireNotNull(bundle).getParcelable<Intent>(AccountManager.KEY_INTENT)
    assertNotNull(intent)

    assertEquals(
      response,
      requireNotNull(intent).getParcelableExtra(AccountManager.KEY_ACCOUNT_AUTHENTICATOR_RESPONSE)
    )
    assertEquals("accountType", intent.getStringExtra(AccountManager.KEY_ACCOUNT_TYPE))
    assertEquals("credentialType", intent.getStringExtra(AccountAuthenticator.KEY_CREDENTIAL_TYPE))
    assertEquals("accountName", intent.getStringExtra(AccountManager.KEY_ACCOUNT_NAME))
  }

  @Test
  fun hasFeatures() {
    val bundle = authenticator
      .hasFeatures(
        mock(AccountAuthenticatorResponse::class.java),
        mock(Account::class.java),
        arrayOf()
      )
    assertNull(bundle)
  }

  @Test
  fun updateCredentials() {
    val bundle = authenticator
      .updateCredentials(
        mock(AccountAuthenticatorResponse::class.java),
        mock(Account::class.java),
        "credential-type",
        mock(Bundle::class.java)
      )
    assertNull(bundle)
  }

  @Test
  fun getAuthTokenLabel() {
    val label = authenticator.getAuthTokenLabel("credential-type")
    assertNull(label)
  }

  @Test
  fun editProperties() {
    val bundle = authenticator
      .editProperties(mock(AccountAuthenticatorResponse::class.java), "accountType")
    assertNull(bundle)
  }

  @Test
  fun confirmCredentials() {
    val bundle = authenticator
      .confirmCredentials(
        mock(AccountAuthenticatorResponse::class.java), mock(Account::class.java),
        mock(Bundle::class.java)
      )
    assertNull(bundle)
  }
}
