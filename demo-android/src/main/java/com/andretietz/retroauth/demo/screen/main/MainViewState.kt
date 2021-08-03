package com.andretietz.retroauth.demo.screen.main

import com.andretietz.retroauth.demo.api.GithubApi

sealed class MainViewState {
  object InitialState : MainViewState()
  data class LoginSuccess<OWNER>(val account: OWNER) : MainViewState()
  object LogoutSuccess : MainViewState()
  data class Error(val throwable: Throwable) : MainViewState()
  class RepositoryUpdate(
    val repos: List<GithubApi.Repository>
  ) : MainViewState()
}
