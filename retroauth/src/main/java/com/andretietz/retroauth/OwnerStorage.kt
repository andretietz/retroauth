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
 * Since every credential belongs to a specific user, this users have to be managed.
 */
interface OwnerStorage<OWNER : Any> {

  /**
   * Creates an [OWNER]  for a specific [credentialType].
   * So open a login and let the user login.
   *
   * @param credentialType Type of credential you want to open the login for.
   *
   * @return [OWNER] which was created or null if canceled
   */
  suspend fun createOwner(
    credentialType: String
  ): OWNER?

  /**
   * Returns the owner if exists
   *
   * @param ownerName name of the owner you want to receive.
   *
   * @return [OWNER] if the owner exists on the system. If not, return `null`.
   */
  fun getOwner(ownerName: String): OWNER?

  /**
   *
   * @return [OWNER] that is currently active (important for multi user systems i.e. there could be
   * multiple users logged in, but there's only one active). If there's no user currently
   * active return `null`
   */
  fun getActiveOwner(): OWNER?

  /**
   *
   * @return a list of [OWNER]s of the given type
   */
  fun getOwners(): List<OWNER>

  /**
   * Switches the active owner. If the [owner] is `null`, it resets the
   * active owner. So there won't be an active user.
   *
   * @param owner to which to switch
   */
  fun switchActiveOwner(owner: OWNER? = null)

  /**
   * Removes the given owner from the system.
   *
   * @param owner the owner to remove.
   *
   * @return `true` when successfully removed, `false` otherwise.
   */
  fun removeOwner(owner: OWNER): Boolean

}
