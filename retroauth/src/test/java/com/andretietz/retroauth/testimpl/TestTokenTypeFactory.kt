package com.andretietz.retroauth.testimpl

import com.andretietz.retroauth.TokenTypeFactory

class TestTokenTypeFactory : TokenTypeFactory<String> {
    override fun create(annotationValues: IntArray): String = "tokenType"
}
