# My IPTV Player — Standalone Android APK

The Android app bundles `index.html` inside the APK and opens it from `file:///android_asset/index.html`. It does not open the GitHub Pages URL. Internet is still needed to load the public IPTV-org playlist and live streams.

## Update
Upload the root `index.html` and the complete `android-wrapper/` folder, plus `.github/workflows/android-apk.yml`, keeping paths intact. Commit to `main`, then open Actions → Build Android APK. Download artifact `my-iptv-standalone-apk` from the successful run.

This is a standalone app shell, but streaming still needs an internet connection. Individual third-party streams may not play in Android WebView.
