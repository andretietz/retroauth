package com.andretietz.retroauth

import org.junit.Assert
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.runners.MockitoJUnitRunner

@RunWith(MockitoJUnitRunner::class)
class AuthenticationHandlerTest {

    @Mock
    private var methodCache: MethodCache<String>? = null
    @Mock
    private var ownerManager: OwnerManager<String, String>? = null
    @Mock
    private var tokenStorage: TokenStorage<String, String, String>? = null
    @Mock
    private var provider: TokenProvider<String>? = null
    @Mock
    private var typeFactory: TokenTypeFactory<String>? = null

    @Test
    fun allocate() {
        val authHandler = AuthenticationHandler(methodCache!!, ownerManager!!, tokenStorage!!, provider!!, typeFactory!!)
        Assert.assertNotNull(authHandler.methodCache)
        Assert.assertNotNull(authHandler.ownerManager)
        Assert.assertNotNull(authHandler.tokenStorage)
        Assert.assertNotNull(authHandler.provider)
        Assert.assertNotNull(authHandler.typeFactory)
    }
}
