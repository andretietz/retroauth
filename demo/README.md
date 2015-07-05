# Demo project
As you'll see, the Demo Project contains only 4 small classes.

## MainActivity
This is an activity as you will have it probably as well. In this I create the AuthRestAdapter and the api service.
it will call the githup api for my repositories. Take a look to the console output here. It should
contain a demo token in the
Http Header:
```
Token: this-is-a-demo-token
```
This token was stored in the

## LoginActivity
It does a fake login without any requests. You'll probably send you own authentication request here,
receive your token and your userdata.


## SomeAuthenticatedService
This is just the interface, being used to call the github api with some fake authentication field
in the header