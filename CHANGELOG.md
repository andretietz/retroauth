## 3.2.0 ()
* Removing the Retrofit CallAdapter in order to enrich the requests using [this method](https://andretietz.com/2021/04/06/custom-retrofit2-annotations-revisited/)
* using the github api instead of facebook in the demo.
## 3.1.0 (2021-01-22)
* Removing [startup hack](https://andretietz.medium.com/auto-initialize-your-android-library-2349daf06920) with google version of it.
* increasing min SDK level from 14 to 21
## 3.0.1 (2020-02-26)
* opened CredentialInterceptor for use outside the library
## 3.0.0 (2020-01-20)
* Renaming Token -> Credentials
* Converted the project to Kotlin
* When getting the credentials, it CAN automatically read user data items, which are defined in the CredentialType
* API changes
  * Solving problem with multiple authenticated requests.
  * Renamed Provider to CredentialProvider and added functionality
  * Better separation of OwnerStorage and CredentialStorage
* Fixed Demo app which is using Facebook with [scribe](https://github.com/scribejava/scribejava)
* Added functionalities to refresh the credentials before the authenticated call is called.
* Renaming TokenProvider to Authenticator
* Callback method when the account is removed from the Android AccountManager

## 2.3.1 (2018-01-15)
* Updating release script
* Fixing Exception when Exception is thrown during the actual call (was always AuthenticationCanceledException)
* Renamed core project to retroauth instead of retroauth-core

## 2.3.0 (2017-09-07)
* Renaming ContextManager to ActivityManager and focusing on providing a Nullable Activity. No Context will be provided anymore! Due to this change this is a breaking change.
* Doesn't use ContentProvider workaround for getting the Application Object. It seemed nice but was difficult when using multiple processes.

## 2.2.2 (2017-08-21)
* Adding the hashCode method for the AndroidTokenType, so that the locking is actually working.

## 2.2.1 (2017-06-06)
* If Request-Locking is enabled, it's locking (only one request at once) requests over multiple retroauth instances.

## 2.2.0 (2017-05-22)
* Removed Deprecated Methods
* Changes in the CallAdapter implementation update to retrofit 2.3.0

## 2.1.6 (2017-01-09)
* Added functionalities to switch accounts easily

## 2.1.5 (2017-01-05)
* Fixed a bug in the ContextManager. Activity Stack was used a bit "optimistic"

## 2.1.4 (2016-08-24)
* retroauth-android
  * set fixed appcompat minimum version to 22.1.0

## 2.1.3 (2016-08-16)
* retroauth-android
  * added robolectric to enhance test coverage
  * bugfix in ContextManager 

## 2.1.2 (2016-08-02)
* retroauth-android:
  * Authentication can be finalized without finishing the activity itself
  * Adding and removing accounts using the AuthAccountManager can have some optional callbacks, which notifies you, when the system created/removed the account

## 2.1.1 (2016-07-27)
* retroauth-core:
  * Removed method "createType" from TokenStorage
  * Created TokenTypeFactory, which can be passed optionally into the AuthenticationHandler

## 2.1.0 (2016-07-25)
* retroauth-core:
  * breaking improvement
    * Switching from String[] to int[], which is easier to handle on android
* retroauth-android:
  * Some of the methods of the AuthAccountManager don't need a Context anymore
  * removed method "getActiveUserToken" from AuthAccountManager, 'cause it's not necessary anymore
* Update dependencies
  * retrofit 2.1.0 (retroauth-core)
  * appcompat 24.1.1 (retroauth-android)

## 2.0.0 (2016-06-15)

* Complete rebuild, to be able to work with retrofit2
  * Removed rxjava as dependency
  * Works as well with plain java
  * added retroauth-android library (for android accountmanager needs)
  * added java demo (google, javafx)
  * added android demo (google, webview)
  * No Context required for creating the Retrofit object


## 1.0.4 (2015-11-02)

* Demo App:
  * Added Github authentication as an example
  * Permission GET_ACCOUNTS, MANAGE_ACCOUNTS, USE_CREDENTIALS, AUTHENTICATE_ACCOUNTS are now limited to APIs below 23 (No Runtime Permissions to ask the user for anymore)
* Dependency Updates:
  * appcompat 23.1.0
  * rxjava 1.0.15
  * (Demo App:) rxandroid 1.0.1
* Bugfixes:
  * there were several issues regarding the relogin on a 401 on specific request types (blocking/async/rx)


## 1.0.3 (2015-08-29)

* Storing multiple credentials in the AuthenticationActivity
* Adding some sequence diagrams for a better understanding
* Bugfixes:
  * Creating an instance of the LockingStrategy required a protected class as argument. fixed this.

## 1.0.2 (2015-08-19)

* Dependency updates:
  * rxjava 1.0.14
  * appcompat 23.0.0
* Introducing RequestStrategies
  * RequestStrategy
    * The most basic one, just executes the request without retrying
  * RetryAndInvalidateStrategy: based on RequestStrategy
    * Retries the request if it returns with 401 and invalidates the token, which was (obviously) not valid anymore
  * LockingStrategy: based on RetryAndInvalidateStrategy
    * only one request (of a tokentype) is executed at once. this is to prevent multiple login screens.

## 1.0.1 (2015-07-28)

* Bugfix
  * When multiple authenticated requests were called at the same time, and the provided token was invalid at this time, multiple 401's were returned and multiple login activities were opened.
    * when you do multiple authenticated requests, there will be only one executed at one time. This is to avoid multiple 401's and multiple activities to open.
    * when a request has to wait for another one, it'll be executed as soon as the previous one returns.

## 1.0.0 (2015-07-21)

* Bugfixes:
  * There was a major issue causing a crash, when trying to show the account chooser, after creating a new account
  * Unit Tests for the main functionalities
  * Javadocs for all classes

## 0.1.4-beta (2015-07-10)

* Bugfix:
 * Right after the first login, the Token was not correctly appended to the TokenInterceptor
* Lots of documentation

## 0.1.3-beta (2015-07-04)

* Demo App:
 * can have unlimited amount of users (using any username and the password "test"
 * shows the active account in the title

* Bugfix:

    The Token didn't invalidate on `AuthAccountManager#invalidateTokenFromActiveUser`
* Added `AuthAccountManager#getUserData` to get the userdata of an active account, which were setup in `AuthenticationActivity#finalizeAuthentication`

## 0.1.2-beta (2015-07-04)

 * All retrofit request types are supported
  * rxjava
  * blocking
  * async
 * Added AuthAccountManager to simplify the Account handling with retroauth

## 0.1.1-beta (2015-06-30)

  * Multiple Accounts possible

    If the User has multiple accounts setup and an authenticated request is called, the user
    will see an account picker to choose between the accounts or create a new one

## 0.1.0-beta (2015-06-28)

First public release
