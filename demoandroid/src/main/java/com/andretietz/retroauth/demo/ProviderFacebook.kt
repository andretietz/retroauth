package com.andretietz.retroauth.demo

import android.accounts.Account

import com.andretietz.retroauth.AndroidToken
import com.andretietz.retroauth.AndroidTokenType
import com.andretietz.retroauth.TokenProvider
import com.andretietz.retroauth.TokenStorage

import okhttp3.Request
import okhttp3.Response

class ProviderFacebook : TokenProvider<Account, AndroidTokenType, AndroidToken> {
    override fun retryRequired(count: Int, response: Response, tokenStorage: TokenStorage<Account, AndroidTokenType, AndroidToken>, owner: Account, type: AndroidTokenType, token: AndroidToken): Boolean {
        if (response.code() == 401) {
            // invalidate token
            tokenStorage.removeToken(owner!!, type, token)
            return true
        }
        return false
    }

    override fun authenticateRequest(request: Request, token: AndroidToken): Request {
        return request.newBuilder()
                .header("Authorization", "Bearer " + token.token)
                .build()
    }

//    override fun retryRequired(count: Int,
//                               response: Response,
//                               tokenStorage: TokenStorage<Account, AndroidTokenType, AndroidToken>,
//                               owner: Account?,
//                               type: AndroidTokenType,
//                               token: AndroidToken): Boolean {
//        if (response.code() == 401) {
//            // invalidate token
//            tokenStorage.removeToken(owner!!, type, token)
//            return true
//        }
//        return false
//    }

    companion object {
        const val CLIENT_ID = "908466759214667"
        const val CLIENT_SECRET = "6254dc69ac506d95b897d35d0dcf9e1f"
        const val CLIENT_CALLBACK = "http://localhost:8000/accounts/facebook/login/callback/"
    }
}
