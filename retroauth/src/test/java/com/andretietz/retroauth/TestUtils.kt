package com.andretietz.retroauth

import okhttp3.Request
import org.junit.Assert
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.runners.MockitoJUnitRunner
import java.util.HashMap

@RunWith(MockitoJUnitRunner::class)
class TestUtils {

    @Test
    fun createUniqueIdentifier() {
        val map = HashMap<Int, Request>()

        var request = Request.Builder()
                .url("http://www.google.com/test/request1")
                .build()
        map[Utils.createUniqueIdentifier(request)] = request

        request = Request.Builder()
                .method("GET", null)
                .url("http://www.google.com/test/request1")
                .build()
        map[Utils.createUniqueIdentifier(request)] = request


        request = Request.Builder()
                .url("http://www.google.com/test/request2")
                .build()
        map[Utils.createUniqueIdentifier(request)] = request

        request = Request.Builder()
                .method("GET", null)
                .url("http://www.google.com/test/request2")
                .build()
        map[Utils.createUniqueIdentifier(request)] = request

        Assert.assertEquals(2, map.size.toLong())

    }
}
