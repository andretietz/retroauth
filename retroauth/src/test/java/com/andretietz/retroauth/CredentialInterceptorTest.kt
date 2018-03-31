package com.andretietz.retroauth

import com.andretietz.retroauth.testimpl.TestProvider
import com.nhaarman.mockito_kotlin.any
import com.nhaarman.mockito_kotlin.eq
import com.nhaarman.mockito_kotlin.whenever
import okhttp3.Call
import okhttp3.Connection
import okhttp3.Interceptor
import okhttp3.Protocol
import okhttp3.Request
import okhttp3.Response
import org.junit.Assert
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.ArgumentMatchers.anyString
import org.mockito.Mock
import org.mockito.Mockito
import org.mockito.junit.MockitoJUnitRunner
import java.io.IOException
import java.util.concurrent.TimeUnit

@RunWith(MockitoJUnitRunner::class)
class CredentialInterceptorTest {


    companion object {
        const val AUTHENTICATION_HEADER_KEY = "auth"
    }

    @Mock
    private lateinit var tokenStorage: TokenStorage<String, String, String>

    private val tokenProvider = TestProvider()
    @Mock
    private lateinit var methodCache: MethodCache<String>

    @Mock
    private lateinit var ownerManager: OwnerManager<String, String>

    @Before
    fun setup() {
        whenever(tokenStorage.getToken(Mockito.anyString(), Mockito.anyString()))
                .thenReturn("token")
    }

    @Test
    fun intercept() {
        val request1 = Request.Builder().url("http://www.google.de/test1").build()
        val request2 = Request.Builder().url("http://www.google.de/test2").build()

        val interceptor = CredentialInterceptor(tokenProvider, ownerManager, tokenStorage, methodCache)
        val interceptorChain = TestInterceptorChain()

        // testing unauthenticated request
        interceptorChain.setupRequest(request1)
        var response = interceptor.intercept(interceptorChain)
        // should not contain any headers
        Assert.assertEquals(0, response!!.headers().size().toLong())

        // testing authenticated request
        interceptorChain.setupRequest(request2)
        whenever(methodCache.getTokenType(any())).thenReturn("tokenType")
        whenever(ownerManager.getOwner(anyString())).thenReturn("owner")
        whenever(tokenStorage.getToken(eq("owner"), eq("tokenType"))).thenReturn("token")
        response = interceptor.intercept(interceptorChain)
        // should contain the token in the header
        Assert.assertTrue("token" == response!!.request().header(AUTHENTICATION_HEADER_KEY))

    }

    private class TestInterceptorChain : Interceptor.Chain {
        private var request: Request? = null

        fun setupRequest(request: Request) {
            this.request = request
        }

        override fun request(): Request? {
            return request
        }

        @Throws(IOException::class)
        override fun proceed(request: Request): Response {
            return Response.Builder()
                    .request(request)
                    .code(200)
                    .protocol(Protocol.HTTP_1_1)
                    .message("required message")
                    .build()
        }

        override fun connection(): Connection? {
            return null
        }

        override fun writeTimeoutMillis(): Int {
            TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
        }

        override fun call(): Call {
            TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
        }

        override fun withWriteTimeout(timeout: Int, unit: TimeUnit?): Interceptor.Chain {
            TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
        }

        override fun connectTimeoutMillis(): Int {
            TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
        }

        override fun withConnectTimeout(timeout: Int, unit: TimeUnit?): Interceptor.Chain {
            TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
        }

        override fun withReadTimeout(timeout: Int, unit: TimeUnit?): Interceptor.Chain {
            TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
        }

        override fun readTimeoutMillis(): Int {
            TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
        }
    }
}
