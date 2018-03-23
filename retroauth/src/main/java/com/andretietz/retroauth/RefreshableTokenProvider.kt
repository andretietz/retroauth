package com.andretietz.retroauth

interface RefreshableTokenProvider<OWNER, TOKEN_TYPE, TOKEN> : TokenProvider<OWNER, TOKEN_TYPE, TOKEN> {
}