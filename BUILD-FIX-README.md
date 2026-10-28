# My IPTV Player — Android Build Fix

This project fixes the Gradle plugin configuration in the uploaded project.

## What was wrong
The previous `android-wrapper/build.gradle` was configured as an Android app module but had no Android Gradle Plugin version. Gradle failed with:
`Plugin [id: 'com.android.application'] was not found ... plugin dependency must include a version number`.

## Fixes included
- Root `android-wrapper/build.gradle` now declares Android Gradle Plugin 8.7.3 and Kotlin plugin 2.0.21 with `apply false`.
- App module `android-wrapper/app/build.gradle` now contains the app configuration and Media3 ExoPlayer/HLS dependencies.
- Removed the duplicate Kotlin `MainActivity.kt`; the project uses the WebView bridge in `MainActivity.java` plus native `PlayerActivity.java`.
- Kept the category playlist HTML in both website and Android asset locations.

## Apply
Upload/replace the contents of this ZIP in the GitHub repository, preserving the paths. Ensure the old `android-wrapper/app/src/main/java/com/jassijayanith/iptvplayer/MainActivity.kt` is deleted if you are copying files individually. Then commit and rerun Actions.
