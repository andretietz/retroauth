/*
 * Copyright (c) 2015 Andre Tietz
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

package eu.unicate.retroauth.annotations;

import android.support.annotation.StringRes;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 * This annotation sets up the account type and the token type of the authenticated
 * requests in the interface itself
 */
@Documented
@Target(TYPE)
@Retention(RUNTIME)
public @interface Authentication {
	/**
	 * @return a Resource String of the Account Type
	 */
	@StringRes int accountType();

	/**
	 * @return a Resource String of the Token Type
	 */
	@StringRes int tokenType();
}
