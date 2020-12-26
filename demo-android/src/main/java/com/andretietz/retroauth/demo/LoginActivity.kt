package com.andretietz.retroauth.demo

import android.annotation.SuppressLint
import android.os.Bundle
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.lifecycle.lifecycleScope
import com.andretietz.retroauth.AndroidCredentials
import com.andretietz.retroauth.AuthenticationActivity
import com.github.scribejava.apis.FacebookApi
import com.github.scribejava.core.builder.ServiceBuilder
import com.github.scribejava.core.model.OAuth2AccessToken
import com.github.scribejava.httpclient.okhttp.OkHttpHttpClient
import kotlinx.android.synthetic.main.activity_login.webView
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import retrofit2.http.GET
import retrofit2.http.Query
import java.util.concurrent.TimeUnit

class LoginActivity : AuthenticationActivity() {

  private val helper = ServiceBuilder(FacebookAuthenticator.CLIENT_ID)
    .apiSecret(FacebookAuthenticator.CLIENT_SECRET)
    .callback(FacebookAuthenticator.CLIENT_CALLBACK)
    .httpClient(OkHttpHttpClient())
    .defaultScope("email")
    .build(FacebookApi.instance())

  private val api = Retrofit.Builder()
    .baseUrl("https://graph.facebook.com/")
    .addConverterFactory(MoshiConverterFactory.create())
    .build().create(FacebookInfoService::class.java)

  @SuppressLint("SetJavaScriptEnabled")
  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setContentView(R.layout.activity_login)

    webView.loadUrl(helper.authorizationUrl)
    webView.settings.javaScriptEnabled = true
    webView.webViewClient = object : WebViewClient() {
      override fun shouldOverrideUrlLoading(view: WebView, url: String): Boolean {
        val authorization = helper.extractAuthorization(url)
        val code = authorization.code
        if (code == null) {
          view.loadUrl(url)
        } else {
          lifecycleScope.launch(Dispatchers.IO) {
            val token = helper.getAccessToken(code)
            val userInfo = api.getUserInfo("name,email", token.accessToken)
            withContext(Dispatchers.Main) {
              val account = createOrGetAccount(userInfo.email)
              storeCredentials(
                account,
                FacebookAuthenticator.createTokenType(application),
                token.toAndroidCredentials()
              )
              finalizeAuthentication(account)
            }
          }
        }
        return true
      }
    }
  }

  /**
   * Since we don't have a stored token yet, we cannot use the annotation.
   * This is an exception for the login screen only.
   */
  internal interface FacebookInfoService {
    @GET("v5.0/me")
    suspend fun getUserInfo(@Query("fields") fields: String, @Query("access_token") token: String): UserInfo

    data class UserInfo(val email: String)
  }

  fun OAuth2AccessToken.toAndroidCredentials(): AndroidCredentials {
    return AndroidCredentials(this.accessToken, mapOf(
      FacebookAuthenticator.KEY_TOKEN_VALIDITY
        to TimeUnit.MILLISECONDS
        // expiry date - 30 seconds (network tolerance)
        .convert((this.expiresIn - 30).toLong(), TimeUnit.SECONDS)
        .plus(System.currentTimeMillis()).toString()
    ))
  }
}
