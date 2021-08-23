[![Download count](https://img.shields.io/github/downloads/aliernfrog/lac-tool/total.svg)]()
# LAC Tool
First <a href="https://play.google.com/store/apps/details?id=com.MA.LAC">LAC</a> related open source app, lets you do most of <a href="https://play.google.com/store/apps/details?id=com.MA.LAC">LAC</a> related stuff.

# <a href="https://github.com/aliernfrog/lac-tool/releases">Download</a>

# Features
- Managing LAC maps
- Managing roles of LAC maps
- Managing LAC wallpapers
- Importing LAC wallpapers without internet connection
- Viewing LAC screenshots
- <a href="#android-11-support">Android 11 support</a>

# Requirements
- At least Android 4.2.1 to run
- Android 5.0 or above recommended

# Android 11 support
The app can manage LAC data even in Android 11.

# Required folders
- `/Android/data/com.MA.LAC/files/editor`
- `/Android/data/com.MA.LAC/files/wallpaper`<br />
- You need to have these folders created if you're using Android 11.<br />
- If these folders aren't created and you're using Android 11, the app will request access to wrong directories and probably crash.

# Notes
- The app needs `READ_EXTERNAL_STORAGE` and `WRITE_EXTERNAL_STORAGE` to access LAC & LAC tool data.
- On Android 11, the app will request specific access to <a href="#required-folders">required folders</a> so it can access LAC data.
