package com.andretietz.retroauth;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.annotation.Config;

import static org.junit.Assert.assertNotNull;
import static org.mockito.Mockito.mock;

@RunWith(RobolectricTestRunner.class)
@Config(constants = BuildConfig.class, sdk = 23)
public class AndroidAuthenticationHandlerTest {

    @Test
    public void createWithTokenTypeFactory() throws Exception {
        Provider provider = mock(Provider.class);
        TokenTypeFactory typeFactory = mock(TokenTypeFactory.class);
        AndroidAuthenticationHandler authenticationHandler = AndroidAuthenticationHandler
                .create(RuntimeEnvironment.application, provider, typeFactory);
        assertNotNull(authenticationHandler.methodCache);
        assertNotNull(authenticationHandler.ownerManager);
        assertNotNull(authenticationHandler.provider);
        assertNotNull(authenticationHandler.tokenStorage);
        assertNotNull(authenticationHandler.typeFactory);
    }
}
