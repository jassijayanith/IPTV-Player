# My IPTV Player

A starter web IPTV player using the publicly available IPTV-org M3U playlist.

## Website
1. Add `index.html` to the repository root.
2. In repository Settings → Pages, select **Deploy from a branch**, branch `main`, folder `/ (root)`, then Save.
3. Visit `https://jassijayanith.github.io/IPTV-Player/` after deployment.

## Android APK
The workflow file at `.github/workflows/android-apk.yml` expects a complete Gradle Android wrapper project under `android-wrapper/`. The workflow alone does **not** make an APK: the wrapper project must also be added. Once added, go to Actions → Build Android APK → Run workflow, then download the artifact from the completed run.

## Important
This is a player, not a streaming service. It does not host video. Individual stream availability, geographic restrictions, playback compatibility and rights depend on each provider.
