package com.andretietz.retroauth

import android.accounts.AccountAuthenticatorResponse
import android.accounts.AccountManager
import android.os.Bundle
import android.widget.TextView

class RetroauthTestLoginActivity : AuthenticationActivity() {
    override fun onCreate(icicle: Bundle?) {
        setTheme(android.support.v7.appcompat.R.style.Base_V7_Theme_AppCompat)
        super.onCreate(icicle)
        setContentView(TextView(this))
    }


    fun setTestResponse(accountAuthenticatorResponse: AccountAuthenticatorResponse) {
        this.accountAuthenticatorResponse = accountAuthenticatorResponse
    }

    fun setTestAccountType(type: String) {
        this.accountType = type
    }

    fun setTestAccountManager(accountManager: AccountManager) {
        this.accountManager = accountManager
    }

    fun setTestTokenStorage(tokenStorage: AndroidTokenStorage) {
        this.tokenStorage = tokenStorage
    }
}
