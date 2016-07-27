# A simple way of calling authenticated requests using retrofit
[![Build Status](https://www.bitrise.io/app/d4189e3709bdf16d.svg?token=KpeuDTgCOEWgfL4RoZaVLQ&branch=master)](https://www.bitrise.io/app/d4189e3709bdf16d)
## Dependencies
* [Retrofit](https://github.com/square/retrofit) 2.1.0

## Example:
Your services using retrofit:
``` java
public interface SomeService {
    @GET("/some/path")
    Call<ResultObject> someRequest();
}
```
Your services using retroauth:
``` java
public interface SomeService {
    int ACCOUNT_TYPE = 1;
    int TOKEN_TYPE = 2;
    @Authenticated({ACCOUNT_TYPE, TOKEN_TYPE})
    @GET("/some/path")
    Call<ResultObject> someRequest();
}

```
If you're an Android Developer feel free to go directly to the [android project](retroauth-android/).
## How to use it (Java)?

Add it as dependency:
```groovy
compile 'com.andretietz:retroauth-core:2.1.1'
```

An Authentication with this library requires 3 generic classes, which you should aware of, before implementing. You can use whatever you want, for explanation reasons I'll use their generic names

 * A class: TOKEN_TYPE - you will generate this class out of the information, the Annotation on the method provides you
 * A class: TOKEN - This is a Token which you'll need to authenticate your requests with
 * A class: OWNER - The owner that owns the Token after the login. 

A common scenario: every OWNER owns mulitple TOKENs of multiple TOKEN_TYPEs
 
### 1. Implement an OwnerManager

``` java
public interface OwnerManager<OWNER, TOKEN_TYPE> {
    OWNER getOwner(TOKEN_TYPE type) throws ChooseOwnerCanceledException;
}
```
 * If the owner does not exist, return null. 
 * If there are multiple owners ask the user to choose one.
   * throw an ChooseOwnerCanceledException when the user canceled choosing.


### 2. Implement a TokenStorage
``` java
public interface TokenStorage<OWNER, TOKEN_TYPE, TOKEN> {
    TOKEN_TYPE createType(int[] annotationValues);
    TOKEN getToken(OWNER owner, TOKEN_TYPE type) throws AuthenticationCanceledException;
    void storeToken(OWNER owner, TOKEN_TYPE type, TOKEN token);
    void removeToken(OWNER owner, TOKEN_TYPE type, TOKEN token);
}
```
 * "createType" - create your custom token type out of the values you're using in the annotation
 * "getToken" - get the token
   * When owner doesn't exist, create one
   * When token doesn't exist, request one
 * "storeToken" - store a token of a token type
 * "removeToken" - remove the token of a token type


### 3. Implement a MethodCache (optional)

``` java
public interface MethodCache<TOKEN_TYPE> {
    void register(int requestIdentifier, TOKEN_TYPE type);
    TOKEN_TYPE getTokenType(int requestIdentifier);
}
```
This should be a simple key value store. If you don't want implement this by yourself, use the default implementation.


### 4. Implement a Provider
``` java
public interface Provider<OWNER, TOKEN_TYPE, TOKEN> {
    Request authenticateRequest(Request request, TOKEN token);
    boolean retryRequired(int count, Response response,
                          TokenStorage<OWNER, TOKEN_TYPE, TOKEN> tokenStorage, OWNER owner, TOKEN_TYPE type, TOKEN token);
}
```

* "authenticateRequest" - implement the provider specific modification of the request to authenticate your request ([okhttp3.Request](https://github.com/square/okhttp/blob/master/okhttp/src/main/java/okhttp3/Request.java))
* "retryRequired" - take a look onto the response and decide by yourself if you want the request to be retried or not
  * This method is a perfect place for refreshing tokens if required.
    * Example: You get a 401 response, you can use the TokenStorage object to remove the current Token (cause it's invalid), call a token refresh endpoint and store the new token also using the TokenStorage.
    return true to retry the whole request and it shouldn't return a 401 anymore. For details see the [android example](retroauth-core/).

Wrap all of the together into the AuthenticationHandler and create your retrofit object

``` java
    AuthenticationHandler<OWNER, TOKEN_TYPE, TOKEN> authHandler = new AuthenticationHandler<>(
            new MethodCache.DefaultMethodCache<>(),
            new MyOwnerManager(), myTokenStorage, myCustomProvider
    );

    Retrofit retrofit = new Retroauth.Builder<>(authHandler)
            .baseUrl(<some-base-url>)
            .build();

```


Take a look at the Java8 Demo implementation, which uses java-scribe to authenticate against the google-apis

## Pull requests are welcome
Since I am the only one working on that, I would like to know your opinion and/or your suggestions.
Please feel free to create Pull-Requests!

## LICENSE
```
Copyrights 2016 André Tietz

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

<http://www.apache.org/licenses/LICENSE-2.0>

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
```