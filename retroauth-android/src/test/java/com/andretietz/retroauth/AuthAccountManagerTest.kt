package com.andretietz.retroauth

import android.accounts.Account
import android.accounts.AccountManager
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.ArgumentMatchers.any
import org.mockito.ArgumentMatchers.anyString
import org.mockito.Mockito.`when`
import org.mockito.Mockito.mock
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment

@RunWith(RobolectricTestRunner::class)
class AuthAccountManagerTest {

    private val accountManager: AccountManager = mock(AccountManager::class.java)
    private val authAccountManager: AuthAccountManager = AuthAccountManager(RuntimeEnvironment.application, accountManager)


    @Test
    @Throws(NoSuchFieldException::class, IllegalAccessException::class)
    fun accountAmount() {
        `when`(accountManager.getAccountsByType(anyString()))
                .thenReturn(arrayOf(mock(Account::class.java)))

        assertEquals(this.authAccountManager.accountAmount("asd").toLong(), 1)
    }

    @Test
    fun getActiveAccountFailing() {
        authAccountManager.resetActiveAccount("accountType")
        val account = authAccountManager.getActiveAccount("accountType")
        assertNull(account)
    }

    @Test
    fun getActiveAccountSuccess() {
        `when`(accountManager.getAccountsByType(anyString()))
                .thenReturn(arrayOf(Account("accountName", "accountType")))
        val activeAccount = authAccountManager
                .setActiveAccount("accountType", "accountName")

        val account = authAccountManager.getActiveAccount("accountType")


        assertNotNull(account)
        assertEquals("accountType", account!!.type)
        assertEquals("accountName", account.name)
        assertEquals(activeAccount, account)
    }

    @Test
    fun getAccountByNameSuccess() {
        val testAccount = Account("accountName", "accountType")
        `when`(accountManager.getAccountsByType(anyString()))
                .thenReturn(arrayOf(testAccount))
        val account = authAccountManager.getAccountByName("accountType", "accountName")

        assertNotNull(account)
        assertEquals(testAccount, account)
    }

    @Test
    fun getAccountByNameFail() {
//        val testAccount = Account("accountName", "accountType")
        `when`(accountManager.getAccountsByType(anyString())).thenReturn(arrayOf())
        val account = authAccountManager.getAccountByName("accountType", "accountName")

        assertNull(account)
    }

    @Test
    fun getActiveUserData() {
        val testAccount = Account("accountName", "accountType")
        `when`(accountManager.getAccountsByType(anyString())).thenReturn(arrayOf(testAccount))
        `when`(accountManager.getUserData(any(Account::class.java), anyString())).thenReturn("value")
        authAccountManager.setActiveAccount("accountType", "accountName")

        val data = authAccountManager.getActiveUserData("accountType", "key")
        assertNotNull(data)
        assertEquals("value", data)
    }
}
