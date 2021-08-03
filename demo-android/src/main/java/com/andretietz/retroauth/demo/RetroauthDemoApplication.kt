package com.andretietz.retroauth.demo

import android.app.Application
import dagger.hilt.android.HiltAndroidApp
//import dagger.hilt.android.HiltAndroidApp
import timber.log.Timber

@HiltAndroidApp
class RetroauthDemoApplication : Application() {
  override fun onCreate() {
    super.onCreate()
    Timber.plant(Timber.DebugTree())
  }
}
