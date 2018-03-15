package com.andretietz.retroauth;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.RuntimeEnvironment;

import static org.junit.Assert.assertNotNull;
import static org.mockito.Mockito.mock;

@RunWith(RobolectricTestRunner.class)
public class AndroidAuthenticationHandlerTest {

    @Test
    public void createWithTokenTypeFactory() throws Exception {
        Provider provider = mock(Provider.class);
        TokenTypeFactory typeFactory = mock(TokenTypeFactory.class);
        AndroidAuthenticationHandler authenticationHandler = AndroidAuthenticationHandler.Companion
                .create(RuntimeEnvironment.application, provider, typeFactory);
        assertNotNull(authenticationHandler.getMethodCache());
        assertNotNull(authenticationHandler.getOwnerManager());
        assertNotNull(authenticationHandler.getProvider());
        assertNotNull(authenticationHandler.getTokenStorage());
        assertNotNull(authenticationHandler.getTypeFactory());
    }
}
