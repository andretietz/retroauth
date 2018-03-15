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
 * The [AuthenticationHandler] is a class that collapses a [MethodCache], an [OwnerManager], a
 * [TokenStorage] and a [Provider] into one single immutable object.
 */
open class AuthenticationHandler<OWNER, TOKEN_TYPE, TOKEN>(val methodCache: MethodCache<TOKEN_TYPE>,
                                                           val ownerManager: OwnerManager<OWNER, TOKEN_TYPE>,
                                                           val tokenStorage: TokenStorage<OWNER, TOKEN_TYPE, TOKEN>,
                                                           val provider: Provider<OWNER, TOKEN_TYPE, TOKEN>,
                                                           val typeFactory: TokenTypeFactory<TOKEN_TYPE>)
