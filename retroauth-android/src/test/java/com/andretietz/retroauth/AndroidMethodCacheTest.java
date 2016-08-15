package com.andretietz.retroauth;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

@RunWith(RobolectricTestRunner.class)
@Config(constants = BuildConfig.class, sdk = 23)
public class AndroidMethodCacheTest {

    private AndroidMethodCache androidMethodCache;

    @Before
    public void setup() {
        androidMethodCache = new AndroidMethodCache();
    }

    @Test
    public void shouldRegisterTokenTypeAndBeGettable() throws Exception {
        AndroidTokenType androidTokenType = new AndroidTokenType("accountType", "token1type");
        AndroidTokenType androidTokenType2 = new AndroidTokenType("accountType", "token2type");
        androidMethodCache.register(1, androidTokenType);
        androidMethodCache.register(2, androidTokenType2);

        assertNotNull(androidMethodCache.getTokenType(1));
        assertEquals(androidTokenType, androidMethodCache.getTokenType(1));
        assertNotNull(androidMethodCache.getTokenType(2));
        assertEquals(androidTokenType2, androidMethodCache.getTokenType(2));
    }
}
