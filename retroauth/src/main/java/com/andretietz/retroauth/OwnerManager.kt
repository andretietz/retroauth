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
interface OwnerManager<out OWNER, in TOKEN_TYPE> {
    /**
     * This method should be used to figure out which user authenticates a request.
     *
     * * If there is no user on the system yet, start the login procedure, this could cause
     * [AuthenticationCanceledException] when the user cancels the login.
     * * If there are multiple users on the system, you should ask the user which one to take. This could cause
     * an [AuthenticationCanceledException].
     *
     * @param type type of the token
     * @return the owner of the token
     */
    fun getOwner(type: TOKEN_TYPE): OWNER
}
