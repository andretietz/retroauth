package com.andretietz.retroauth

import org.junit.Assert.assertNotNull
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment

@RunWith(RobolectricTestRunner::class)
class ActivityManagerTest {

  @Before
  fun setup() {
    ActivityManager[RuntimeEnvironment.application]
  }

  @Test
  fun initializing() {
    val activityManager = ActivityManager[RuntimeEnvironment.application]
    assertNotNull(activityManager)
  }
}
