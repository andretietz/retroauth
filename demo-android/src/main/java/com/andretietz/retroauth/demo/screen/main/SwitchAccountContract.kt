package com.andretietz.retroauth.demo.screen.main

import android.accounts.Account
import android.accounts.AccountManager
import android.content.Context
import android.content.Intent
import android.webkit.CookieManager
import androidx.activity.result.contract.ActivityResultContract
import androidx.appcompat.app.AppCompatActivity
import com.andretietz.retroauth.demo.R
import com.andretietz.retroauth.demo.auth.LoginActivity

class SwitchAccountContract : ActivityResultContract<Account, Account?>() {
  override fun createIntent(context: Context, input: Account?): Intent {
    CookieManager.getInstance().removeAllCookies(null)
    return if (input != null) {
      AccountManager.newChooseAccountIntent(
        input,
        null,
        arrayOf(input.type),
        null,
        null,
        null,
        null
      )
    } else {
      Intent(context, LoginActivity::class.java).also {
        it.putExtra(
          AccountManager.KEY_ACCOUNT_TYPE,
          context.getText(R.string.authentication_ACCOUNT)
        )
      }
    }
  }


  override fun parseResult(resultCode: Int, data: Intent?): Account? {
    return if (resultCode == AppCompatActivity.RESULT_OK) {
      val accountType = data?.getStringExtra(AccountManager.KEY_ACCOUNT_TYPE)
      val accountName = data?.getStringExtra(AccountManager.KEY_ACCOUNT_NAME)
      if (accountType != null && accountName != null) {
        Account(accountName, accountType)
      } else null
    } else null
  }
}
