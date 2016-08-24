# A simple way of calling authenticated requests using retrofit in android
[![Android Arsenal](https://img.shields.io/badge/Android%20Arsenal-retroauth-brightgreen.svg?style=flat)](http://android-arsenal.com/details/1/2195)
## Dependencies
* [Retrofit](https://github.com/square/retrofit) 2.1.0 (transitive from retroauth-core)
* appcompat-v7 22.1.0 and higher

Method-Count: 224
Field-Count:  53

## What does it do?
If you call a request method, annotated with the authenticated annotation, it'll do the following steps:
* Step 1: Checks if there already is an account in the Android AccountManager. If not, it'll open a LoginActivity (you choose which). If there already is an account, go on with step 2, If there's more than one account open an Dialog to pick an account.
* Step 2: Tries to get the authentication token from the (choosen) account for authorizing the request. If there is no valid token, your LoginActivity will open. After login go to Step 1.
* Step 3: Sends the actual request
* Step 4: By implementing a Provider you can check the response (i.e. a 401 you will be able to refresh the token) and decide if you want to retry the request or not.

## How to use it on Android

Add it as dependency:
```groovy
compile 'com.andretietz.retroauth:retroauth-android:2.1.4'
```
Make sure you're overriding the appcompat dependency if required!
i.e.:
```groovy
compile 'com.android.support:appcompat-v7:24.2.0'
```


### 1. You need to deal with at least 3 different strings
1. An action string which will be used to start your Login 
 * (recommended: use your applicationId for example and add: ".ACTION")
2. An Account-Type string. This should be a unique string! 
 * (recommended: use your applicationId for example and add: ".ACCOUNT")
3. A Token-Type string. It should be a unique string too. 
 * (recommended: use your applicationId for example and add: ".TOKEN")
4. (Optional) Create as many Token-Type Strings as you need.

add them to your strings.xml

```xml
<string name="authentication_ACTION" translatable="false">com.andretietz.retroauth.demo.ACTION</string>
<string name="authentication_ACCOUNT" translatable="false">com.andretietz.retroauth.demo.ACCOUNT</string>
<string name="authentication_TOKEN" translatable="false">com.andretietz.retroauth.demo.TOKEN</string>
... other tokens
```

Change the String keys as you like, but remember renaming them in all the other places too!

 
### 2. Create an Activity (or use one you already have) where the user can login. This Activity must extend from AuthenticationActivity and call finalizeAuthentication when the authentication finished
 i.e. (see Demo for an example)
 
```java
public class LoginActivity extends AuthenticationActivity {
...
private void someLoginMethod() {
     String user;
     String token;
     ... 
     // do login work here and make sure, that you provide at least a user and a token String
     ...
     Account account = createOrGetAccount(user);
     storeToken(account, getString(R.string.authentication_TOKEN), token);
     // or optional
     storeToken(account, getString(R.string.authentication_TOKEN), token, refreshToken);
     // add multiple tokens: storeToken(account, getString(R.string.authentication_TOKEN_X), token2);
     // store some additional userdata (optionally)
     storeUserData(account, "key_for_some_user_data", "very-important-userdata");
     // finishes the activity and set this account to the "current-active" one
     finalizeAuthentication(account);
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
                <action android:name="<your-ACTION-string-defined-in-1>"/>
                <category android:name="android.intent.category.DEFAULT"/>
            </intent-filter>
        </activity>
 ...
 </manifest>
 ```
### 3. Setup an AuthenticationService
There are two ways of doing that:
 
* Option 1:
Extend the AuthenticationService and provide the ACTION-string.
 
```java
public class SomeAuthenticationService extends AuthenticationService {
@Override
public String getLoginAction(Context context) {
    // this is used only to provide the action for the LoginActivity to open
    return context.getString(R.string.authentication_ACTION);
}
}
```
* Option 2.  
Instead of creating you own Service feel free to use the "RetroauthAuthenticationService"
Make sure you define a new string:
```xml
<string name="com.andretietz.retroauth.authentication.ACTION" translatable="false">@string/authentication_ACTION</string>
```
the key of your ACTION-string defined in step 1 is: "com.andretietz.retroauth.authentication.ACTION"
 
In both cases:
Provide a authenticator.xml:
```xml
<account-authenticator xmlns:android="http://schemas.android.com/apk/res/android"
                   android:accountType="@string/authentication_ACCOUNT"
                   android:icon="@mipmap/ic_launcher"
                   android:smallIcon="@mipmap/ic_launcher"
                   android:label="@string/app_name" />
```
 
Add the Service to the Manifest:
If you choose

* Option 1: provide the Service you created in the manifest
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
* Option 2: provide the RetroauthAuthenticationService in the manifest
```xml
      ...
      <service
          android:name="com.andretietz.retroauth.RetroauthAuthenticationService"
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
### 4. Create a Provider implementation
Since every Provider may have a different way of authenticating their request, you have to tell how this should work
 
```java
public class MyProvider implements Provider<Account, AndroidTokenType, AndroidToken> {

 @Override
 public Request authenticateRequest(Request request, AndroidToken androidToken) {
     // this is an example of adding the token to the header of a request 
     return request.newBuilder()
             .header("Authorization", "Bearer " + androidToken.token)
             .build();
 }

 @Override
 public boolean retryRequired(int count, Response response, TokenStorage<Account, AndroidTokenType, AndroidToken> tokenStorage, Account account, AndroidTokenType androidTokenType, AndroidToken androidToken) {
        // this is an optional (sample) implementation
        if (!response.isSuccessful()) {
            if (response.code() == 401) {
                tokenStorage.removeToken(account, androidTokenType, androidToken);
                ...
                // refresh your token using androidToken.refreshToken
                ...
                // store the refreshed token
                tokenStorage.storeToken(account, androidTokenType, new AndroidToken(newAccessToken, newRefreshToken));
                // retry
                return true;
            }
        }
        return false;
 }
}
```
 
### 5. Create your REST interface
 * Add authentication information to it:
 
```java
public interface SomeAuthenticatedService {
 @GET("/some/path")
 Call<ResultObject> someUnauthenticatedCall();

 @Authenticated({R.string.accountType, R.string.tokenType})
 @GET("/some/other/path")
 Call<ResultObject> someAuthenticatedCall();
}
```
 
 * Create the Retrofit object and instantiate it
```java
Retrofit retrofit = new Retroauth.Builder<>(new AndroidAuthenticationHandler(new MyProvider()))
        .baseUrl("https://api.awesome.com/")
        .client(httpClient)
        // add whatever you used to do with retrofit2
        // i.e.:
        .addConverterFactory(GsonConverterFactory.create())
        
        .build();
// create your services
SomeAuthenticatedService service = retrofit.create(SomeAuthenticatedService.class);
// use them
service.someAuthenticatedCall().execute();
```
