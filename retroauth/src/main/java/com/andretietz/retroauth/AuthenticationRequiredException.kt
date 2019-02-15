package com.andretietz.retroauth

import java.io.IOException

/**
 * This authentication gets thrown, if the stored token is invalid or not existent and cannot be refreshed.
 * This is to cancel the request, which has been tried to call. The Login-Screen will open.
 */
class AuthenticationRequiredException : IOException()
