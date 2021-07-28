package com.andretietz.retroauth.demo.screen.main

import android.accounts.Account
import androidx.lifecycle.ViewModel
import com.andretietz.retroauth.AndroidAccountManagerCredentialStorage
import com.andretietz.retroauth.AndroidAccountManagerOwnerStorage
import com.andretietz.retroauth.AndroidCredentials
import com.andretietz.retroauth.AuthenticationRequiredException
import com.andretietz.retroauth.Callback
import com.andretietz.retroauth.demo.api.GithubApi
import com.andretietz.retroauth.demo.auth.GithubAuthenticator
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.CoroutineName
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import retrofit2.Retrofit
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(
  retrofit: Retrofit,
  private val ownerStorage: AndroidAccountManagerOwnerStorage,
  private val credentialStorage: AndroidAccountManagerCredentialStorage,
  private val authenticator: GithubAuthenticator
) : ViewModel() {

  private val _state = MutableStateFlow<MainViewState>(MainViewState.InitialState)

  private val scope = CoroutineScope(Dispatchers.Default + CoroutineName("ViewModelScope"))

  private val errorHandler = CoroutineExceptionHandler { _, error ->
    _state.value = MainViewState.Error(error)
  }

  val state = _state

  private val api = retrofit.create(GithubApi::class.java)

  fun addAccount() {
//    cleanWebCookies()
    ownerStorage.createOwner(
      authenticator.getOwnerType(),
      authenticator.getCredentialType(),
      object : Callback<Account> {
        override fun onResult(result: Account) {
//          Timber.d("Logged in: $result")
          _state.value = MainViewState.InitialState
          _state.value = MainViewState.LoginSuccess(result)
        }

        override fun onError(error: Throwable) {
          _state.value = MainViewState.Error(error)
        }
      })
  }

  fun loadRepositories() {
    scope.launch(Dispatchers.IO + errorHandler) {
      try {
        _state.value = MainViewState.RepositoryUpdate(api.getRepositories(), System.currentTimeMillis())
//        Timber.e("result")
      } catch (error: AuthenticationRequiredException) {
//        Timber.e(error, "result")
        _state.value = MainViewState.InitialState
//        withContext(Dispatchers.Main) {
//        }
//        Timber.i("User not logged in! Opening Login Screen")
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
            _state.value = MainViewState.Error(error)
          }
        })
    }
  }

  fun logout() {
    ownerStorage.getActiveOwner(authenticator.getOwnerType())?.let { account ->
      ownerStorage.removeOwner(authenticator.getOwnerType(), account, object : Callback<Boolean> {
        override fun onResult(result: Boolean) {
          if (result) _state.value = MainViewState.LogoutSuccess
        }

        override fun onError(error: Throwable) {
          _state.value = MainViewState.Error(error)
        }
      })
    }
  }

  fun getCurrentAccount() = ownerStorage.getActiveOwner(authenticator.getOwnerType())

}
