package com.andretietz.retroauth

import android.app.Application
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.assertj.core.api.Assertions.assertThat
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class ActivityManagerTest {
  private val application = ApplicationProvider.getApplicationContext<Application>()

  @Before
  fun setup() {
    ActivityManager[application]
  }

  @Test
  fun initializing() {
    val activityManager = ActivityManager[application]
    assertThat(activityManager).isNotNull
  }
}
