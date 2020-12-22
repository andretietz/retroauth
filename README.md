# A simple way of calling authenticated requests using retrofit

[![Android Arsenal](https://img.shields.io/badge/Android%20Arsenal-retroauth-brightgreen.svg?style=flat)](http://android-arsenal.com/details/1/2195)
[![Android Weekly](http://img.shields.io/badge/Android%20Weekly-%23163-2CB3E5.svg?style=flat)](http://androidweekly.net/issues/issue-163)
[![Snapshot build](https://github.com/andretietz/retroauth/workflows/Snapshot%20build/badge.svg)](https://github.com/andretietz/retroauth/actions?query=workflow%3A%22Snapshot+build%22)


I split the project into 2 separate ones.

 * [retroauth](retroauth)
  This is the base implementation, to be used in plain java/kotlin projects.
 * [retroauth-android](retroauth-android/)
  On top of the pure Kotlin implementation there's the Android implementation, which uses the
Android AccountManager in order to store Owners (Accounts) and their Credentials.

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
