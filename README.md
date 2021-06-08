# A simple way of calling authenticated requests using retrofit
[![Snapshot build](https://github.com/andretietz/retroauth/workflows/Snapshot%20build/badge.svg)](https://github.com/andretietz/retroauth/actions?query=workflow%3A%22Snapshot+build%22)


I split the project into 2 separate ones.

 * [retroauth](retroauth)
  This is the base implementation, to be used in plain java/kotlin projects.
 * [android-accountmanager](android-accountmanager/)
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
