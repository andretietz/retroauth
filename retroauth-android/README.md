# A simple way of calling authenticated requests using retrofit in android
[![Android Arsenal](https://img.shields.io/badge/Android%20Arsenal-retroauth-brightgreen.svg?style=flat)](http://android-arsenal.com/details/1/2195)
## Dependencies
* [Retrofit](https://github.com/square/retrofit) 2.3.0 (transitive from retroauth-core)
* appcompat-v7 23.0.0
* kotlin-stdlib 1.2.30

Method-Count: 348
Field-Count:  77

## What does it do?
If you call a request method, annotated with the authenticated annotation, it'll do the following steps:
* Step 1: Checks if there already is an account in the Android AccountManager. If not, it'll open a LoginActivity (you choose which). If there already is an account, go on with step 2, If there's more than one account open an Dialog to pick an account.
* Step 2: Tries to get the authentication token from the (choosen) account for authorizing the request. If there is no valid token, your LoginActivity will open. After login go to Step 1.
* Step 3: If no Login was required (token exists already), it sends the actual request.
* Step 4: By implementing a Authenticator you can check the response (i.e. a 401 you will be able to refresh the token) and decide if you want to retry the request or not.

## How to use it?

Add it as dependency:
```groovy
implementation 'com.andretietz.retroauth:retroauth-android:x.y.z'
```
Make sure you're overriding the appcompat dependency if required!
i.e.:
```groovy
compile 'com.android.support:appcompat-v7:x.y.z'
```

## Setup
### 1. Define an Account-Type String
The Account-Type should be unique for an app or company, depending on if you want to share the account in multiple apps of your company or not.
I recommend using something like ```your.company.id.ACCOUNT```.
### 2. Define an Authentication Action.
We'll use this String later in order to start our Login-Activity using an intent-filter in the manifest.
This could be something like ```your.company.id.ACTION```
### 3. Create an Authentication Service.
This Service is started whenever the Android OS is asked for a login of the in #1 provided Account-Type. It then uses the Action-String defined in #2 to show the Login.
This must be a service since you can add create accounts within the account-settings.

This is a very small implementation, that could look like this:
```kotlin
class DemoAuthenticationService : AuthenticationService() {
    override fun getLoginAction(): String = "your.company.id.ACTION"
}

```
### 4. Creating the link to the authenticator
With this xml in the res/xml folder of our project we tell the Android OS that there is an authenticator for our Account-Type (defined in #1)
If you provide multiple account types, you need to provide multiple authenticator xmls

Here's an example. Make sure you're replacing the accountType with your own.
```xml
<account-authenticator xmlns:android="http://schemas.android.com/apk/res/android"
                   android:accountType="your.company.id.ACCOUNT"
                   android:icon="@mipmap/ic_launcher"
                   android:smallIcon="@mipmap/ic_launcher"
                   android:label="@string/app_name" />
```

### 5. Gluing the Service and the Authenticator together.

For that we need to add the Service we created in #3 into the manifest:
Note that there's an additional `meta-data` tag which provides the xml we created in #4. If you have multiple xml's you need to provide additional Services.

```xml
     <service
         android:name=".DemoAuthenticationService"
         android:exported="false">
         <intent-filter>
             <action android:name="android.accounts.AccountAuthenticator"/>
         </intent-filter>
         <meta-data
             android:name="android.accounts.AccountAuthenticator"
             android:resource="@xml/authenticator"/>
     </service>
```

### 6. Provide a LoginActivity
Make sure you're extending it from the `AuthenticationActivity`
```kotlin
class LoginActivity : AuthenticationActivity() {
    ...
    fun someLoginMethod() {
        val user: String
        val token: String
        ...
        // do login work here and make sure, that you provide at least a user and a token String
        ...
        Account account = createOrGetAccount(user);
        storeToken(
                account,
                tokenType,  // AndroidTokenType
                token,      // String as you get it from your Authenticator implementation
                mapOf(
                        "some-key" to "some-value"
                )
        // store some additional userdata (optionally)
        storeUserData(account, "key_for_some_user_data", "very-important-userdata");
        // finishes the activity and set this account to the "current-active" one
        finalizeAuthentication(account);
    }
    ...
}
```
and add it also in the manifest using a special `intent-filter`. This `intent-filter` should
as `action:name` contain the Action String you defined in #2

 ```xml
 <?xml version="1.0" encoding="utf-8"?>
 <manifest>
 ...
        <activity android:name=".LoginActivity">
            <intent-filter>
                <action android:name="your.company.id.ACTION"/>
                <category android:name="android.intent.category.DEFAULT"/>
            </intent-filter>
        </activity>
 ...
 </manifest>
 ```

## Usage
### Create an Authenticator implementation
For the Android Implementation you need to create an Authenticator:
```kotlin
class YourAuthenticator
    : Authenticator<String, Account, AndroidTokenType, AndroidToken>() {
```

There are 3 Methods required to implement:
* The Owner-Type
```kotlin
    override fun getOwnerType(annotationOwnerType: Int): String
```
This method provides us the ownerType that has been setup in the `@Authenticated` annotation.
The value is optional! So if you don't need it, don't use it. A reason to use it could be, you need to use multiple ownerTypes on one endpoint.

* The Token-Type
```kotlin
    override fun getTokenType(annotationTokenType: Int): AndroidTokenType {
        return AndroidTokenType(
            "your.company.id.TOKEN_TYPE",
            setOf(
                "some optional",
                "keys",
                "which a token provides"
            )
        )
    }

```
Note that when getting an AndroidToken it only contains the data, which is loaded
using this set of optional keys.

* Authenticate the request
```kotlin
    override fun authenticateRequest(request: Request, token: AndroidToken): Request {
        return request.newBuilder()
                .header("Authorization", "Bearer " + token.token)
                .build()
    }
```
 
### 5. Create your REST interface
```kotlin
interface SomeAuthenticatedService {
 @GET("/some/path")
 fun someUnauthenticatedCall(): Call<ResultObject>

 @Authenticated
 @GET("/some/other/path")
 fun someAuthenticatedCall(): Call<ResultObject>
}
```
 
 * Create the Retrofit object and instantiate it
```java
Retrofit retrofit = RetroauthAndroidBuilder.create(application, YourAuthenticator()))
        .baseUrl("https://api.awesome.com/")
        // add whatever you used to do with retrofit2
        // i.e.:
        .addConverterFactory(GsonConverterFactory.create())
        
        .build();
// create your services
val service = retrofit.create(SomeAuthenticatedService.class)
// use them
service.someAuthenticatedCall().execute()
```
