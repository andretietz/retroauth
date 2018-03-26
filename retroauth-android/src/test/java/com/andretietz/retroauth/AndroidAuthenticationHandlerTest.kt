package com.andretietz.retroauth

import org.junit.Assert.assertNotNull
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mockito.mock
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment

@RunWith(RobolectricTestRunner::class)
class AndroidAuthenticationHandlerTest {

    @Test
    @Throws(Exception::class)
    fun createWithTokenTypeFactory() {
        val provider = mock(TokenProvider::class.java)
        val typeFactory = mock(TokenTypeFactory::class.java)
        val authenticationHandler = AndroidAuthenticationHandler
                .create(RuntimeEnvironment.application,
                        provider as TokenProvider<AndroidToken>,
                        typeFactory as TokenTypeFactory<AndroidTokenType>)
        assertNotNull(authenticationHandler.methodCache)
        assertNotNull(authenticationHandler.ownerManager)
        assertNotNull(authenticationHandler.provider)
        assertNotNull(authenticationHandler.tokenStorage)
        assertNotNull(authenticationHandler.typeFactory)
    }
}
