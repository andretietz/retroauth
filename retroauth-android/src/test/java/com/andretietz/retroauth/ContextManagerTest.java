package com.andretietz.retroauth;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.annotation.Config;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

@RunWith(RobolectricTestRunner.class)
@Config(constants = BuildConfig.class, sdk = 23)
public class ContextManagerTest {


    @Before
    public void setup() {
        ContextManager.get(RuntimeEnvironment.application);
    }

    @Test
    public void initializing() throws Exception {
        ContextManager contextManager = ContextManager.get(RuntimeEnvironment.application);
        assertNotNull(contextManager);

    }

    @Test
    public void getContext() {
        assertNotNull(ContextManager.get().getContext());
    }

    @Test
    public void getActivityFailing() {
        assertNull(ContextManager.get().getActivity());
    }

}
