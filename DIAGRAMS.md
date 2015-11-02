## Sequence Diagrams
### When you execute your first authenticated request on a device, this is going to happen inside of retroauth:

![Authenticated Request, while having no account](https://cloud.githubusercontent.com/assets/2174386/9561852/a28b7628-4e58-11e5-82ba-c504979e4c10.png)
### After you have an account and a valid token:

![Authenticated request, using the active account](https://cloud.githubusercontent.com/assets/2174386/9561848/2d7a5598-4e58-11e5-9f0d-0e1c359a6c99.png)

### When there's no current active account but several accounts exist on the device:
(you can force this, by using the Method resetActiveAccount of the AuthAccountManager)

![Authenticated Request, when it's not clear which account to use](https://cloud.githubusercontent.com/assets/2174386/9561858/0ab670d6-4e59-11e5-9ae6-ba5bdf398ab2.png)

### When you have at least on account, an active one is set but your token is not vaild (anymore)

![Authenticated Request, invalid token](https://cloud.githubusercontent.com/assets/2174386/9575401/b3285e10-4fcf-11e5-966f-59dcbb2e8822.png)