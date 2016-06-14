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
 * Since every token belongs to a specific user, this users have to be managed.
 */
public interface OwnerManager<OWNER, TOKEN_TYPE> {
    /**
     * This method should be used to figure out which user should be authenticate a request.
     * If you're on multi-user systems, you should ask the user to choose which owner
     * he wants to use to authenticate requests. if the user cancels to choose the owner, throw
     * {@link ChooseOwnerCanceledException}, if there's no owner return <code>null</code>
     *
     * @param type type of the token
     * @return the owner of the token of the give token type or <code>null</code>
     * @throws ChooseOwnerCanceledException when the user cancels to choose the owner
     */
    OWNER getOwner(TOKEN_TYPE type) throws ChooseOwnerCanceledException;
}
