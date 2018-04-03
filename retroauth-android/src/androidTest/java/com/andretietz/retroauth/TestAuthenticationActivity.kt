package com.andretietz.retroauth

import android.accounts.AccountManager


class TestAuthenticationActivity : AuthenticationActivity() {
    fun setTestAccountManager(accountManager: AccountManager) {
        this.accountManager = accountManager
    }

}