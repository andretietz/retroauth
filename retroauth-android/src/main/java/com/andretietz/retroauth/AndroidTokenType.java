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

import android.support.annotation.NonNull;

/**
 * represents a token bound to an account
 */
public final class AndroidTokenType {

    public final String tokenType;
    public final String accountType;

    public AndroidTokenType(@NonNull String accountType, @NonNull String tokenType) {
        this.accountType = accountType;
        this.tokenType = tokenType;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        AndroidTokenType that = (AndroidTokenType) o;
        if (!tokenType.equals(that.tokenType)) return false;
        return accountType.equals(that.accountType);
    }

    @Override
    public int hashCode() {
        int result = tokenType.hashCode();
        result = 31 * result + accountType.hashCode();
        return result;
    }

    public static final class Factory implements TokenTypeFactory<AndroidTokenType> {
        private final ContextManager contextManager;

        private Factory() {
            this.contextManager = ContextManager.get();
        }

        public static Factory create() {
            return new Factory();
        }

        @Override
        public AndroidTokenType create(int[] annotationValues) {
            String accountType = contextManager.getContext().getString(annotationValues[0]);
            String tokenType = contextManager.getContext().getString(annotationValues[1]);
            return new AndroidTokenType(accountType, tokenType);
        }
    }
}
