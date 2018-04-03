//package com.andretietz.retroauth
//
//
//import okhttp3.HttpUrl
//import org.junit.Assert
//import org.junit.Test
//import org.junit.runner.RunWith
//import org.mockito.Mock
//import org.mockito.junit.MockitoJUnitRunner
//
//@RunWith(MockitoJUnitRunner::class)
//class RetroauthBuilderTest {
//
//    @Mock
//    private lateinit var provider: TokenProvider<String, String, String>
//    @Mock
//    private lateinit var methodCache: MethodCache<String>
//    @Mock
//    private lateinit var ownerManager: OwnerManager<String, String>
//    @Mock
//    private lateinit var tokenStorage: TokenStorage<String, String, String>
//
//    @Test
//    fun builder() {
//        val url = HttpUrl.parse("https://github.com/andretietz/retroauth/")!!
//
//        val retrofit = Retroauth.Builder(provider, ownerManager, tokenStorage, methodCache)
//                .baseUrl(url)
//                .build()
//
//        val factories = retrofit.callAdapterFactories()
//        // There should be 2 factories. one for retroauth and a retrofit default one
//        Assert.assertEquals(2, factories.size.toLong())
//        // The first one must be the RetroauthCallAdapterFactory
//        Assert.assertTrue(factories[0] is RetroauthCallAdapterFactory<*, *, *>)
//        // base url should be the same as defined
//        Assert.assertTrue(url == retrofit.baseUrl())
//    }
//}
