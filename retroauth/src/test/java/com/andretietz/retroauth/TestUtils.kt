package com.andretietz.retroauth

import okhttp3.Request
import org.junit.Assert
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.junit.MockitoJUnitRunner
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

  @Test
  fun check() {

    val request1 = Request.Builder()
        .method("GET", null)
        .header("foo", "bar")
        .url("http://www.google.com/test/request2")
        .build()


    val request2 = Request.Builder()
        .method("GET", null)
        .header("var", "foo")
        .url("http://www.google.com/test/request2")
        .build()

    val request3 = Request.Builder()
        .method("GET", null)
        .header("var", "foo")
        .tag("fpp")
        .url("http://www.google.com/test/request2")
        .build()

    val id1 = Utils.createUniqueIdentifier(request1)
    val id2 = Utils.createUniqueIdentifier(request2)
    val id3 = Utils.createUniqueIdentifier(request3)

    /** Headers should not effect the id **/
    Assert.assertTrue(id1 == id2)
    Assert.assertTrue(id2 == id3)

  }
}
