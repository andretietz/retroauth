package com.andretietz.retroauth

import android.app.Application
import android.content.ContentProvider
import android.content.ContentValues
import android.content.Context
import android.content.pm.ProviderInfo
import android.database.Cursor
import android.net.Uri

internal class RetroauthInitProvider : Initializer<ActivityManager?>() {
  override fun create(context: Context): ActivityManager? {
    context.takeIf { it.applicationContext is Application }?.let {
      return ActivityManager[it.applicationContext as Application]
    }
    throw IllegalStateException("Could not initialize retroauth. Context is not an application!")
  }
}
