# A simple way of calling authenticated requests using retrofit

[![Build Status](https://www.bitrise.io/app/d4189e3709bdf16d.svg?token=KpeuDTgCOEWgfL4RoZaVLQ&branch=master)](https://www.bitrise.io/app/d4189e3709bdf16d)
[![Android Arsenal](https://img.shields.io/badge/Android%20Arsenal-retroauth-brightgreen.svg?style=flat)](http://android-arsenal.com/details/1/2195)

I split the project into two separate ones. 

 * [retroauth-core](retroauth-core/)
  This is the base implementation, to be used in plain java.
 * [retroauth-android](retroauth-android/)
  On top of the Java implementation there's the Android implementation, which makes use of the
Android AccountManager.

![Dependency Graph](https://cloud.githubusercontent.com/assets/2174386/16000555/4d82f452-314d-11e6-9807-7ba0dbbb7f96.png)


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