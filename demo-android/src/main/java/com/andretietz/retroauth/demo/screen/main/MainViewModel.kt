package com.andretietz.retroauth.demo.screen.main

import androidx.lifecycle.ViewModel
import com.andretietz.retroauth.AndroidAccountManagerCredentialStorage
import com.andretietz.retroauth.AndroidAccountManagerOwnerStorage
import com.andretietz.retroauth.AuthenticationCanceledException
import com.andretietz.retroauth.AuthenticationRequiredException
import com.andretietz.retroauth.Credentials
import com.andretietz.retroauth.demo.api.GithubApi
import com.andretietz.retroauth.demo.auth.GithubAuthenticator
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineName
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(
  private val api: GithubApi,
  private val ownerStorage: AndroidAccountManagerOwnerStorage,
  private val credentialStorage: AndroidAccountManagerCredentialStorage,
  private val authenticator: GithubAuthenticator
) : ViewModel() {

  private val _state = MutableStateFlow<MainViewState>(MainViewState.InitialState)

  private val scope = CoroutineScope(Dispatchers.Default + CoroutineName("ViewModelScope"))

  val state = _state

  init {
      scope.launch {
        loadRepositories()
      }
  }

  fun addAccount() {
    scope.launch {
      val account = ownerStorage.createOwner(
        authenticator.getCredentialType())
      if (account == null) {
        _state.value = MainViewState.Error(AuthenticationCanceledException())
      } else {
        Timber.d("Logged in: $account")
        _state.value = MainViewState.InitialState
        _state.value = MainViewState.LoginSuccess(account)
      }
    }
  }

  fun loadRepositories() = scope.launch(Dispatchers.IO) {
    try {
      _state.value = MainViewState.RepositoryUpdate(api.getRepositories())
    } catch (error: AuthenticationRequiredException) {
      _state.value = MainViewState.InitialState
    }
  }

  fun invalidateTokens() {
    ownerStorage.getActiveOwner()?.let { account ->
      val credential = credentialStorage.getCredentials(
        account,
        authenticator.getCredentialType())
      if (credential == null) {
        _state.value = MainViewState.Error(AuthenticationCanceledException())
      } else {
        credentialStorage.storeCredentials(
          account,
          authenticator.getCredentialType(),
          Credentials("some-invalid-token", credential.data)
        )
      }
    }
  }

  fun logout() = scope.launch(Dispatchers.Default) {
    ownerStorage.getActiveOwner()?.let { account ->
      if (ownerStorage.removeOwner(account)) {
        _state.value = MainViewState.LogoutSuccess
      } else {
        _state.value = MainViewState.Error(UnknownError())
      }
    }
  }

  fun getCurrentAccount() = ownerStorage.getActiveOwner()

}
