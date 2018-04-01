package com.andretietz.retroauth.testimpl

import com.andretietz.retroauth.OwnerManager

class TestOwnerManager() : OwnerManager<String, String> {
    override fun createOrGetOwner(type: String): String = "owner"
}