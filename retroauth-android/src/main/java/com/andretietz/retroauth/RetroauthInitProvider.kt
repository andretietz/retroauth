package com.andretietz.retroauth

import android.app.Application
import android.content.Context
import androidx.startup.Initializer

internal class RetroauthInitProvider : Initializer<ActivityManager> {
  override fun create(context: Context): ActivityManager {
    context.takeIf { it.applicationContext is Application }?.let {
      return ActivityManager[it.applicationContext as Application]
    }
    throw IllegalStateException("Could not initialize retroauth. Context is not an application!")
  }

  override fun dependencies(): MutableList<Class<out Initializer<*>>> = mutableListOf()
}
