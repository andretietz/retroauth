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
interface OwnerManager<OWNER : Any, in TOKEN_TYPE : Any> {
    /**
     * This method creates or gets the user (owner).
     *
     * * If there is no user on the system yet, start the login procedure, this could cause
     * [AuthenticationCanceledException] when the user cancels the login.
     * * If there are multiple users on the system, you should ask the user which one to take.
     * This could also cause an [AuthenticationCanceledException].
     *
     * @param type type of the token
     * @return the owner
     */
    fun createOrGetOwner(type: TOKEN_TYPE): OWNER

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
