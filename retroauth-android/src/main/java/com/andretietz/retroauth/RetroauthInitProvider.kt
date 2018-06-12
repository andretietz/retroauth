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
    if (context.applicationContext is Application) {
      val application = context.applicationContext as Application
      ActivityManager[application]
    }
    return false
  }

  override fun attachInfo(context: Context?, info: ProviderInfo?) {
    if (info == null) {
      throw NullPointerException("YourLibraryInitProvider ProviderInfo cannot be null.")
    }
    // So if the authorities equal the library internal ones, the developer forgot to set his applicationId
    if ("com.andretietz.retroauth.retroauthinitprovider".equals(info.authority)) {
      throw IllegalStateException("Incorrect provider authority in manifest. Most likely due to a "
          + "missing applicationId variable in application\'s build.gradle.")
    }
    super.attachInfo(context, info)
  }

  override fun insert(p0: Uri?, p1: ContentValues?): Uri? = null
  override fun query(p0: Uri?, p1: Array<out String>?, p2: String?, p3: Array<out String>?, p4: String?): Cursor? = null
  override fun update(p0: Uri?, p1: ContentValues?, p2: String?, p3: Array<out String>?): Int = 0
  override fun delete(p0: Uri?, p1: String?, p2: Array<out String>?): Int = 0
  override fun getType(p0: Uri?): String? = null
}