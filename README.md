# The simple way of calling authenticated requests in retrofit style
### This is a library in beta!
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
compile 'eu.unicate.android:retroauth:0.1.3-beta'
```

### 1. Create AccountType and TokenType(s)
i.e.
``` xml
<resources>
    ...
	<!-- String for the account type you want to use in your app -->
	<string name="auth_account_type">eu.unicate.retroauth.demo.account</string>
	<!-- String for the type of token you're using. -->
	<string name="auth_token_type">eu.unicate.retroauth.demo.token</string>
	...
</resources>
```
### 2. Add Service to manifest
```xml
        ...
        <service
            android:name="eu.unicate.retroauth.AuthenticationService"
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
We need an authenticator.xml provided as meta-data in the manifest. This tells the Android System
which Activity to call when an Account of the given accountType is requested.

Make sure, that your authenticator.xml provides the activity action to open the login
```xml
<account-authenticator xmlns:android="http://schemas.android.com/apk/res/android"
                       xmlns:retroauth="http://schemas.android.com/apk/res-auto"
                       retroauth:authenticationAction="some.path.to.LoginActivity"
					   android:accountType="@string/auth_account_type"  <= This is the String provided in Step 1
					   android:icon="@mipmap/ic_launcher"
					   android:smallIcon="@mipmap/ic_launcher"
					   android:label="@string/app_name" />
```
The authenticationAction can be:
* Full canonial path of the login activity
* Custom Action

### 2. Create an Activity (or use one you already have) where the user can login.
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


Make sure your LoginActivity is in the manifest:
```xml
<?xml version="1.0" encoding="utf-8"?>
<manifest>
...
       <!-- if the fullpath is setup as retroauth:authenticationAction in the authenticator.xml -->
       <activity android:name=".LoginActivity" />

       <!-- OR if a custom action is setup as retroauth:authenticationAction in the authenticator.xml -->
       <activity android:name=".LoginActivity" >
           <intent-filter>
               <action android:name="my.custom.action"/>
               <category android:name="android.intent.category.DEFAULT"/>
           </intent-filter>
       </activity>
...
</manifest>
```

### 3. Create your REST interface
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

### 4. Create your Service
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

Have fun trying!

## What's left to do?
* JavaDoc
* Tests. Right now there are no tests whatsoever.
* Proper Exceptions


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