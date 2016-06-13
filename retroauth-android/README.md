# A simple way of calling authenticated requests using retrofit in android
[![Build Status](https://travis-ci.org/andretietz/retroauth.svg?branch=master)](https://travis-ci.org/andretietz/retroauth)
## Dependencies
* [Retrofit](https://github.com/square/retrofit) 2.0.2
* appcompat-v7 23.4.0

## What does it do?
If you call a request method, annotated with the authenticated annotation, it'll do the following steps:
* Step 1: Checks if there already is an account in the Android AccountManager. If not, it'll open a LoginActivity (you choose which). If there already is an account, go on with step 2, If there's more than one account open an Dialog to pick an account.
* Step 2: Tries to get the authentication token from the (choosen) account for authorizing the request. If there is no valid token, your LoginActivity will open. After login go to Step 1.
* Step 3: Sends the actual request
* Step 4: By implementing a Provider you can check the response (i.e. a 401 you will be able to refresh the token) and decide if you want to retry the request or not.

### 1. You need to deal with at least 3 different strings
1. An action string which will be used to start your Login (recommended: use your applicationId for example and add: ".ACTION")
2. An Account-Type string. This should be a unique string! (recommended: use your applicationId for example and add: ".ACCOUNT")
3. A Token-Type string. It should be a unique string too. (recommended: use your applicationId for example and add: ".TOKEN")
4. (Optional) Create as many Token-Type Strings as you need.
 
### 2. Create an Activity (or use one you already have) where the user can login. This Activity must extend from AuthenticationActivity and call finalizeAuthentication when the authentication finished
 i.e. (see Demo for an example)
 
```java
public class LoginActivity extends AuthenticationActivity {
...
private void someLoginMethod() {
     String user;
     String token;
     ... // do login work here and make sure, that you provide at least a user and a token String
     // the Token type is the one you defined in Step 1
     Account account = createOrGetAccount(user);
     storeToken(account, "<your-TOKEN-string-defined-in-step-1>"), token);
     // or optional
     storeToken(account, getString(R.string.auth_token_type), token, refreshToken);
     // add multiple tokens: storeToken(account, "<your-TOKEN-string-defined-in-step-X>", token2);
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
 There are multiple ways of doing that:
 
 * Option 1:
 Extend the AuthenticationService and provide the ACTION-string.
 ```java
 public class SomeAuthenticationService extends AuthenticationService {
 	@Override
 	public String getLoginAction(Context context) {
 	    // this is used only to provide the action for the LoginActivity to open
 		return "<your-ACTION-string-defined-in-step-1>"; // use context.getString instead if you like
 	}
 }
 ```
 * Option 2.  
 Instead of creating you own Service feel free to use the "RetroauthAuthenticationService"
 Make sure you define a new string:
 
     <string name="com.andretietz.retroauth.authentication.ACTION" translatable="false"><your-ACTION-string-defined-in-step-1></string>
 the key of your ACTION-string defined in step 1 is: "com.andretietz.retroauth.authentication.ACTION"
 
 In both cases:
 Provide a authenticator.xml:
  ```xml
  <account-authenticator xmlns:android="http://schemas.android.com/apk/res/android"
  					   android:accountType="<your-ACCOUNT-string-defined-in-step-1>"
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
        // implementing this is optional
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
 
     @Authenticated({"<your-account-type>", "<token-required-for-this-call>"})
     @GET("/some/other/path")
     Call<ResultObject> someAuthenticatedCall();
 }
 ```
 ### 5. Create your Retrofit Object and your calls
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
