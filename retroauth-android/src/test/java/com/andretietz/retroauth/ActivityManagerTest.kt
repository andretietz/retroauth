package com.andretietz.retroauth

import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment

import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull

@RunWith(RobolectricTestRunner::class)
class ActivityManagerTest {


    @Before
    fun setup() {
        ActivityManager[RuntimeEnvironment.application]
    }

    @Test
    @Throws(Exception::class)
    fun initializing() {
        val activityManager = ActivityManager[RuntimeEnvironment.application]
        assertNotNull(activityManager)

    }

    @Test
    fun getActivityFailing() {
        assertNull(ActivityManager[RuntimeEnvironment.application].activity)
    }

}
