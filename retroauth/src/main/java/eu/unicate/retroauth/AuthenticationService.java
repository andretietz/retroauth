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

package eu.unicate.retroauth;

import android.app.Service;
import android.content.ComponentName;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.XmlResourceParser;
import android.os.Bundle;
import android.os.IBinder;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;

/**
 * You have to extend this service in order to use this library
 */
public class AuthenticationService extends Service {

	@Override
	public IBinder onBind(Intent intent) {
		AccountAuthenticator authenticator = new AccountAuthenticator(this, checkAuthenticationAction(getAuthenticationAction()));
		return authenticator.getIBinder();
	}

	private Intent checkAuthenticationAction(String action) {
		Intent actionIntent;
		// check if there is an action setup
		if(action == null) {
			throw new RuntimeException("You have to add the \"retroauth:authenticationAction\" attribute to your authenticator.xml! It's value should be the full package path of the activity to login");
		}
		actionIntent = buildIntent(action);
		// check if there is an intentfilter set for the action
		ComponentName componentName = actionIntent.resolveActivity(getPackageManager());
		// check if there's an activity that can handle this action
		if(componentName == null) {
			throw new RuntimeException("Could not find Activity to handle your Action: " + action + ".\n" +
					"Please either pass the full canonial activity name to the attribute  \"retroauth:authenticationAction\" in the authenticator.xml\n" +
					"or create an intent filter for your activity\n" +
					"<intent-filter>\n" +
					"<action android:name=\"" + action + "\"/>\n" +
					"<category android:name=\"android.intent.category.DEFAULT\"/>\n" +
					"</intent-filter>");
		}
		return actionIntent;
	}

	private Intent buildIntent(String action) {
		try {
			Class<?> componentClass = Class.forName(action);
			if(!AuthenticationActivity.class.isAssignableFrom(componentClass)) {
				throw new RuntimeException("The Activity: " + action + " used for authentication has to extend " + AuthenticationActivity.class.getSimpleName());
			}
			return new Intent(this, componentClass);
		} catch (ClassNotFoundException e) {
			return new Intent(action);
		}
	}

	private Class<?> checkAndGetClass(String className) {
		Class<?> componentClass;
		try {
			componentClass = Class.forName(className);
			// check if that activity is extending from AuthenticationActivity
			if(!AuthenticationActivity.class.isAssignableFrom(componentClass)) {
				throw new RuntimeException("The Activity: " + className + " used for authentication has to extend " + AuthenticationActivity.class.getSimpleName());
			}
		} catch (ClassNotFoundException e) {
			throw new RuntimeException(e);
		}
		return componentClass;
	}

	private String getAuthenticationAction() {
		String action;
		XmlResourceParser resourceParser = null;
		try {
			ComponentName serviceComponent = new ComponentName(this, this.getClass());
			android.content.pm.ServiceInfo ai = getPackageManager().getServiceInfo(serviceComponent, PackageManager.GET_META_DATA);
			Bundle bundle = ai.metaData;
			int xmlRes = bundle.getInt("android.accounts.AccountAuthenticator");
			resourceParser = getResources().getXml(xmlRes);
			int eventType = resourceParser.getEventType();
			while (eventType != XmlPullParser.END_DOCUMENT) {
				if(eventType == XmlPullParser.START_TAG) {
					if(resourceParser.getName().equals("account-authenticator")) {
						for(int i=0;i<resourceParser.getAttributeCount();i++) {
							if("http://schemas.android.com/apk/res-auto".equals(resourceParser.getAttributeNamespace(i)) &&
									resourceParser.getAttributeName(i).equals("authenticationAction")) {
								int actionRes = resourceParser.getAttributeResourceValue(i, 0);
								if(actionRes != 0) {
									action = getString(actionRes);
								} else {
									action = resourceParser.getAttributeValue(i);
								}
								return action;
							}
						}
					}
				}
				eventType = resourceParser.next();
			}

		} catch (PackageManager.NameNotFoundException | XmlPullParserException | IOException e) {
			e.printStackTrace();
		} finally {
			if (resourceParser != null) {
				resourceParser.close();
			}
		}
		return null;
	}
}
