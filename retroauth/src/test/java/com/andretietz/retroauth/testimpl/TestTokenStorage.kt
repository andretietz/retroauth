package com.andretietz.retroauth.testimpl

import com.andretietz.retroauth.TokenStorage

open class TestTokenStorage : TokenStorage<String, String, String> {

    companion object {
        const val TEST_TOKEN = "token"
    }

    private var tmpToken: String? = null


    override fun getToken(owner: String, type: String): String {
        return tmpToken ?: TEST_TOKEN
    }

    override fun removeToken(owner: String, type: String, token: String) {

    }

    override fun storeToken(owner: String, type: String, token: String): String {
        tmpToken = token
        return token
    }
}
