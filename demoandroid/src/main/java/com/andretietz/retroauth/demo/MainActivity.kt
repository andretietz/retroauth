package com.andretietz.retroauth.demo

import android.accounts.AccountManager
import android.os.Build
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.webkit.CookieManager
import android.widget.Toast
import com.andretietz.retroauth.AndroidAuthenticationHandler
import com.andretietz.retroauth.AndroidTokenType
import com.andretietz.retroauth.AuthAccountManager
import com.andretietz.retroauth.Retroauth
import com.andretietz.retroauth.TokenTypeFactory
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.activity_main.buttonInvalidateToken
import kotlinx.android.synthetic.main.activity_main.buttonLogout
import kotlinx.android.synthetic.main.activity_main.buttonRequestEmail
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory
import retrofit2.converter.moshi.MoshiConverterFactory
import timber.log.Timber

class MainActivity : AppCompatActivity() {
    private lateinit var service: FacebookService
    private lateinit var authAccountManager: AuthAccountManager
    private lateinit var accountManager: AccountManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        Timber.plant(Timber.DebugTree())

        authAccountManager = AuthAccountManager(application)
        accountManager = AccountManager.get(this)

        /**
         * Optional: create your own OkHttpClient
         */
        val interceptor = HttpLoggingInterceptor()
        interceptor.level = HttpLoggingInterceptor.Level.BODY
        val httpClient = OkHttpClient.Builder()
                .addInterceptor(interceptor)
                .build()

        val provider = ProviderFacebook()

        /**
         * Create your Retrofit Object using the [Retroauth.Builder]
         */
        val retrofit = Retroauth.Builder(
                AndroidAuthenticationHandler.create(application, provider, object : TokenTypeFactory<AndroidTokenType> {
                    override fun create(annotationValues: IntArray): AndroidTokenType =
                            AndroidTokenType(
                                    getString(R.string.com_andretietz_retroauth_authentication_ACCOUNT),
                                    getString(R.string.com_andretietz_retroauth_authentication_TOKEN))
                }))
                .baseUrl("https://graph.facebook.com/")
                .client(httpClient)
                .addConverterFactory(MoshiConverterFactory.create())
                .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
                .build()

        //        provider.onRetrofitCreated(retrofit);

        /**
         * Create your API Service
         */
        service = retrofit.create(FacebookService::class.java)


        buttonRequestEmail.setOnClickListener {
            /**
             * Use it!
             */
            service.getUserDetails()
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(
                            { item -> show(item.toString()) },
                            { error -> showError(error) }
                    )
        }

        buttonInvalidateToken.setOnClickListener {
            authAccountManager
                    .getActiveAccount(getString(R.string.com_andretietz_retroauth_authentication_ACCOUNT))
                    ?.let {
                        // This is for demo purposes only. We're "manually" setting some-invalid-token.
                        accountManager.setAuthToken(it,
                                getString(R.string.com_andretietz_retroauth_authentication_TOKEN), "some-invalid-token")
                    }
        }

        buttonLogout.setOnClickListener {
            authAccountManager.removeActiveAccount(getString(R.string.com_andretietz_retroauth_authentication_ACCOUNT))
            /** remove all cookies to avoid an automatic relogin */
            val cookieManager = CookieManager.getInstance()
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
                @Suppress("DEPRECATION")
                cookieManager.removeAllCookie()
            } else {
                cookieManager.removeAllCookies(null)
            }
        }
    }

    private fun show(toShow: String) {
        Toast.makeText(this, toShow, Toast.LENGTH_SHORT).show()
    }

    private fun showError(error: Throwable) {
        show(error.toString())
    }
}
