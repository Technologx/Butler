# Butler
Here to serve you an easier way of coding.

##IconRequests
This library contains a singleton that will help process the request and supply callbacks.
The settings are created with a builder, and all the callbacks are handled through EventBus.
Possible events posted: AppLoadingEvent, AppLoadedEvent, AppSelectionEvent, RequestEvent
Each with their own helper methods. Subscribe to these events anywhere!
Please note that all eventbus posts are stickied as a user can always quit the view.
Handle them accordingly and look at the samplea app for further help.

To apply, add the following to your root build.gradle:

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

This library contains an icon request tool, with code taken from

[Icon-Request](https://github.com/afollestad/icon-request), which is a derivative of [PkRequestManager](https://github.com/Pkmmte/PkRequestManager)
