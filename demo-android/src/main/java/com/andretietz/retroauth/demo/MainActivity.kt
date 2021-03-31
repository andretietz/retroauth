package com.andretietz.retroauth.demo

import android.accounts.Account
import android.accounts.AccountManager
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.webkit.CookieManager
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.andretietz.retroauth.*
import com.andretietz.retroauth.demo.databinding.ActivityMainBinding
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import timber.log.Timber

class MainActivity : AppCompatActivity() {

  companion object {
    const val ACCOUNT_CHOOSER_REQUESTCODE = 0x123
  }

  private lateinit var service: FacebookService

  private val ownerManager by lazy { AndroidOwnerStorage(application) }
  private val credentialStorage by lazy { AndroidCredentialStorage(application) }

  private lateinit var views: ActivityMainBinding

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setContentView(R.layout.activity_main)
    views = ActivityMainBinding.inflate(layoutInflater)
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
     * Create your Retrofit Object using the [Retrofit.androidAuthentication]
     */
    val retrofit = Retrofit.Builder()
      .baseUrl("https://graph.facebook.com/")
      .client(httpClient)
      .addConverterFactory(MoshiConverterFactory.create())
      .build()
      .androidAuthentication(application, provider)

    /**
     * Create your API Service
     */
    service = retrofit.create(FacebookService::class.java)

    views.buttonAddAccount.setOnClickListener {
      cleanWebCookies()
      ownerManager.createOwner(
        provider.ownerType,
        provider.credentialType,
        object : Callback<Account> {
          override fun onResult(result: Account) {
            Timber.d("Logged in: $result")
          }

          override fun onError(error: Throwable) {
            lifecycleScope.launch { showError(error) }
          }
        })
    }

    views.buttonRequestEmail.setOnClickListener {
      lifecycleScope.launch(Dispatchers.IO) {
        val user = service.getUserDetails().toString()
        show(user)
      }
    }

    views.buttonInvalidateToken.setOnClickListener {
      ownerManager.getActiveOwner(provider.ownerType)?.let { account ->
        credentialStorage.getCredentials(
          account,
          provider.credentialType,
          object : Callback<AndroidCredentials> {
            override fun onResult(result: AndroidCredentials) {
              credentialStorage.storeCredentials(
                account,
                provider.credentialType,
                AndroidCredentials("some-invalid-token", result.data)
              )
            }

            override fun onError(error: Throwable) {
              lifecycleScope.launch { showError(error) }
            }
          })
      }
    }

    @Suppress("DEPRECATION")
    views.buttonSwitchAccount.setOnClickListener {
      cleanWebCookies()
      if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
        AccountManager.newChooseAccountIntent(
          ownerManager.getActiveOwner(provider.ownerType),
          null,
          arrayOf(provider.ownerType),
          true,
          null,
          null,
          null,
          null
        )
      } else {
        AccountManager.newChooseAccountIntent(
          ownerManager.getActiveOwner(provider.ownerType),
          null,
          arrayOf(provider.ownerType),
          null,
          null,
          null,
          null
        )
      }.also {
        startActivityForResult(it, ACCOUNT_CHOOSER_REQUESTCODE)
      }
    }

    views.buttonLogout.setOnClickListener {
      ownerManager.getActiveOwner(provider.ownerType)?.let { account ->
        ownerManager.removeOwner(account.type, account, object : Callback<Boolean> {
          override fun onResult(result: Boolean) {
            lifecycleScope.launch { show("Logged out: $result") }

          }

          override fun onError(error: Throwable) {
            lifecycleScope.launch { showError(error) }
          }
        })
      }
      cleanWebCookies()
    }

  }

  private fun cleanWebCookies() {
    /** remove all cookies to avoid an automatic relogin */
    CookieManager.getInstance().removeAllCookies(null)
  }

  private suspend fun show(toShow: String) = withContext(Dispatchers.Main) {
    Toast.makeText(applicationContext, toShow, Toast.LENGTH_SHORT).show()
  }

  private suspend fun showError(error: Throwable) {
    show(error.toString())
  }

  override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
    super.onActivityResult(requestCode, resultCode, data)
    if (requestCode == ACCOUNT_CHOOSER_REQUESTCODE && resultCode == RESULT_OK) {
      lifecycleScope.launch {
        if (data != null) {
          val type = requireNotNull(data.getStringExtra(AccountManager.KEY_ACCOUNT_TYPE))
          val name = requireNotNull(data.getStringExtra(AccountManager.KEY_ACCOUNT_NAME))
          ownerManager.switchActiveOwner(type, Account(name, type))
          show("Account switched to $name")
        } else {
          show("Wasn't able to switch accounts")
        }
      }
    }
  }
}
