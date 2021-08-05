package com.andretietz.retroauth.demo.auth

import android.annotation.SuppressLint
import android.os.Bundle
import android.webkit.CookieManager
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.lifecycle.lifecycleScope
import com.andretietz.retroauth.AuthenticationActivity
import com.andretietz.retroauth.demo.databinding.ActivityLoginBinding
import com.github.scribejava.core.oauth.OAuth20Service
import com.squareup.moshi.JsonClass
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import retrofit2.http.GET
import retrofit2.http.Header
import javax.inject.Inject

@AndroidEntryPoint
class LoginActivity : AuthenticationActivity() {

  @Inject
  lateinit var helper: OAuth20Service

  @Inject
  lateinit var api: SignInApi

  @SuppressLint("SetJavaScriptEnabled")
  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    CookieManager.getInstance().removeAllCookies { }
    val views = ActivityLoginBinding.inflate(layoutInflater)
    setContentView(views.root)
    views.webView.loadUrl(helper.authorizationUrl)
    views.webView.settings.javaScriptEnabled = true
    views.webView.webViewClient = object : WebViewClient() {
      override fun shouldOverrideUrlLoading(view: WebView, url: String): Boolean {
        val authorization = helper.extractAuthorization(url)
        val code = authorization.code
        if (code == null) {
          view.loadUrl(url)
        } else {
          lifecycleScope.launch(Dispatchers.IO) {
            val token = helper.getAccessToken(code)
            val userInfo = api.getUser("Bearer ${token.accessToken}")
            withContext(Dispatchers.Main) {
              val account = createOrGetAccount(userInfo.login)
              storeCredentials(
                account,
                GithubAuthenticator.createTokenType(application),
                AndroidCredential(token.accessToken)
              )
//              storeUserData(account, "email", userInfo.email)
              finalizeAuthentication(account)
            }
          }
        }
        return true
      }
    }
  }

  interface SignInApi {
    /**
     * We call this method right after authentication in order to get user information
     * to store within the account. At this point of time, the account and it's token isn't stored
     * yet, why we cannot use the annotation.
     *
     * https://docs.github.com/en/rest/reference/users#get-the-authenticated-user
     */
    @GET("user")
    suspend fun getUser(@Header("Authorization") token: String): User

    @JsonClass(generateAdapter = true)
    data class User(
      val login: String
    )
  }
}
