# The Simple way of calling authenticated Requests with retrofit (and rxjava)
## Example:
``` java
@Authentication(accountType = R.string.auth_account_type, tokenType = R.string.auth_token_type)
public interface SomeService {
    @Authenticated
	@GET("/some/{user}/path")
	Observable<List<JsonElement>> someCall(@Path("user") String user);
}
```
## What does it do?
If you call an authenticated Method of this Service it'll do the following things under the hood:
1. Check if there is an Account in the Android AccountManager already. If not, it'll open the LoginActivity (starts the request after successful login again). If there is an account already go on with step 2
2. Get the authentication token from the account (AccountManager) and adds it to the request header
3. Send the actual request
4. If that request fails with an 401 (the only one right now) it invalidates the Token you used and the Login will open again to refresh the token
## How to use it?
### 1. Create 3 Strings in your strings.xml
i.e.
``` xml
<?xml version="1.0" encoding="utf-8"?>
<resources>
	<!-- String for the account type you want to use in your app -->
	<string name="auth_account_type">eu.unicate.retroauth.demo.account</string>
	<!-- String for the type of token you're using. -->
	<string name="auth_token_type">eu.unicate.retroauth.demo.token</string>
	<!-- String for the Action to open the activity to login if necessary -->
	<string name="authentication_action">eu.unicate.auth.action.AUTH</string>
</resources>
```
### 2. Create an Activity (or use one you have already) where the user can login. This Activity must extend from AuthenticationActivity and call finalizeAuthentication when the authentication finished
i.e. (see Demo for an example)

```java
public class LoginActivity extends AuthenticationActivity {
   ...
   private void someLoginMethod() {
       String user;
       String token;
       Bundle additionalUserData; // nullable
       ... // do login work here and make sure, that you provide at least a user and a token String
       finalizeAuthentication(user, getString(R.string.auth_token_type), token, additionalUserData);
   }
   ...
}
```

### 3. Make sure your LoginActivity has the intent filter in the manifest:
```xml
<?xml version="1.0" encoding="utf-8"?>
<manifest>
...
       <activity android:name=".LoginActivity">
           <intent-filter>
               <!-- THIS MUST BE THE SAME STRING AS DEFINED IN YOUR strings.xml -->
               <action android:name="eu.unicate.auth.action.AUTH"/>
               <category android:name="android.intent.category.DEFAULT"/>
           </intent-filter>
       </activity>
...
</manifest>
```
### 4. Create your Rest Interface (as you are used to do with retrofit)
* Add Authentication Information to it:
```java
@Authentication(accountType = R.string.auth_account_type, tokenType = R.string.auth_token_type)
public interface SomeService {
    @GET("/some/path")
    Observable<List<JsonElement>> someUnauthenticatedCall();

    @Authenticated
    @GET("/some/path")
    Observable<List<JsonElement>> someAuthenticatedCall();
}
```
#### WARNING: it will only work with calls that return an observable (for now)
### 5. Create your RestAdapter in Retrofit style:
```java
AuthRestAdapter restAdapter = new AuthRestAdapter.Builder()
   .setEndpoint("http://some.api.endpoint")
   .setLogLevel(RestAdapter.LogLevel.FULL)
   .build();
```

### 6. Create a TokenInterceptor
```java
public class SomeTokenInterceptor extends TokenInterceptor {
    @Override
    public void injectToken(RequestFacade facade, String token) {
        facade.addHeader("Authorization", "Bearer " + token);
    }
}
```
(For the Bearertoken, there is also a predefined one TokenInterceptor.BEARER_TOKENINTERCEPTOR)

### 7. Create your Service
```java
service = restAdapter.create(context, new SomeTokenInterceptor(), SomeAuthenticatedService.class);
```

That's it.

If you now call an authenticated Method of your Service it'll:
1. Check if there is an Account already
 * If not, it'll open the LoginActivity using the Action String you provided
  * When you call finalizeAuthentication it'll create an account and store the provided token as such
 * If so go on with 2.
2. Get the token from the account (by the tokentype provided in the strings)
* Use the TokenInterceptor you provided to add the Token to your Request Header
3. Send the actual request
4. If that request fails with an 401 (the only one right now) it invalidates the Token you used and the Login will open again to refresh the token

## Dependencies
* Retrofit 1.9.0
* RxJava 1.0.12
* appcompat-v7: 22.2.0

## What's left todo?
* It can handle max. one Account. This is not very bad I am thinking about it (please give me your impressions as well)
* Only rxjava methods can be used to be authenticated. This is not that nice, better would be if it supports all of the. Right now there should be an exception thrown
* Tests. Right now there are no tests whatsoever. Not only this is a reason to NOT USE THIS LIBRARY IN PRODUCTION YET
* Multiple tokentypes in one class is not possible right now. If you really need that, just create 2 different service interfaces, this should work (as well untested ;) )


## Pull requests are Welcome
Since I am the only one working on that, I would like to know your opinion and or your Suggestions
Please feel free to create Pull requests!