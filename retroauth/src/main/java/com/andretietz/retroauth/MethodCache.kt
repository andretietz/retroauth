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

import java.util.HashMap

/**
 * This cache stores the unique hash of a request to identify it later on when
 * authenticating the request itself. The identifier is created right now
 * in [Utils.createUniqueIdentifier], this may change.
 */
interface MethodCache<OWNER_TYPE : Any, TOKEN_TYPE : Any> {

  /**
   * Registers a token type with a specific identifier.
   *
   * @param requestIdentifier to identify the request later on
   * @param type type of the request.
   */
  fun register(requestIdentifier: Int, type: RequestType<OWNER_TYPE, TOKEN_TYPE>)

  /**
   * @param requestIdentifier the request identifier
   * @return the token type to authenticate the request
   */
  fun getTokenType(requestIdentifier: Int): RequestType<OWNER_TYPE, TOKEN_TYPE>?

  /**
   * The default implementation of the [MethodCache].
   *
   * @param <TOKEN_TYPE>
   */
  class DefaultMethodCache<OWNER_TYPE : Any, TOKEN_TYPE : Any> : MethodCache<OWNER_TYPE, TOKEN_TYPE> {
    private val map = HashMap<Int, RequestType<OWNER_TYPE, TOKEN_TYPE>>()

    override fun register(requestIdentifier: Int, type: RequestType<OWNER_TYPE, TOKEN_TYPE>) {
      map[requestIdentifier] = type
    }

    override fun getTokenType(requestIdentifier: Int): RequestType<OWNER_TYPE, TOKEN_TYPE>? = map[requestIdentifier]
  }
}
