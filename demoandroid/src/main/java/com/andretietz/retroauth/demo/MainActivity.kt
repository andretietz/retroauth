package com.andretietz.retroauth.demo

import android.os.Build
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.webkit.CookieManager
import android.widget.Toast
import com.andretietz.retroauth.AndroidOwnerManager
import com.andretietz.retroauth.AndroidToken
import com.andretietz.retroauth.AndroidTokenStorage
import com.andretietz.retroauth.RetroauthAndroidBuilder
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.activity_main.buttonInvalidateToken
import kotlinx.android.synthetic.main.activity_main.buttonLogout
import kotlinx.android.synthetic.main.activity_main.buttonRequestEmail
import kotlinx.android.synthetic.main.activity_main.buttonSwitch
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory
import retrofit2.converter.moshi.MoshiConverterFactory
import timber.log.Timber

class MainActivity : AppCompatActivity() {
    private lateinit var service: FacebookService

    private val ownerManager by lazy { AndroidOwnerManager(application) }
    private val tokenStorage by lazy { AndroidTokenStorage(application) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        Timber.plant(Timber.DebugTree())

        /**
         * Optional: create your own OkHttpClient
         */
        val interceptor = HttpLoggingInterceptor()
        interceptor.level = HttpLoggingInterceptor.Level.BODY
        val httpClient = OkHttpClient.Builder()
                .addInterceptor(interceptor)
                .build()

        val provider = ProviderFacebook(application)

        /**
         * Create your Retrofit Object using the [RetroauthAndroidBuilder.createBuilder]
         */
        val retrofit = RetroauthAndroidBuilder.createBuilder(application, provider)
                .baseUrl("https://graph.facebook.com/")
                .client(httpClient)
                .addConverterFactory(MoshiConverterFactory.create())
                .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
                .build()

        /**
         * Create your API Service
         */
        service = retrofit.create(FacebookService::class.java)

        buttonRequestEmail.setOnClickListener {
            service.getUserDetails()
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(
                            { item -> show(item.toString()) },
                            { error -> showError(error) }
                    )
        }

        buttonInvalidateToken.setOnClickListener {
            ownerManager.getActiveOwner(provider.ownerType)?.let { account ->
                val token = tokenStorage.getToken(account, provider.tokenType)
                tokenStorage.storeToken(
                        account,
                        provider.tokenType,
                        AndroidToken("some-invalid-token", token.data)
                )
            }
        }

        buttonLogout.setOnClickListener {
            ownerManager.getActiveOwner(provider.ownerType)?.let { account ->
                val token = tokenStorage.getToken(account, provider.tokenType)
                tokenStorage.removeToken(account, provider.tokenType, token)
            }
            /** remove all cookies to avoid an automatic relogin */
            val cookieManager = CookieManager.getInstance()
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
                @Suppress("DEPRECATION")
                cookieManager.removeAllCookie()
            } else {
                cookieManager.removeAllCookies(null)
            }
        }

        buttonSwitch.setOnClickListener {
            ownerManager.switchActiveOwner(provider.ownerType)
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
