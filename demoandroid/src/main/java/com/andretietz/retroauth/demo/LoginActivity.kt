package com.andretietz.retroauth.demo

import android.annotation.TargetApi
import android.os.Build
import android.os.Build.VERSION
import android.os.Build.VERSION_CODES
import android.os.Bundle
import android.webkit.CookieManager
import android.webkit.WebResourceRequest
import android.webkit.WebView
import android.webkit.WebViewClient
import com.andretietz.retroauth.AuthenticationActivity
import com.github.scribejava.apis.FacebookApi
import com.github.scribejava.core.builder.ServiceBuilder
import com.github.scribejava.core.model.OAuth2AccessToken
import com.github.scribejava.core.oauth.OAuth20Service
import com.github.scribejava.httpclient.okhttp.OkHttpHttpClient
import io.reactivex.Single
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.activity_login.webView
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory
import retrofit2.converter.moshi.MoshiConverterFactory
import retrofit2.http.GET
import retrofit2.http.Query
import timber.log.Timber
import java.util.concurrent.Callable

class LoginActivity : AuthenticationActivity() {

    private val helper = ServiceBuilder(ProviderFacebook.CLIENT_ID)
            .apiSecret(ProviderFacebook.CLIENT_SECRET)
            .callback(ProviderFacebook.CLIENT_CALLBACK)
            .scope("email")
            .httpClient(OkHttpHttpClient(client()))
            .build(FacebookApi.instance())

    private fun client(): OkHttpClient {
        val interceptor = HttpLoggingInterceptor()
        interceptor.level = HttpLoggingInterceptor.Level.BODY
        return OkHttpClient.Builder()
                .addInterceptor(interceptor)
                .build()
    }

    override fun onCreate(icicle: Bundle?) {
        super.onCreate(icicle)
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
                    Single.fromCallable(TokenVerifier(helper, code))
                            .subscribeOn(Schedulers.io())
                            .subscribe({ result ->
                                val account = createOrGetAccount(result.name)
                                storeToken(
                                        account,
                                        getRequestedTokenType()!!,
                                        result.token.accessToken,
                                        mapOf("expiringIn" to result.token.expiresIn.toString()))
                                finalizeAuthentication(account)
                            },
                                    { error -> Timber.e(error) })

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

//    override fun finish() {
//        val cookieManager = CookieManager.getInstance()
//        if (VERSION.SDK_INT < VERSION_CODES.LOLLIPOP) {
//            @Suppress("DEPRECATION")
//            cookieManager.removeAllCookie()
//        } else {
//            cookieManager.removeAllCookies(null)
//        }
//        super.finish()
//    }

    private class TokenVerifier(private val service: OAuth20Service, private val code: String)
        : Callable<LoginResult> {
        private val api = Retrofit.Builder()
                .baseUrl("https://graph.facebook.com/")
                .client(client())
                .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
                .addConverterFactory(MoshiConverterFactory.create())
                .build().create(FacebookInfoService::class.java)

        override fun call(): LoginResult {
            val token = service.getAccessToken(code)
            val info = api.getUserInfo("name,email", token.accessToken).blockingGet()
            return LoginResult(info.email, token)
        }

        companion object {
            @JvmStatic
            internal fun client(): OkHttpClient {
                val interceptor = HttpLoggingInterceptor()
                interceptor.level = HttpLoggingInterceptor.Level.BODY
                return OkHttpClient.Builder()
                        .addInterceptor(interceptor)
                        .build()
            }
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
}
