//package com.andretietz.retroauth
//
//import com.andretietz.retroauth.testimpl.TestInterface
//import com.nhaarman.mockito_kotlin.any
//import com.nhaarman.mockito_kotlin.whenever
//import org.junit.Assert
//import org.junit.Before
//import org.junit.Test
//import org.junit.runner.RunWith
//import org.mockito.Mock
//import org.mockito.junit.MockitoJUnitRunner
//import retrofit2.CallAdapter
//import retrofit2.Retrofit
//import java.lang.reflect.Type
//
//@RunWith(MockitoJUnitRunner::class)
//class RetroauthCallAdapterFactoryTest {
//
//    @Mock
//    private lateinit var type: Type
//
//    @Mock
//    private lateinit var callAdapterFactory: CallAdapter.Factory
//    @Mock
//    private lateinit var callAdapter: CallAdapter<*, *>
//
//    @Mock
//    private lateinit var tokenStorage: TokenStorage<String, String, String>
//    @Mock
//    private lateinit var tokenProvider: TokenProvider<String, String, String>
//
//    private var retrofit = Retrofit.Builder().baseUrl("http://foo.com").build()
//
//    @Before
//    fun setup() {
//        whenever(tokenProvider.getTokenType(any())).thenReturn("tokenType")
//        whenever(callAdapterFactory.get(any(), any(), any())).thenReturn(callAdapter)
//    }
//
//    @Test
//    fun adapterFactory() {
//
//        val adapterFactory = RetroauthCallAdapterFactory(
//                listOf(callAdapterFactory),
//                tokenProvider
//        )
//
//        val methods = TestInterface::class.java.methods
//
//        for (method in methods) {
//            val callAdapter = adapterFactory.get(type, method.annotations, retrofit)
//            if (method.name == "authenticatedMethod") {
//                // the authenticated method should use the RetroauthCallAdapter
//                Assert.assertTrue(callAdapter is RetroauthCallAdapterFactory.RetroauthCallAdapter<*, *>)
//            } else {
//                // the unauthenticated method should not use the RetroauthCallAdapter
//                Assert.assertFalse(callAdapter is RetroauthCallAdapterFactory.RetroauthCallAdapter<*, *>)
//            }
//        }
//    }
//}
