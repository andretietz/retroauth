# The simple way of calling authenticated requests in retrofit style
[![Build Status](https://travis-ci.org/andretietz/retroauth.svg?branch=master)](https://travis-ci.org/andretietz/retroauth)
## Dependencies
* [Retrofit](https://github.com/square/retrofit) 2.0.2

Min SDK Version: 14

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
    @Authenticated({"account-type", "token-type"})
    @GET("/some/path")
    Call<ResultObject> someRequest();
}

```
## About
This project was initially created for android. Since retrofit is plain java, you can use this project in plain java too.

Android Developers can go directly to the android subproject.



Sequence Diagrams can be found in the DIAGRAMS.md file

## How to use it?
Add it as dependency:
```groovy
compile 'com.andretietz:retroauth-core:2.0.0'
```

This library provides you a special Builder for your Retrofit Object. This Builder requires you to implement an AuthenticationHander, which contains out of 4 classes to control the authentication.
For the Android-Case most of the following classes are implemented already.


1. A Method-Cache
    The method-cache is a map, in which you can store information of an annotated request. In case of the android implementation this is an account-type and a token-type). You can use a default class if not required different.
    
    ``` java
     methodCache = new MethodCache.DefaultMethodCache<>();
    ```

2. An OwnerManager
    Usually Tokens and other methods required for an authenticated request, belong to a user/owner. The interface requires you to implement a single method which returns the owner by a type of authentication. In case of android this is an Account stored by the account manager. In the java demo I am returning the same string all the time, which means that there
    s only 1 owner.
    
    ``` java
        public class SimpleOwnerManager implements OwnerManager<String, String> {
            @Override
            public String getOwner(String tokenType) throws ChooseOwnerCanceledException {
                // since we don't care about multiuser here, we return the same thing
                return "retroauth";
            }
        }
    ```

3. A Token-Storage
    This is an interface which requires you to implement a storage for tokens or similar authentication data to be able to authenticate a request.
    
    ``` java
        new TokenStorage<String, String, OAuth2AccessToken>() {
            @Override
            public String createType(String[] annotationValues) {
                return null;
            }
    
            @Override
            public OAuth2AccessToken getToken(String owner, String tokenType) throws AuthenticationCanceledException {
                return null;
            }
    
            @Override
            public void removeToken(String owner, String tokenType, OAuth2AccessToken token) {
    
            }
    
            @Override
            public void storeToken(String owner, String tokenType, OAuth2AccessToken token) {
    
            }
        }
    ```

4. A Provider
    Since every provider can have a different approach of authenticating it's requests (i.e. HTTP header, url extension) you can decide how this should be done by implementing this provider specific

Wrap all of the together into the AuthenticationHandler and create your retrofit object

``` java
    AuthenticationHandler<String, String, OAuth2AccessToken> authHandler = new AuthenticationHandler<>(
            new MethodCache.DefaultMethodCache<>(),
            new SimpleOwnerManager(), xmlTokenStorage, provider
    );

    Retrofit retrofit = new Retroauth.Builder<>(authHandler)
            .baseUrl(<some-base-url>)
            .build();

```


Take a look at the Java8 Demo implementation, which uses java-scribe to authenticate against the google-apis

## Pull requests are welcome
Since I am the only one working on that, I would like to know your opinion and/or your suggestions.
Please feel free to create Pull requests!

## LICENSE
```
Copyrights 2015 André Tietz

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