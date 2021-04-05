package com.andretietz.retroauth.demo.di

import android.app.Application
import com.andretietz.retroauth.AndroidCredentialStorage
import com.andretietz.retroauth.AndroidOwnerStorage
import com.andretietz.retroauth.androidAuthentication
import com.andretietz.retroauth.demo.auth.GithubAuthenticator
import com.github.scribejava.apis.GitHubApi
import com.github.scribejava.core.builder.ServiceBuilder
import com.github.scribejava.core.oauth.OAuth20Service
import com.github.scribejava.httpclient.okhttp.OkHttpHttpClient
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import okhttp3.Cache
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
class ApiModule {

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
      .addInterceptor(HttpLoggingInterceptor()
        .also {
          it.level = HttpLoggingInterceptor.Level.HEADERS
        })
      .addInterceptor { chain: Interceptor.Chain ->
        val request = chain.request().newBuilder()
          .addHeader("Accept", "application/vnd.github.v3+json")
          .build()
        chain.proceed(request)
      }
      .cache(Cache(application.cacheDir, 50 * 1024 * 1024))
      .build()

    /**
     * Create your Retrofit Object using the [Retrofit.androidAuthentication]
     */
    val retrofit = Retrofit.Builder()
      .baseUrl("https://api.github.com")
      .client(httpClient)
      .addConverterFactory(MoshiConverterFactory.create())
      .build()
      .androidAuthentication(application, authenticator)

    /**
     * Create your API Service
     */
    return retrofit
  }

  @Singleton
  @Provides
  fun providesOwnerStorage(application: Application) = AndroidOwnerStorage(application)

  @Singleton
  @Provides
  fun providesCredentialStorage(application: Application) = AndroidCredentialStorage(application)

  @Singleton
  @Provides
  fun providesAuthenticator(application: Application): GithubAuthenticator =
    GithubAuthenticator(application)
}
