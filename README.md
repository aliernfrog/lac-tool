
# LAC Tool
Do most <a href="https://play.google.com/store/apps/details?id=com.MA.LAC">LAC</a> related stuff without having to mess with files 

# <a href="https://github.com/aliernfrog/lac-tool/releases">Download</a>
[![Download count](https://img.shields.io/github/downloads/aliernfrog/lac-tool/total.svg)]()
- <a href="https://github.com/aliernfrog/lac-tool/releases">from GitHub (recommended, faster updates)</a>
- <a href="https://f-droid.org/packages/com.aliernfrog.LacMapTool">from F-Droid</a>

# Features
- LAC map management (importing and more)
- LAC map role & option management
- LAC map merging
- LAC in game cellphone wallpaper management
- Import LAC in game cellphone wallpapers without internet connection
- LAC in game screenshot management
- These features work in any Android version above 4.3 (including Android 11 and above too!)

# Requirements
- Android 4.3 and above

# Installation
- If you're on Android 11 or above, please <a href="#for-android-11-and-above">check this</a> for the app to work properly
- Download and install the app

# For Android 11 and above
Due to some restrictions, LAC Tool fails to create some directories it needs on Android 11 and above.<br>
For the app to work properly, please create these directories manually:
- `Android/data/com.MA.LAC/files/editor`
- `Android/data/com.MA.LAC/files/wallpaper`
- `Android/data/com.MA.LAC/files/screenshots`

# GitHub version vs F-Droid version
Both versions have the same features.
GitHub version is recommended as it will receive updates faster than F-Droid version

# Building
- Clone the repository
- Open it in Android Studio
- Remove signingConfigs from `app/build.gradle` if needed
