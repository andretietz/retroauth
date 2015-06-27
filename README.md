# The Simple way of calling authenticated Requests with retrofit (and rxjava)
## WTF is the Problem?
Most of you propably know, that in most of the Android applications, have a Backend to talk to. and let's say 90% of the calls you are calling to that Backend are authenticated.

The problem is that each time you call some authenticated request something could go wrong. i.e.:
* The User was never logged in
* The Users Authentication Token is invalid

The next thing is handling Useraccounts on Android devices seems like a huge deal.

This library should help you getting around this problems.

## How to use it?
Note: just for the sake of simplicity, we'll assume that the Github call to "/users/{user}/repos" is an authenticated one. (if you have a better idea, feel welcome to pullrequest)

1. Create an Activity where the user can login. This Activity should extend from AuthenticationActivity
 * When the login finished call AuthenticationActivity.finalizeLogin to pass the data to the AccountManager
2. Create 3 Strings in your strings.xml
 1. A String that describes your Type of account. This String should be unique, it'll not directly be shown to the user. In the Demo I choose: "eu.unicate.demo.account"
 2. A String (at least one) that is some kind of ID for the Type of token you are going to use. In the Demo I choose: "eu.unicate.demo.token"
 3. A String that is used as Android Action to open your Activity
3. Create your Retrofit RestAPI in an interface as you've done it before
``` java
public interface GithubService {
	@GET("/users/{user}/repos")
	Observable<List<JsonElement>> listRepos(@Path("user") String user);
}
```


## Dependencies
* Retrofit 1.9.0
* RxJava 1.0.12
* appcompat-v7: 22.2.0