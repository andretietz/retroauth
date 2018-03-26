package com.andretietz.retroauth


import org.junit.Assert
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.runners.MockitoJUnitRunner

import okhttp3.HttpUrl
import retrofit2.CallAdapter
import retrofit2.Retrofit

@RunWith(MockitoJUnitRunner::class)
class RetroauthBuilderTest {

    @Mock
    private var provider: TokenProvider<String>? = null
    @Mock
    private var methodCache: MethodCache<String>? = null
    @Mock
    private var ownerManager: OwnerManager<String, String>? = null
    @Mock
    private var tokenStorage: TokenStorage<String, String, String>? = null
    @Mock
    private var typeFactory: TokenTypeFactory<String>? = null

    @Test
    fun builder() {
        val authHandler = AuthenticationHandler(
                methodCache!!,
                ownerManager!!,
                tokenStorage!!,
                provider!!,
                typeFactory!!
        )

        val url = HttpUrl.parse("https://github.com/andretietz/retroauth/")

        val retrofit = Retroauth.Builder(authHandler)
                .baseUrl(url!!)
                .build()

        val factories = retrofit.callAdapterFactories()
        // There should be 2 factories. one for retroauth and a retrofit default one
        Assert.assertEquals(2, factories.size.toLong())
        // The first one must be the RetroauthCallAdapterFactory
        Assert.assertTrue(factories[0] is RetroauthCallAdapterFactory<*, *, *>)
        // base url should be the same as defined
        Assert.assertTrue(url == retrofit.baseUrl())
    }
}
