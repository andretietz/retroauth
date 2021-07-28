package com.andretietz.retroauth.demo.screen.main

import android.accounts.Account
import androidx.lifecycle.ViewModel
import com.andretietz.retroauth.AndroidAccountManagerCredentialStorage
import com.andretietz.retroauth.AndroidAccountManagerOwnerStorage
import com.andretietz.retroauth.demo.auth.GithubAuthenticator
//import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import okhttp3.Cache
import retrofit2.Retrofit
//import javax.inject.Inject

//@HiltViewModel
class AndroidMainViewModel constructor(
  retrofit: Retrofit,
  private val ownerStorage: AndroidAccountManagerOwnerStorage,
  credentialStorage: AndroidAccountManagerCredentialStorage,
  private val authenticator: GithubAuthenticator,
  cache: Cache
) : ViewModel(), MainViewModel<Account> by CommonMainViewModel(
  retrofit,
  ownerStorage,
  credentialStorage,
  authenticator,
  cache,
  CoroutineScope(Dispatchers.Default)
) {
//
//
//  private fun cleanWebCookies() {
//    /** remove all cookies to avoid an automatic re-login within the webview.  */
//    CookieManager.getInstance().removeAllCookies(null)
//  }

  fun getCurrentAccount() = ownerStorage.getActiveOwner(authenticator.getOwnerType())

}
