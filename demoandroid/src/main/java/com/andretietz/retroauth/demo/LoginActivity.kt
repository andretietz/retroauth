package com.andretietz.retroauth.demo

import android.annotation.TargetApi
import android.os.Build
import android.os.Bundle
import android.webkit.WebResourceRequest
import android.webkit.WebView
import android.webkit.WebViewClient
import com.andretietz.retroauth.AndroidToken
import com.andretietz.retroauth.AuthenticationActivity
import com.github.scribejava.apis.FacebookApi
import com.github.scribejava.core.builder.ServiceBuilder
import com.github.scribejava.core.model.OAuth2AccessToken
import com.github.scribejava.core.oauth.OAuth20Service
import com.github.scribejava.httpclient.okhttp.OkHttpHttpClient
import io.reactivex.Single
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.activity_login.webView
import retrofit2.Retrofit
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory
import retrofit2.converter.moshi.MoshiConverterFactory
import retrofit2.http.GET
import retrofit2.http.Query
import timber.log.Timber
import java.util.concurrent.Callable
import java.util.concurrent.TimeUnit

class LoginActivity : AuthenticationActivity() {
  private val compositeDisposable = CompositeDisposable()
  private val helper = ServiceBuilder(FacebookAuthenticator.CLIENT_ID)
    .apiSecret(FacebookAuthenticator.CLIENT_SECRET)
    .callback(FacebookAuthenticator.CLIENT_CALLBACK)
    .withScope("email")
    .httpClient(OkHttpHttpClient())
    .build(FacebookApi.instance())

  private val authenticator by lazy { FacebookAuthenticator(application) }

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setContentView(R.layout.activity_login)
    Timber.plant(Timber.DebugTree())

    webView.loadUrl(helper.authorizationUrl)
    @Suppress("UsePropertyAccessSyntax")
    webView.getSettings().setJavaScriptEnabled(true)
    webView.webViewClient = object : WebViewClient() {
      @Suppress("OverridingDeprecatedMember")
      override fun shouldOverrideUrlLoading(view: WebView, url: String): Boolean {
        val authorization = helper.extractAuthorization(url)
        val code = authorization.code
        if (code == null) {
          view.loadUrl(url)
        } else {
          val disposable = Single.fromCallable(TokenVerifier(helper, code))
            .subscribeOn(Schedulers.io())
            .subscribe({ result ->
              val account = createOrGetAccount(result.name)
              val expiryDate = TimeUnit.MILLISECONDS
                // expiry date - 30 seconds (network tolerance)
                .convert((result.token.expiresIn - 30).toLong(), TimeUnit.SECONDS)
                .plus(System.currentTimeMillis())
              storeToken(
                account,
                authenticator.tokenType,
                result.token.toAndroidToken()
              )
              finalizeAuthentication(account)
            }, { error -> Timber.e(error) })
          compositeDisposable.add(disposable)
        }
        return true
      }

      @Suppress("DEPRECATION")
      @TargetApi(Build.VERSION_CODES.N)
      override fun shouldOverrideUrlLoading(view: WebView, request: WebResourceRequest): Boolean {
        return shouldOverrideUrlLoading(view, request.url.toString())
      }
    }
  }

  override fun onDestroy() {
    compositeDisposable.dispose()
    super.onDestroy()
  }

  private class TokenVerifier(private val service: OAuth20Service, private val code: String) : Callable<LoginResult> {
    private val api = Retrofit.Builder()
      .baseUrl("https://graph.facebook.com/")
      .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
      .addConverterFactory(MoshiConverterFactory.create())
      .build().create(FacebookInfoService::class.java)

    override fun call(): LoginResult {
      val token = service.getAccessToken(code)
      val info = api.getUserInfo("name,email", token.accessToken).blockingGet()
      return LoginResult(info.email, token)
    }
  }

  internal interface FacebookInfoService {
    @GET("v2.11/me")
    fun getUserInfo(@Query("fields") fields: String, @Query("access_token") token: String): Single<UserInfo>
  }

  internal class UserInfo {
    var name: String = ""
    var email: String = ""
  }

  private class LoginResult(val name: String, val token: OAuth2AccessToken)

  fun OAuth2AccessToken.toAndroidToken(): AndroidToken {
    return AndroidToken(this.accessToken, mapOf(
      FacebookAuthenticator.KEY_TOKEN_VALIDITY
        to TimeUnit.MILLISECONDS
        // expiry date - 30 seconds (network tolerance)
        .convert((this.expiresIn - 30).toLong(), TimeUnit.SECONDS)
        .plus(System.currentTimeMillis()).toString()
    ))

  }
}
