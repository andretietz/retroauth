package com.andretietz.retroauth.demo.di

import android.app.Application
import com.andretietz.retroauth.AndroidAccountManagerCredentialStorage
import com.andretietz.retroauth.AndroidAccountManagerOwnerStorage
import com.andretietz.retroauth.androidAuthentication
import com.andretietz.retroauth.demo.R
import com.andretietz.retroauth.demo.api.GithubApi
import com.andretietz.retroauth.demo.auth.GithubAuthenticator
import com.andretietz.retroauth.demo.auth.LoginActivity
import com.github.scribejava.apis.GitHubApi
import com.github.scribejava.core.builder.ServiceBuilder
import com.github.scribejava.core.oauth.OAuth20Service
import com.github.scribejava.httpclient.okhttp.OkHttpHttpClient
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object ApiModule {

  /**
   * provides the scribejava object to authenticate using github.
   */
  @Singleton
  @Provides
  fun provideOauthService(): OAuth20Service {
    return ServiceBuilder(GithubAuthenticator.CLIENT_ID)
      .apiSecret(GithubAuthenticator.CLIENT_SECRET)
      .callback(GithubAuthenticator.CLIENT_CALLBACK)
      .httpClient(OkHttpHttpClient())
      .defaultScope("repo user:email")
      .build(GitHubApi.instance())
  }

  @Singleton
  @Provides
  fun provideRetrofit(
    application: Application,
    authenticator: GithubAuthenticator
  ): Retrofit {
    /**
     * Optional: create your own OkHttpClient
     */
    val httpClient = OkHttpClient.Builder()
      .addNetworkInterceptor(HttpLoggingInterceptor()
        .also { it.level = HttpLoggingInterceptor.Level.BODY })
      .addInterceptor { chain: Interceptor.Chain ->
        val request = chain.request().newBuilder()
          .addHeader("Accept", "application/vnd.github.v3+json")
          .build()
        chain.proceed(request)
      }
      .build()

    /**
     * Create your Retrofit Object using the [Retrofit.androidAuthentication]
     */
    return Retrofit.Builder()
      .baseUrl("https://api.github.com")
      .client(httpClient)
      .addConverterFactory(MoshiConverterFactory.create())
      .build()
      .androidAuthentication(application, authenticator, application.getString(R.string.authentication_ACCOUNT))
  }

  @Singleton
  @Provides
  fun providesOwnerStorage(application: Application) =
    AndroidAccountManagerOwnerStorage(
      application,
      application.getString(R.string.authentication_ACCOUNT))

  @Singleton
  @Provides
  fun providesCredentialStorage(application: Application) = AndroidAccountManagerCredentialStorage(application)

  @Singleton
  @Provides
  fun providesAuthenticator(application: Application): GithubAuthenticator =
    GithubAuthenticator(application)

  @Singleton
  @Provides
  fun providesGithubSignInApi(retrofit: Retrofit): LoginActivity.SignInApi =
    retrofit.create(LoginActivity.SignInApi::class.java)

  @Singleton
  @Provides
  fun providesGithubApi(retrofit: Retrofit): GithubApi =
    retrofit.create(GithubApi::class.java)
}
