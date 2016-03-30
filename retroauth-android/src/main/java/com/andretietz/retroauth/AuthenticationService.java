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

package com.andretietz.retroauth;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.IBinder;

/**
 * You have to extend this service in order to use this library
 */
public abstract class AuthenticationService extends Service {

	@Override
	public IBinder onBind(Intent intent) {
		AccountAuthenticator authenticator = new AccountAuthenticator(this, getLoginAction(this));
		return authenticator.getIBinder();
	}

	/**
	 * @param context a valid context
	 * @return An Action String to open the activity to login
	 */
	public abstract String getLoginAction(Context context);
}
