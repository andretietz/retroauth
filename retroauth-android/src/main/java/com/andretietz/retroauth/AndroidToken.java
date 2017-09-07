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


package com.andretietz.retroauth;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

/**
 * The default android token represents a token string and a refresh token
 */
public class AndroidToken {

    public final String token;
    public final String refreshToken;

    public AndroidToken(@NonNull String token, @Nullable String refreshToken) {
        this.token = token;
        this.refreshToken = refreshToken;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        AndroidToken that = (AndroidToken) o;

        if (!token.equals(that.token)) return false;
        return refreshToken != null ? refreshToken.equals(that.refreshToken) : that.refreshToken == null;

    }

    @Override
    public int hashCode() {
        int result = token.hashCode();
        result = 31 * result + (refreshToken != null ? refreshToken.hashCode() : 0);
        return result;
    }
}
