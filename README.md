# The simple way of calling authenticated requests in retrofit style
### This is a library in beta!
[![Build Status](https://travis-ci.org/Unic8/retroauth.svg?branch=master)](https://travis-ci.org/Unic8/retroauth)
## Dependencies
* [Retrofit](https://github.com/square/retrofit) 1.9.0
* [RxJava](https://github.com/ReactiveX/RxJava) 1.0.12
* appcompat-v7: 22.2.0

## Example:
``` java
@Authentication(accountType = R.string.auth_account_type, tokenType = R.string.auth_token_type)
public interface SomeService {
    @Authenticated
    @GET("/some/path")
    Observable<ResultObject> someAuthenticatedRxJavaCall();

    // or
    @Authenticated
    @GET("/some/path")
    JsonElement someAuthenticatedBlockingCall();

    // or
    @Authenticated
    @GET("/some/path")
    void someAuthenticatedAsyncCall(Callback<JsonElement> callback);
}
```
## What does it do?
If you call an authenticated method of this service, it'll do the following things inside the library:
* Step 1: Checks if there already is an account in the Android AccountManager. If not, it'll open the LoginActivity. If there already is an account, go on with step 2, If there's more than one account open an Dialog to pick an account.
* Step 2: Gets the authentication token from the (choosen) account and adds it to the request header. If there is no valid token, the LoginActivity could open with the pre-filled accounts username
* Step 3: Sends the actual request
* Step 4: If that request fails with an 401 (the only one right now) it invalidates the used token and continues with step 1.

## How to use it?
Add it as dependency:
```groovy
compile 'eu.unicate.android:retroauth:0.1.4-beta'
```

### 1. Create 3 strings in your strings.xml
i.e.
``` xml
<resources>
    ...
	<!-- String for the account type you want to use in your app -->
	<string name="auth_account_type">eu.unicate.retroauth.demo.account</string>
	<!-- String for the type of token you're using. -->
	<string name="auth_token_type">eu.unicate.retroauth.demo.token</string>
	<!-- String for the Action to open the activity to login if necessary -->
	<string name="authentication_action">eu.unicate.auth.action.AUTH</string>
	...
</resources>
```
### 2. Create an Activity (or use one you already have) where the user can login. This Activity must extend from AuthenticationActivity and call finalizeAuthentication when the authentication finished
i.e. (see Demo for an example)

```java
public class LoginActivity extends AuthenticationActivity {
   ...
   private void someLoginMethod() {
       String user;
       String token;
       Bundle additionalUserData; // nullable
       ... // do login work here and make sure, that you provide at least a user and a token String
       // the Token type is the one you defined in Step 1
       finalizeAuthentication(user, getString(R.string.auth_token_type), token, additionalUserData);
   }
   ...
}
```
Make sure your LoginActivity has the intent filter in the manifest:
```xml
<?xml version="1.0" encoding="utf-8"?>
<manifest>
...
       <activity android:name=".LoginActivity">
           <intent-filter>
               <!-- THIS MUST BE THE SAME STRING AS DEFINED IN STEP 1 (sadly the resource string cannot be used for that) -->
               <action android:name="eu.unicate.auth.action.AUTH"/>
               <category android:name="android.intent.category.DEFAULT"/>
           </intent-filter>
       </activity>
...
</manifest>
```
### 3. Implement a very basic AuthenticationService
```java
public class SomeAuthenticationService extends AuthenticationService {
	@Override
	public String getLoginAction(Context context) {
	    // this is used only to provide the action for the LoginActivity to open
		return context.getString(R.string.authentication_action); // <=  This is the String provided in Step 1
	}
}
```
Provide a authenticator.xml:
```xml
<account-authenticator xmlns:android="http://schemas.android.com/apk/res/android"
					   android:accountType="@string/auth_account_type"  <= This is the String provided in Step 1
					   android:icon="@mipmap/ic_launcher"
					   android:smallIcon="@mipmap/ic_launcher"
					   android:label="@string/app_name" />
```

Add the Service to the Manifest:

```xml
        ...
        <service
            android:name=".SomeAuthenticationService"
            android:process=":auth"
            android:exported="false">
            <intent-filter>
                <action android:name="android.accounts.AccountAuthenticator"/>
            </intent-filter>
            <meta-data
                android:name="android.accounts.AccountAuthenticator"
                android:resource="@xml/authenticator"/>
        </service>
        ...
    </application>
</manifest>
```
### 4. Create your REST interface (as you are used to do with retrofit)
* Add authentication information to it:

```java
@Authentication(accountType = R.string.auth_account_type, tokenType = R.string.auth_token_type)
public interface SomeService {
    @GET("/some/path")
    Observable<ResultObject> someUnauthenticatedCall();

    @Authenticated
    @GET("/some/path")
    Observable<ResultObject> someAuthenticatedRxJavaCall();

    // or
    @Authenticated
    @GET("/some/path")
    JsonElement someAuthenticatedBlockingCall();

    // or
    @Authenticated
    @GET("/some/path")
    void someAuthenticatedAsyncCall(Callback<JsonElement> callback);
}
```
### 5. Create your Service
```java
// create your RestAdapter as you would do it with retrofit as well
// just use AuthRestAdapter instead of RestAdapter
AuthRestAdapter restAdapter = new AuthRestAdapter.Builder()
   .setEndpoint("http://some.api.endpoint")
   .setLogLevel(RestAdapter.LogLevel.FULL)
   .build();
...
public class SomeTokenInterceptor extends TokenInterceptor {
    @Override
    public void injectToken(RequestFacade facade, String token) {
        facade.addHeader("Authorization", "Bearer " + token);
    }
}
// (For the Bearertoken, there is also a predefined one TokenInterceptor.BEARER_TOKENINTERCEPTOR)
// more predefined will follow
...
service = restAdapter.create(context, new SomeTokenInterceptor(), SomeAuthenticatedService.class);
// If you want the Login to open, make sure your context is an activity. If you're calling this
// from a service with a Servicecontext the login won't open. This is because the addAccount Method
// requires an Activity to be able to open the (Login)Activity
```

## That's it.

Have fun!

## Pull requests are welcome
Since I am the only one working on that, I would like to know your opinion and/or your suggestions.
Please feel free to create Pull requests!

## LICENSE

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

<http://www.apache.org/licenses/LICENSE-2.0>

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.