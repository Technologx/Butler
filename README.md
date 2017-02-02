# Butler
Here to serve you an easier way of coding.

##IconRequests
This library contains a singleton that will help process the request and supply callbacks.
The settings are created with a builder, and all the callbacks are handled through EventBus.
Possible events posted: AppLoadingEvent, AppLoadedEvent, AppSelectionEvent, RequestEvent
Each with their own helper methods. Subscribe to these events anywhere!
Please note that all eventbus posts are stickied as a user can always quit the view.
Handle them accordingly and look at the samplea app for further help.

# How to include

Add the following to your root **build.gradle**:

```gradle
allprojects {
	repositories {
		...
		maven { url "https://jitpack.io" }
	}
}
```

And add the following dependencies:

```gradle
dependencies {
        compile 'com.github.AllanWang:Butler:-SNAPSHOT' //Or a specific version/commit
}

```

[JitPack site](https://jitpack.io/#AllanWang/Butler) for reference



# License

This library contains an icon request tool, with code taken from [Icon-Request](https://github.com/afollestad/icon-request), which is a derivative of [PkRequestManager](https://github.com/Pkmmte/PkRequestManager).

```
Copyright (c) 2017 Allan Wang

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

    http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
```