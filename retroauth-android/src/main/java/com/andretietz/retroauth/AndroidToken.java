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

/**
 * The default android token represents a token string and a refresh token
 */
public class AndroidToken {

    public final String token;
    public final String refreshToken;

    public AndroidToken(String token, String refreshToken) {
        this.token = token;
        this.refreshToken = refreshToken;
    }

}
