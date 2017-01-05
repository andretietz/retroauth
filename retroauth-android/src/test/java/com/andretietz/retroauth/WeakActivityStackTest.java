package com.andretietz.retroauth;

import android.app.Activity;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;

@RunWith(RobolectricTestRunner.class)
@Config(constants = BuildConfig.class, sdk = 23)
public class WeakActivityStackTest {


    WeakActivityStack stack;

    @Before
    public void setup() {
        stack = new WeakActivityStack();
    }

    @Test
    public void basicStackTest() {
        Activity activity1 = mock(Activity.class);
        Activity activity2 = mock(Activity.class);
        Activity activity3 = mock(Activity.class);

        stack.push(activity1);
        stack.push(activity2);
        stack.push(activity3);

        assertEquals(activity3, stack.pop());
        assertEquals(activity2, stack.pop());
        assertEquals(activity1, stack.pop());
    }


    @Test
    public void stackRemovalTest() {
        Activity activity1 = mock(Activity.class);
        Activity activity2 = mock(Activity.class);
        Activity activity3 = mock(Activity.class);

        stack.push(activity1);
        stack.push(activity2);
        stack.push(activity3);

        stack.remove(activity2);

        assertEquals(activity3, stack.pop());
        assertEquals(activity1, stack.pop());
    }

    @Test
    public void splashScreenTest() {
        Activity splashScreen = mock(Activity.class);
        Activity mainScreen = mock(Activity.class);

        stack.push(splashScreen);
        stack.push(mainScreen);

        stack.remove(splashScreen);

        assertEquals(mainScreen, stack.peek());
    }


}