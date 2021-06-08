package com.andretietz.retroauth

import android.app.Activity
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mockito.mock

@RunWith(AndroidJUnit4::class)
class WeakActivityStackTest {

  private val stack = WeakActivityStack()

  @Test
  fun basicStackTest() {
    val activity1 = mock(Activity::class.java)
    val activity2 = mock(Activity::class.java)
    val activity3 = mock(Activity::class.java)

    stack.push(activity1)
    stack.push(activity2)
    stack.push(activity3)

    assertEquals(activity3, stack.pop())
    assertEquals(activity2, stack.pop())
    assertEquals(activity1, stack.pop())
  }

  @Test
  fun stackRemovalTest() {
    val activity1 = mock(Activity::class.java)
    val activity2 = mock(Activity::class.java)
    val activity3 = mock(Activity::class.java)

    stack.push(activity1)
    stack.push(activity2)
    stack.push(activity3)

    stack.remove(activity2)

    assertEquals(activity3, stack.pop())
    assertEquals(activity1, stack.pop())
  }

  @Test
  fun splashScreenTest() {
    val splashScreen = mock(Activity::class.java)
    val mainScreen = mock(Activity::class.java)

    stack.push(splashScreen)
    stack.push(mainScreen)

    stack.remove(splashScreen)

    assertEquals(mainScreen, stack.peek())
  }
}
