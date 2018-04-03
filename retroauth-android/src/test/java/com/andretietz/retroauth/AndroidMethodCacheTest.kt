//package com.andretietz.retroauth
//
//import org.junit.Assert.assertEquals
//import org.junit.Assert.assertNotNull
//import org.junit.Test
//import org.junit.runner.RunWith
//import org.robolectric.RobolectricTestRunner
//
//@RunWith(RobolectricTestRunner::class)
//class AndroidMethodCacheTest {
//
//    private val androidMethodCache: AndroidMethodCache = AndroidMethodCache()
//
//    @Test
//    @Throws(Exception::class)
//    fun shouldRegisterTokenTypeAndBeGettable() {
//        val androidTokenType = AndroidTokenType("accountType", "token1type")
//        val androidTokenType2 = AndroidTokenType("accountType", "token2type")
//        androidMethodCache.register(1, androidTokenType)
//        androidMethodCache.register(2, androidTokenType2)
//
//        assertNotNull(androidMethodCache.getTokenType(1))
//        assertEquals(androidTokenType, androidMethodCache.getTokenType(1))
//        assertNotNull(androidMethodCache.getTokenType(2))
//        assertEquals(androidTokenType2, androidMethodCache.getTokenType(2))
//    }
//}
