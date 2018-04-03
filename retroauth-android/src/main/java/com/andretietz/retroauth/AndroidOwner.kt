package com.andretietz.retroauth

import android.accounts.Account

class AndroidOwner(val account: Account) : Owner<String> {

    override fun getName(): String {
        return account.name
    }

    override fun getType(): String {
        return account.type
    }
}