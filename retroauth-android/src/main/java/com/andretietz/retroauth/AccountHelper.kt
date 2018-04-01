//package com.andretietz.retroauth
//
//import android.accounts.Account
//import android.accounts.AccountManager
//import android.app.Application
//import android.content.Context
//
//internal class AccountHelper(
//        private val application: Application,
//        private val accountManager: AccountManager = AccountManager.get(application)
//) {
//
//    companion object {
//        private const val RETROAUTH_ACCOUNT_NAME_KEY = "com.andretietz.retroauth.ACTIVE_ACCOUNT"
//    }
//
//    /**
//     * TODO
//     */
//    fun getCurrentAccountName(accountType: String): String? {
//        val preferences = application.getSharedPreferences(accountType, Context.MODE_PRIVATE)
//        return preferences.getString(RETROAUTH_ACCOUNT_NAME_KEY, null)
//    }
//
//    /**
//     * This method returns an account if the account exists on in the account manager.
//     *
//     * When calling this method make sure you have the correct permission to read this accountType. Since you
//     * probably want to read your own account number, no permission is required for this.
//     * If not, you need [android.Manifest.permission.GET_ACCOUNTS] permission
//     *
//     * @param accountType of which you want to get the active account
//     * @param accountName account name you're searching for
//     * @return the account if found. `null` if not
//     */
//    fun getAccountByNameIfExists(accountType: String, accountName: String): Account? {
//        val accounts = accountManager.getAccountsByType(accountType)
//        for (account in accounts) {
//            if (accountName == account.name) return account
//        }
//        return null
//    }
//
//    /**
//     * Sets an account to "the current" one.
//     *
//     * @param account account you want to set as active
//     * @return the account which is not the currently active user
//     */
//    fun setCurrentAccount(account: Account): Account {
//        val preferences = application.getSharedPreferences(account.type, Context.MODE_PRIVATE)
//        preferences.edit().putString(RETROAUTH_ACCOUNT_NAME_KEY, account.name).apply()
//        return account
//    }
//
//    fun resetCurrentAccount(accountType: String) {
//        val preferences = application.getSharedPreferences(accountType, Context.MODE_PRIVATE)
//        preferences.edit().remove(RETROAUTH_ACCOUNT_NAME_KEY).apply()
//    }
//}