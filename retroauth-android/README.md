## What does it do?
If you call a request method, annotated with the authenticated annotation, it'll do the following steps:
* Step 1: Checks if there already is an account in the Android AccountManager. If not, it'll open a LoginActivity (you choose which). If there already is an account, go on with step 2, If there's more than one account open an Dialog to pick an account.
* Step 2: Tries to get the authentication token from the (choosen) account to add it to the request header. If there is no valid token, your LoginActivity could open with the pre-filled accounts username. After login go to Step 1.
* Step 3: Sends the actual request
* Step 4: If the request fails with an 401 (by default, but changeable) it invalidates the used token in the Android AccountManager and continues with step 1.

### 1. Create 3 strings in your strings.xml
 
 
 
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
         storeToken(account, getString(R.string.auth_token_type), token);
         // add multiple tokens: storeToken(account, getString(R.string.auth_token_type2), token2);
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
 ### 4. Create your REST interface
 * Add authentication information to it:
 
 ```java
 @Authentication(accountType = R.string.auth_account_type, tokenType = R.string.auth_token_type)
 public interface SomeAuthenticatedService {
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
 // create your RestAdapter just use AuthRestAdapter instead of RestAdapter
 // it provides all functionality as the original one
 AuthRestAdapter restAdapter = new AuthRestAdapter.Builder()
    .setEndpoint("http://some.api.endpoint")
    .setLogLevel(RestAdapter.LogLevel.FULL)
    .build();
 ...
 service = restAdapter.create(context, TokenInterceptor.BEARER_TOKENINTERCEPTOR, SomeAuthenticatedService.class);
 // If you want the Login to open, make sure your context is an activity. If you're calling this
 // from a service with a Service-context or even with the application context, the login won't open.
 // This is because the addAccount Method requires an Activity to be able to open the (Login)Activity
 ```
