//package com.andretietz.retroauth
//
//import android.accounts.Account
//import android.accounts.AccountManager
//import android.support.test.InstrumentationRegistry
//import android.support.test.rule.ActivityTestRule
//import android.support.test.runner.AndroidJUnit4
//import org.junit.Rule
//import org.junit.Test
//import org.junit.runner.RunWith
//
//
//@RunWith(AndroidJUnit4::class)
//class AuthenticationActivityTest {
//
//
//    companion object {
//        private const val intentAction = "action"
//        private const val accountName = "accountName"
//        private const val accountType = "accountType"
//        private const val tokenType = "tokenType"
//        private const val dataKey = "dataKey"
//        private const val dataValue = "dataValue"
//        private val account = Account(accountName, accountType)
//
//        private val accountManager = AccountManager.get(InstrumentationRegistry.getTargetContext().applicationContext)
//    }
//
//    @get:Rule
//    val rule = ActivityTestRule(
//            TestAuthenticationActivity::class.java,
//            false,
//            false
//    )
//
////    @Before
////    fun setup() {
////        accountManager.addAccountExplicitly(account, null, null)
////    }
////
////    @After
////    fun tearDown() {
////        accountManager.removeAccount(account, null, null, null)
////    }
//
////    @Test(expected = IllegalStateException::class)
////    fun startActivityFail() {
//    // onCreate is calling this method to check
////        AuthenticationActivity.checkForAccountTypeValidity(null)
////    }
//
//    @Test
//    fun startActivitySuccessWithoutTokenType() {
//        rule.launchActivity(AuthenticationActivity.createLoginIntent(intentAction, accountType))
//    }
//
//    @Test
//    fun startActivitySuccessWithTokenType() {
//        rule.launchActivity(AuthenticationActivity.createLoginIntent(intentAction, accountType, tokenType))
//    }
//
////    @Test
////    fun setUserData() {
////        val activity = rule.launchActivity(AuthenticationActivity.createLoginIntent(intentAction, accountType))
////        val accountManager = spy(AccountManager.get(activity))
////
////        activity.setTestAccountManager(accountManager)
////
////        activity.storeUserData(account, dataKey, dataValue)
////
////        verify(accountManager).setUserData(
////                eq(account),
////                eq(dataKey),
////                eq(dataValue))
////    }
//}