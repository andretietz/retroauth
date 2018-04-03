//package com.andretietz.retroauth.testimpl
//
//import com.andretietz.retroauth.TokenProvider
//import okhttp3.Request
//
//open class TestProvider : TokenProvider<String, String, String> {
//    override fun getTokenType(annotationValues: IntArray): String {
//        return "tokenType"
//    }
//
//    override fun authenticateRequest(request: Request, token: String): Request =
//            request
//                    .newBuilder()
//                    .header("auth", token)
//                    .build()
//
//}
