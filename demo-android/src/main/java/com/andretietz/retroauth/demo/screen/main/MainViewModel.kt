package com.andretietz.retroauth.demo.screen.main

import androidx.lifecycle.ViewModel
import com.andretietz.retroauth.AndroidAccountManagerCredentialStorage
import com.andretietz.retroauth.AndroidAccountManagerOwnerStorage
import com.andretietz.retroauth.AndroidCredential
import com.andretietz.retroauth.AuthenticationCanceledException
import com.andretietz.retroauth.AuthenticationRequiredException
import com.andretietz.retroauth.demo.api.GithubApi
import com.andretietz.retroauth.demo.auth.GithubAuthenticator
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineName
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import retrofit2.Retrofit
import timber.log.Timber
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

//  private val errorHandler = CoroutineExceptionHandler { _, error ->
//    _state.value = MainViewState.Error(error)
//  }

  val state = _state

  private val api = retrofit.create(GithubApi::class.java)

  fun addAccount() {
    scope.launch {
      val account = ownerStorage.createOwner(
        authenticator.getOwnerType(),
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
    ownerStorage.getActiveOwner(authenticator.getOwnerType())?.let { account ->
      val credential = credentialStorage.getCredentials(
        account,
        authenticator.getCredentialType())
      if (credential == null) {
        _state.value = MainViewState.Error(AuthenticationCanceledException())
      } else {
        credentialStorage.storeCredentials(
          account,
          authenticator.getCredentialType(),
          AndroidCredential("some-invalid-token", credential.data)
        )
      }
    }
  }

  fun logout() = scope.launch(Dispatchers.Default) {
    ownerStorage.getActiveOwner(authenticator.getOwnerType())?.let { account ->
      if (ownerStorage.removeOwner(authenticator.getOwnerType(), account)) {
        _state.value = MainViewState.LogoutSuccess
      } else {
        _state.value = MainViewState.Error(UnknownError())
      }
    }
  }

  fun getCurrentAccount() = ownerStorage.getActiveOwner(authenticator.getOwnerType())

}
