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

package eu.unicate.retroauth.strategies;

import rx.Observable;

/**
 * This class implements the most basic request strategy.
 * It does no retrying.
 */
public class RequestStrategy {
	/**
	 * This is to modify the request observable. In the easiest case you just return the request
	 * that comes in.
	 * This is used to extend the requests as it is done i.e. in the {@link RetryAndInvalidateStrategy}
	 * or in the {@link LockingStrategy}
	 *
	 * @param request the request that is supposed to be executed
	 * @return The request as you want it to be
	 */
	public <T> Observable<T> execute(Observable<T> request) {
		return request;
	}
}
