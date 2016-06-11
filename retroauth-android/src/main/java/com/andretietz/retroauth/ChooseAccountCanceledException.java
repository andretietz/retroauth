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
 * Thrown in {@link OwnerManager#getOwner(Object)}, in case the system provides more than one user and the current user
 * canceled choosing the right one
 */
public class ChooseAccountCanceledException extends Exception {
    public ChooseAccountCanceledException() {
    }

    public ChooseAccountCanceledException(String detailMessage) {
        super(detailMessage);
    }

    public ChooseAccountCanceledException(String detailMessage, Throwable throwable) {
        super(detailMessage, throwable);
    }

    public ChooseAccountCanceledException(Throwable throwable) {
        super(throwable);
    }
}
