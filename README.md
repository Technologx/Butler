# Butler
Here to serve you an easier way of coding.

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
