package com.andretietz.retroauth

import com.andretietz.retroauth.testimpl.TestInterface
import com.andretietz.retroauth.testimpl.TestProvider
import com.andretietz.retroauth.testimpl.TestResponse
import com.andretietz.retroauth.testimpl.TestTokenStorage
import com.andretietz.retroauth.testimpl.TestTokenTypeFactory
import com.nhaarman.mockito_kotlin.eq
import com.nhaarman.mockito_kotlin.spy
import com.nhaarman.mockito_kotlin.times
import com.nhaarman.mockito_kotlin.verify
import com.nhaarman.mockito_kotlin.whenever
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.ArgumentMatchers.anyString
import org.mockito.Mock
import org.mockito.junit.MockitoJUnitRunner
import retrofit2.HttpException
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory
import retrofit2.converter.moshi.MoshiConverterFactory
import java.io.IOException


@RunWith(MockitoJUnitRunner::class)
class RetroauthTest {

    private lateinit var server: MockWebServer
    private lateinit var service: TestInterface
    private var provider: TokenProvider<String> = spy(TestProvider())
    private var tokenStorage = spy(TestTokenStorage())

    companion object {
        val expectedResult = TestResponse(1)
    }

    @Mock
    private lateinit var ownerManager: OwnerManager<String, String>

    @Before
    @Throws(IOException::class)
    fun prepare() {
        server = MockWebServer()
        server.start()
        val methodCache = MethodCache.DefaultMethodCache<String>()
        val authHandler = AuthenticationHandler(
                methodCache,
                ownerManager,
                tokenStorage,
                provider,
                TestTokenTypeFactory()
        )

        val retrofit = Retroauth.Builder(authHandler)
                .baseUrl(server.url("/"))
                .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
                .addConverterFactory(MoshiConverterFactory.create())
                .build()

        service = retrofit.create(TestInterface::class.java)

        whenever(ownerManager.getOwner(anyString())).thenReturn("owner")
    }

    @After
    fun shutdown() {
        server.shutdown()
    }

    @Test
    fun simpleHappyCase() {
        // test very simple case
        server.enqueue(MockResponse().setBody(TestInterface.TEST_BODY))

        service.authenticatedMethod()
                .test()
                .assertResult(expectedResult)

        server.enqueue(MockResponse().setBody(TestInterface.TEST_BODY))

        service.unauthenticatedMethod()
                .test()
                .assertResult(expectedResult)
    }

    @Test
    fun simpleErrorCase() {
        // test simple error case
        server.enqueue(MockResponse().setResponseCode(400))
        service.authenticatedMethod()
                .test()
                .assertFailure(HttpException::class.java)


        server.enqueue(MockResponse().setResponseCode(400))
        service.unauthenticatedMethod()
                .test()
                .assertFailure(HttpException::class.java)
    }

    @Test
    fun userCanceledAuthentication() {

        whenever(ownerManager.getOwner(anyString())).thenThrow(AuthenticationCanceledException::class.java)

        service.authenticatedMethod()
                .test()
                .assertFailure(AuthenticationCanceledException::class.java)

        server.enqueue(MockResponse().setBody(TestInterface.TEST_BODY))
        service.unauthenticatedMethod()
                .test()
                .assertResult(expectedResult)

    }

    @Test
    fun testIfTokenRefreshWorks() {

        server.enqueue(MockResponse().setResponseCode(401))

        server.enqueue(
                MockResponse()
                        .setResponseCode(200)
                        .setBody(TestInterface.TEST_BODY)
        )

        

        whenever(provider.refreshToken(eq("token")))
                .thenAnswer {
                    Thread.sleep(250)
                    return@thenAnswer "new token"
                }
        service.authenticatedMethod()
                .test()
                .assertResult(expectedResult)

        verify(tokenStorage, times(1)).storeToken(anyString(), anyString(), eq("new token"))


    }
//    @Test
//    fun concurrencyTestForTokenRefreshes() {
//
//        server.enqueue(MockResponse().setResponseCode(401))
//
//        server.enqueue(
//                MockResponse()
//                        .setResponseCode(200)
//                        .setBody(TestInterface.TEST_BODY)
//        )
//
//        whenever(provider.refreshToken(eq("token")))
//                .thenAnswer {
//                    Thread.sleep(250)
//                    return@thenAnswer "new token"
//                }
//        service.authenticatedMethod()
//                .test()
//                .assertResult(expectedResult)
//
//        verify(tokenStorage, times(1)).storeToken(anyString(), anyString(), eq("new token"))
//
//
//    }
}
