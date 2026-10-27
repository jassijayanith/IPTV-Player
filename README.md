# My IPTV Player

Includes:
- `index.html`: GitHub Pages website
- `android-wrapper/`: Android WebView wrapper that packages the same player UI into an installable APK
- `.github/workflows/android-apk.yml`: GitHub Actions workflow to build a debug APK

## Publish the website
In repository Settings → Pages, choose Deploy from a branch, `main`, and `/ (root)`.

## Build the APK
Push/commit the files to the repository. Open Actions → Build Android APK. A push to Android-related paths should trigger the workflow. When the run completes, open the run and download `my-iptv-player-debug-apk` under Artifacts.

This is a player, not a streaming service. It loads the public IPTV-org playlist and does not host video. Individual streams may fail, be geographically restricted, or be incompatible with WebView. Only watch streams you are authorized to access.
