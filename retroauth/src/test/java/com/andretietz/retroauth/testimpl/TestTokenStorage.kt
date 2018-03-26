package com.andretietz.retroauth.testimpl

import com.andretietz.retroauth.TokenStorage

class TestTokenStorage : TokenStorage<String, String, String> {

    companion object {
        val TEST_TOKEN = "token"
    }

    private var behaviour: TestBehaviour? = null

    override fun getToken(owner: String, type: String): String {
        return behaviour?.getToken(owner, type) ?: TEST_TOKEN
    }

    override fun removeToken(owner: String, type: String, token: String) {

    }

    override fun storeToken(owner: String, type: String, token: String) {

    }

    fun setTestBehaviour(behaviour: TestBehaviour) {
        this.behaviour = behaviour
    }

    interface TestBehaviour {
        fun getToken(owner: String, tokenType: String): String
    }
}
