package com.andretietz.retroauth

import com.andretietz.retroauth.testimpl.TestInterface
import com.andretietz.retroauth.testimpl.TestProvider
import org.junit.Assert
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.Mockito
import org.mockito.junit.MockitoJUnitRunner
import retrofit2.CallAdapter
import retrofit2.Retrofit
import java.lang.reflect.Type
import java.util.ArrayList

@RunWith(MockitoJUnitRunner::class)
class RetroauthCallAdapterFactoryTest {

//    @Mock
    internal var type: Type? = null

    @Mock
    internal var callAdapter: CallAdapter<*, *>? = null

    @Mock
    private lateinit var tokenStorage: TokenStorage<String, String, String>

    private val tokenProvider = TestProvider()

    internal var retrofit = Retrofit.Builder().baseUrl("http://foo.com").build()

    @Before
    fun setup() {
        Mockito.`when`(tokenStorage.getToken(Mockito.anyString(), Mockito.anyString())).thenReturn("token")
    }

    @Test
    fun adapterFactory() {
        val factories = ArrayList<CallAdapter.Factory>()
        factories.add(object : CallAdapter.Factory() {
            override fun get(returnType: Type, annotations: Array<Annotation>, retrofit: Retrofit): CallAdapter<*, *>? = callAdapter
        })

        val adapterFactory = RetroauthCallAdapterFactory(factories, tokenProvider)

        val methods = TestInterface::class.java.methods

        for (method in methods) {
            val callAdapter = adapterFactory.get(type!!, method.annotations, retrofit)
            if (method.name == "authenticatedMethod") {
                // the authenticated method should use the RetroauthCallAdapter
                Assert.assertTrue(callAdapter is RetroauthCallAdapterFactory.RetroauthCallAdapter<*, *>)
            } else {
                // the unauthenticated method should not use the one
                Assert.assertFalse(callAdapter is RetroauthCallAdapterFactory.RetroauthCallAdapter<*, *>)
            }
        }
    }
}
