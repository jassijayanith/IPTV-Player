# My IPTV Player — Native Android

Native Kotlin Android UI and AndroidX Media3 ExoPlayer playback. No WebView is used. The app downloads the public IPTV-org M3U playlist directly. Live streams need internet and may be unavailable or incompatible; this app does not host streams.

Build with GitHub Actions: commit the `android-wrapper/` folder and workflow, open Actions → Build Native Android IPTV APK, then download `my-iptv-native-apk` from the completed run's Artifacts.


## Android build notes

The Android app uses the same category-based interface as the website (`index.html`).
The APK opens the bundled copy at `android-wrapper/app/src/main/assets/index.html` and
hands channel playback to the native Media3 player. The Android Gradle Plugin version
is declared in `android-wrapper/build.gradle`; the app module is configured in
`android-wrapper/app/build.gradle`. Only the Java `MainActivity` is used to avoid
duplicate activity class definitions.
