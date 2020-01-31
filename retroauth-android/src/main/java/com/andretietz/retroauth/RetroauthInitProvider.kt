package com.andretietz.retroauth

import android.app.Application
import android.content.ContentProvider
import android.content.ContentValues
import android.content.Context
import android.content.pm.ProviderInfo
import android.database.Cursor
import android.net.Uri

internal class RetroauthInitProvider : ContentProvider() {

  override fun onCreate(): Boolean {
    context?.takeIf { it.applicationContext is Application }?.let {
      ActivityManager[it.applicationContext as Application]
    }
    return true
  }

  override fun attachInfo(context: Context?, info: ProviderInfo?) {
    if (info == null) throw NullPointerException("RetroauthInitProvider ProviderInfo cannot be null.")
    // So if the authorities equal the library internal ones, the developer forgot to set his applicationId
    if ("com.andretietz.retroauth.retroauthinitprovider" == info.authority) {
      error("Incorrect provider authority in manifest. Most likely due to a " +
        "missing applicationId variable in application\'s build.gradle.")
    }
    super.attachInfo(context, info)
  }

  override fun insert(uri: Uri, values: ContentValues?): Uri? = null
  override fun query(
    uri: Uri,
    projection: Array<out String>?,
    selection: String?,
    selectionArgs: Array<out String>?,
    sortOrder: String?
  ): Cursor? = null

  override fun update(
    uri: Uri,
    values: ContentValues?,
    selection: String?,
    selectionArgs: Array<out String>?
  ): Int = 0

  override fun delete(uri: Uri, selection: String?, selectionArgs: Array<out String>?): Int = 0
  override fun getType(uri: Uri): String? = null
}
