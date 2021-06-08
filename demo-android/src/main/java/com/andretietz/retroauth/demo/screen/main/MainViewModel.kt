package com.andretietz.retroauth.demo.screen.main

import com.andretietz.retroauth.*
import com.andretietz.retroauth.demo.api.GithubApi
import com.andretietz.retroauth.demo.screen.main.MainViewModel.ViewState
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import okhttp3.Cache
import retrofit2.Retrofit

interface MainViewModel<OWNER : Any> {
  val state: StateFlow<ViewState<OWNER>>

  fun addAccount()
  fun loadRepositories()
  fun invalidateTokens()
  fun logout()


  sealed class ViewState<out OWNER> {
    object InitialState : ViewState<Nothing>()
    data class LoginSuccess<OWNER>(val account: OWNER) : ViewState<OWNER>()
    object LogoutSuccess : ViewState<Nothing>()
    data class Error(val throwable: Throwable) : ViewState<Nothing>()
    data class RepositoryUpdate(val repos: List<GithubApi.Repository>) : ViewState<Nothing>()
  }
}

class CommonMainViewModel<CREDENTIAL_TYPE : Any, CREDENTIAL : Any, OWNER_TYPE : Any, OWNER : Any>(
  retrofit: Retrofit,
  private val ownerStorage: OwnerStorage<OWNER_TYPE, OWNER, CREDENTIAL_TYPE>,
  private val credentialStorage: CredentialStorage<OWNER, CREDENTIAL_TYPE, CREDENTIAL>,
  private val authenticator: Authenticator<OWNER_TYPE, OWNER, CREDENTIAL_TYPE, CREDENTIAL>,
  private val cache: Cache,
  private val scope: CoroutineScope
) : MainViewModel<OWNER> {

  private val _state = MutableStateFlow<ViewState<OWNER>>(ViewState.InitialState)

  private val errorHandler = CoroutineExceptionHandler { _, error ->
    _state.value = ViewState.Error(error)
  }

  override val state = _state.asStateFlow()

  private val api = retrofit.create(GithubApi::class.java)

  override fun addAccount() {
//    cleanWebCookies()
    ownerStorage.createOwner(
      authenticator.getOwnerType(),
      authenticator.getCredentialType(),
      object : Callback<OWNER> {
        override fun onResult(result: OWNER) {
//          Timber.d("Logged in: $result")
          _state.value = ViewState.InitialState
          _state.value = ViewState.LoginSuccess(result)
        }

        override fun onError(error: Throwable) {
          _state.value = ViewState.Error(error)
        }
      })
  }

  override fun loadRepositories() {
    scope.launch(Dispatchers.IO + errorHandler) {
      try {
        _state.value = ViewState.RepositoryUpdate(api.getRepositories())
//        Timber.e("result")
      } catch (error: AuthenticationRequiredException) {
//        Timber.e(error, "result")
        _state.value = ViewState.InitialState
//        withContext(Dispatchers.Main) {
//        }
//        Timber.i("User not logged in! Opening Login Screen")
      }
    }
  }

  override fun invalidateTokens() {
    ownerStorage.getActiveOwner(authenticator.getOwnerType())?.let { account ->
      credentialStorage.getCredentials(
        account,
        authenticator.getCredentialType(),
        object : Callback<CREDENTIAL> {
          override fun onResult(result: CREDENTIAL) {
//            credentialStorage.storeCredentials(
//              account,
//              authenticator.getCredentialType(),
//              AndroidCredentials("some-invalid-token", result.data)
//            )
          }

          override fun onError(error: Throwable) {
            _state.value = ViewState.Error(error)
          }
        })
    }
  }

  override fun logout() {
    ownerStorage.getActiveOwner(authenticator.getOwnerType())?.let { account ->
      ownerStorage.removeOwner(authenticator.getOwnerType(), account, object : Callback<Boolean> {
        override fun onResult(result: Boolean) {
          if (result) _state.value = ViewState.LogoutSuccess
        }

        override fun onError(error: Throwable) {
          _state.value = ViewState.Error(error)
        }
      })
    }
    cache.delete()
  }

}
