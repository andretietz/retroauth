package com.andretietz.retroauth.demo

import android.os.Build
import android.os.Bundle
import android.webkit.CookieManager
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.andretietz.retroauth.AndroidCredentialStorage
import com.andretietz.retroauth.AndroidCredentials
import com.andretietz.retroauth.AndroidOwnerStorage
import com.andretietz.retroauth.Callback
import com.andretietz.retroauth.RetroauthAndroid
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

  private val ownerManager by lazy { AndroidOwnerStorage(application) }
  private val credentialStorage by lazy { AndroidCredentialStorage(application) }

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

    val provider = FacebookAuthenticator(application)

    /**
     * Create your Retrofit Object using the [RetroauthAndroid.createBuilder]
     */
    val retrofit = RetroauthAndroid.createBuilder(application, provider)
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
        credentialStorage.getCredentials(account, provider.credentialType, object : Callback<AndroidCredentials> {
          override fun onResult(result: AndroidCredentials) {
            credentialStorage.storeCredentials(
              account,
              provider.credentialType,
              AndroidCredentials("some-invalid-token", result.data))
          }

          override fun onError(error: Throwable) {
            showError(error)
          }
        })
      }
    }

    buttonLogout.setOnClickListener {
      ownerManager.getActiveOwner(provider.ownerType)?.let { account ->
        ownerManager.removeOwner(account.type, account, object : Callback<Boolean> {
          override fun onResult(result: Boolean) {
            show("Logged out: $result")
          }

          override fun onError(error: Throwable) {
            showError(error)
          }
        })
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
