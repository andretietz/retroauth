package com.andretietz.retroauth.demo

import com.andretietz.retroauth.AuthenticationService

class DemoAuthenticationService : AuthenticationService() {
    override fun getLoginAction(): String = getString(R.string.authentication_ACTION)
}