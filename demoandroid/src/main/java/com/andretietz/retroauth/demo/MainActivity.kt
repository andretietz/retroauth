package com.andretietz.retroauth.demo

import android.accounts.AccountManager
import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.view.View
import android.widget.Toast
import com.andretietz.retroauth.AndroidAuthenticationHandler
import com.andretietz.retroauth.AndroidTokenType
import com.andretietz.retroauth.AuthAccountManager
import com.andretietz.retroauth.Retroauth
import com.andretietz.retroauth.TokenTypeFactory
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
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
                            AndroidTokenType(getString(annotationValues[0]), getString(annotationValues[1]))
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


        findViewById<View>(R.id.buttonRequestEmail).setOnClickListener({ _ ->
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
        })


        findViewById<View>(R.id.buttonInvalidateToken).setOnClickListener({ _ ->
            val activeAccount = authAccountManager
                    .getActiveAccount(getString(R.string.com_andretietz_retroauth_authentication_ACCOUNT))
            if (activeAccount != null) {
                // This is for demo purposes only. We're "manually" setting some-invalid-token.
                accountManager.setAuthToken(activeAccount,
                        getString(R.string.com_andretietz_retroauth_authentication_TOKEN), "some-invalid-token")
            }
        })
        findViewById<View>(R.id.buttonInvalidateRefreshToken).setOnClickListener({ _ ->
            val activeAccount = authAccountManager
                    .getActiveAccount(getString(R.string.com_andretietz_retroauth_authentication_ACCOUNT))
            if (activeAccount != null) {
                // This is for demo purposes only. We're "manually" setting some-invalid-token. (refresh token)
                accountManager
                        .setAuthToken(activeAccount,
                                String.format("%s_refresh",
                                        getString(R.string.com_andretietz_retroauth_authentication_TOKEN)),
                                "some-invalid-token")
            }
        })

        findViewById<View>(R.id.buttonSwitchAccount).setOnClickListener({ _ ->
            // warning can be ignored when using own account type
            val intent = authAccountManager.newChooseAccountIntent(
                    getString(R.string.com_andretietz_retroauth_authentication_ACCOUNT)
            )
            startActivityForResult(intent, RC_ACCOUNT_CHOOSER)
        })

        findViewById<View>(R.id.buttonAddAccount).setOnClickListener({ _ ->
            authAccountManager.addAccount(
                    getString(R.string.com_andretietz_retroauth_authentication_ACCOUNT),
                    getString(R.string.com_andretietz_retroauth_authentication_TOKEN))
        })

        findViewById<View>(R.id.buttonRemoveAccount).setOnClickListener { _ ->
            authAccountManager.removeActiveAccount(getString(R.string.com_andretietz_retroauth_authentication_ACCOUNT))
        }
    }

    private fun show(toShow: String) {
        Toast.makeText(this, toShow, Toast.LENGTH_SHORT).show()
    }

    private fun showError(error: Throwable) {
        show(error.toString())
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (resultCode == Activity.RESULT_OK && data != null) {
            if (requestCode == RC_ACCOUNT_CHOOSER) {
                val accountType = data.getStringExtra(AccountManager.KEY_ACCOUNT_TYPE)
                val accountName = data.getStringExtra(AccountManager.KEY_ACCOUNT_NAME)
                authAccountManager.setActiveAccount(accountType, accountName)
            }
        }
    }

    companion object {
        private const val RC_ACCOUNT_CHOOSER = 123
    }
}
