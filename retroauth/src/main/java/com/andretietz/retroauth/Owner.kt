package com.andretietz.retroauth

interface Owner<out OWNER_TYPE> {
    fun getName(): String
    fun getType(): OWNER_TYPE
}