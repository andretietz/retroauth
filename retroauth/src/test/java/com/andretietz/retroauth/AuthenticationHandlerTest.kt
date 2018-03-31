package com.andretietz.retroauth

import org.junit.Assert
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.junit.MockitoJUnitRunner

@RunWith(MockitoJUnitRunner::class)
class AuthenticationHandlerTest {

    @Mock
    private lateinit var methodCache: MethodCache<String>
    @Mock
    private lateinit var ownerManager: OwnerManager<String, String>
    @Mock
    private lateinit var tokenStorage: TokenStorage<String, String, String>
    @Mock
    private lateinit var provider: TokenProvider<String, String, String>

    @Test
    fun allocate() {
        val authHandler = AuthenticationHandler(methodCache, ownerManager, tokenStorage, provider)
        Assert.assertNotNull(authHandler.methodCache)
        Assert.assertNotNull(authHandler.ownerManager)
        Assert.assertNotNull(authHandler.tokenStorage)
        Assert.assertNotNull(authHandler.provider)
    }
}
