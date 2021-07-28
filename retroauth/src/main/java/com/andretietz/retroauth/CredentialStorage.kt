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

import java.util.concurrent.Future

/**
 * This is the interface of a credential storage.
 */
interface CredentialStorage<in OWNER : Any, CREDENTIAL : Any> {

  /**
   * This method returns an authentication credential that is stored locally.
   *
   * @param owner The owner type of the credential you want to get
   * @param type the type of the Credentials you want to get
   * @return the Credentials to authenticate your request with
   */
  fun getCredentials(
    owner: OWNER,
    type: CredentialType,
    callback: Callback<CREDENTIAL>? = null
  ): Future<CREDENTIAL>

  /**
   * Removes the credentials of a specific type and owner from the credentials storage.
   *
   * @param owner Owner of the Credentials
   * @param type Type of the Credentials
   * @param credentials Credentials to remove
   */
  fun removeCredentials(owner: OWNER, type: CredentialType, credentials: CREDENTIAL)

  /**
   * Stores a credentials of a specific type and owner to the credentials storage.
   *
   * @param owner Owner of the Credentials
   * @param type Type of the Credentials
   * @param credentials Credentials to store
   */
  fun storeCredentials(owner: OWNER, type: CredentialType, credentials: CREDENTIAL)
}
