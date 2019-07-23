# Android Test Helpers

## Add it as dependency:

```groovy
testImplementation "com.andretietz.retroauth:retroauth-test:X.Y.Z"
```

## Use it in you tests

When creating the `Retroauth.Builder` use the `RetroauthTestHelper.createBuilder()` method to do so. This method creates a RetroauthBuilder that set's up a very basic authentication mechanism so that you can test your request, without mocking all of the interfaces required (`TokenStorage`, `OwnerManager`, `Authenticator`).
by default it adds a test token to every authenticated request. You can make that fit your needs using the optional parameters of
[RetroauthTestHelper.createBuilder](src/main/java/com/andretietz/retroauth/test/RetroauthTestHelper.kt)

```kotlin
    // Create your retrofit instance
    retrofit = RetroauthTestHelper.createBuilder()
      .baseUrl(server.url("/")) // <- example is using okhttp's WebMockServer
      .addConverterFactory(GsonConverterFactory.create())
      .build()
    // and the api interface
    val api = retrofit.create(SomeApi::class.java)
    // Prepare the response
    server.enqueue(MockResponse().setBody("{\"data\": \"testdata\"}"))

    // Execute the call
    val response = api.someAuthCall().execute()
    val data = response.body()
    
    // Test the call
    assertTrue(data != null)
    assertEquals(data!!.data, "testdata")
    assertTrue(server.takeRequest().headers[RetroauthTestHelper.TEST_AUTH_HEADER_NAME] == RetroauthTestHelper.TOKEN)

```

## Intentionally not working

There are a few things that cannot be done using the test implementation. I.e. calling `TestOwnerManager.createOwner` This would assume a login/account creation page. This shouldn't be required in testing anyways. A simple account and a simple token is provided by the implementation and can be handed in as constructor params in `TestOwnerManager`, if required.
