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

import okhttp3.OkHttpClient
import retrofit2.Retrofit

/**
 * This is the wrapper builder to create the [Retrofit] object.
 */
object Retroauth {
  fun <OWNER_TYPE : Any, OWNER : Any, CREDENTIAL_TYPE : Any, CREDENTIAL : Any> setup(
    retrofit: Retrofit,
    authenticator: Authenticator<OWNER_TYPE, OWNER, CREDENTIAL_TYPE, CREDENTIAL>,
    ownerStorage: OwnerStorage<OWNER_TYPE, OWNER, CREDENTIAL_TYPE>,
    credentialStorage: CredentialStorage<OWNER, CREDENTIAL_TYPE, CREDENTIAL>
  ): Retrofit {
    val okHttpClient = retrofit.callFactory().let { callFactory ->
      check(callFactory is OkHttpClient) { "Retroauth only works with OkHttp as Http Client!" }
      callFactory.newBuilder()
        .addInterceptor(CredentialInterceptor(authenticator, ownerStorage, credentialStorage))
        .build()
    }
    return retrofit.newBuilder()
      .client(okHttpClient)
      .build()
  }
}

fun <OWNER_TYPE : Any, OWNER : Any, CREDENTIAL_TYPE : Any, CREDENTIAL : Any> Retrofit.authentication(
  authenticator: Authenticator<OWNER_TYPE, OWNER, CREDENTIAL_TYPE, CREDENTIAL>,
  ownerStorage: OwnerStorage<OWNER_TYPE, OWNER, CREDENTIAL_TYPE>,
  credentialStorage: CredentialStorage<OWNER, CREDENTIAL_TYPE, CREDENTIAL>
): Retrofit = Retroauth.setup(this, authenticator, ownerStorage, credentialStorage)
