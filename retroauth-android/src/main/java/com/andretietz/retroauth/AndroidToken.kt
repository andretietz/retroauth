/*
 * Copyright (c) 2016 Andre Tietz
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */


package com.andretietz.retroauth

/**
 * The default android token represents a token string and a refresh token
 */
data class AndroidToken(val token: String, val refreshToken: String?) {

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other == null || javaClass != other.javaClass) return false
        val that = other as AndroidToken?
        if (token != that!!.token) return false
        return if (refreshToken != null) refreshToken == that.refreshToken else that.refreshToken == null
    }

    override fun hashCode(): Int {
        var result = token.hashCode()
        result = 31 * result + (refreshToken?.hashCode() ?: 0)
        return result
    }
}
