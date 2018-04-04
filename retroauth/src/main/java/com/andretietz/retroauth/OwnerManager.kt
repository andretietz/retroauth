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
 * Since every token belongs to a specific user, this users have to be managed.
 */
interface OwnerManager<in OWNER_TYPE : Any, OWNER : Any, in TOKEN_TYPE : Any> {

    /**
     * Creates an OWNER of a specific [OWNER_TYPE] for a specific [TOKEN_TYPE]
     */
    @Throws(AuthenticationCanceledException::class)
    fun createOwner(ownerType: OWNER_TYPE, tokenType: TOKEN_TYPE, callback: OwnerManager.Callback? = null): OWNER

    /**
     * @return OWNER if exists
     */
    fun getOwner(ownerType: OWNER_TYPE, ownerName: String): OWNER?

    fun getActiveOwner(ownerType: OWNER_TYPE): OWNER?

    fun openOwnerPicker(ownerType: OWNER_TYPE, tokenType: TOKEN_TYPE): OWNER?

    fun switchActiveOwner(ownerType: OWNER_TYPE, owner: OWNER? = null)

    /**
     * Removes the given owner from the system.
     *
     * @param owner the owner to remove.
     * @param callback Optional to get notified when the removal is complete.
     */
    fun removeOwner(owner: OWNER, callback: Callback? = null)

    interface Callback {
        fun done(success: Boolean)
    }
}
