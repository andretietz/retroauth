package com.andretietz.retroauth.demo.screen.main

import android.accounts.Account
import android.accounts.AccountManager
import android.content.Intent
import android.os.Build
import android.webkit.CookieManager
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.andretietz.retroauth.*
import com.andretietz.retroauth.demo.api.GithubApi
import com.andretietz.retroauth.demo.auth.GithubAuthenticator
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.consumeAsFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import retrofit2.Retrofit
import timber.log.Timber
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(
  retrofit: Retrofit,
  private val ownerStorage: AndroidOwnerStorage,
  private val credentialStorage: AndroidCredentialStorage,
  private val authenticator: GithubAuthenticator
) : ViewModel() {

  private val _state = Channel<ViewState>(Channel.CONFLATED).apply {
    viewModelScope.launch { send(ViewState.InitialState) }
  }

  val state = _state.receiveAsFlow()

  private val api = retrofit.create(GithubApi::class.java)

  fun addAccount() {
    cleanWebCookies()
    ownerStorage.createOwner(
      authenticator.getOwnerType(),
      authenticator.getCredentialType(),
      object : Callback<Account> {
        override fun onResult(result: Account) {
          Timber.d("Logged in: $result")
          _state.offer(ViewState.LoginSuccess(result))
        }

        override fun onError(error: Throwable) {
          _state.offer(ViewState.Error(error))
        }
      })
  }

  suspend fun loadRepositories() {
    withContext(Dispatchers.IO) {
      try {
        _state.offer(ViewState.RepositoryUpdate(api.getRepositories()))
      } catch (error: AuthenticationRequiredException) {
        Timber.i("User not logged in! Opening Login Screen")
      }
    }
  }

  fun invalidateTokens() {
    ownerStorage.getActiveOwner(authenticator.getOwnerType())?.let { account ->
      credentialStorage.getCredentials(
        account,
        authenticator.getCredentialType(),
        object : Callback<AndroidCredentials> {
          override fun onResult(result: AndroidCredentials) {
            credentialStorage.storeCredentials(
              account,
              authenticator.getCredentialType(),
              AndroidCredentials("some-invalid-token", result.data)
            )
          }

          override fun onError(error: Throwable) {
//            lifecycleScope.launch { showError(error) }
          }
        })
    }
  }

  @Suppress("DEPRECATION")
  fun createSwitchAccountIntent(): Intent {
    cleanWebCookies()
    return if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
      AccountManager.newChooseAccountIntent(
        ownerStorage.getActiveOwner(authenticator.getOwnerType()),
        null,
        arrayOf(authenticator.getOwnerType()),
        true,
        null,
        null,
        null,
        null
      )
    } else {
      AccountManager.newChooseAccountIntent(
        ownerStorage.getActiveOwner(authenticator.getOwnerType()),
        null,
        arrayOf(authenticator.getOwnerType()),
        null,
        null,
        null,
        null
      )
    }
  }

  fun logout() {
    ownerStorage.getActiveOwner(authenticator.getOwnerType())?.let { account ->
      ownerStorage.removeOwner(account.type, account, object : Callback<Boolean> {
        override fun onResult(result: Boolean) {
          if (result) _state.offer(ViewState.LogoutSuccess)
        }

        override fun onError(error: Throwable) {
          _state.offer(ViewState.Error(error))
        }
      })
    }
    cleanWebCookies()
  }


  private fun cleanWebCookies() {
    /** remove all cookies to avoid an automatic re-login within the webview.  */
    CookieManager.getInstance().removeAllCookies(null)
  }

  sealed class ViewState {
    object InitialState : ViewState()
    data class LoginSuccess(val account: Account) : ViewState()
    object LogoutSuccess : ViewState()
    data class Error(val throwable: Throwable) : ViewState()
    data class RepositoryUpdate(val repos: List<GithubApi.Repository>) : ViewState()
  }
}
