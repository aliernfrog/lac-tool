[![Download count](https://img.shields.io/github/downloads/aliernfrog/lac-tool/total.svg)]()
# LAC Tool
Do most <a href="https://play.google.com/store/apps/details?id=com.MA.LAC">LAC</a> related stuff without having to mess with files 

# <a href="https://github.com/aliernfrog/lac-tool/releases">Download</a>

# Features
- LAC map management (importing and more)
- LAC map role & option management
- LAC in game cellphone wallpaper management
- Import LAC in game cellphone wallpapers without internet connection
- LAC in game screenshot management
- These features work in any Android version above 4.3, including Android 11

# Requirements
- Any Android version above 4.3

# Installation
- If you're on Android 11 and above, please <a href="#for-android-11-and-above">check this</a> for the app to work properly
- Download .apk <a href="https://github.com/aliernfrog/lac-tool/releases">from here</a>
- Open .apk file & install the app

# For Android 11 and above
Due to some restrictions, LAC Tool fails to create some directories it needs.<br>
For the app to work properly, you can create these directories manually:
- `Android/data/com.MA.LAC/files/editor`
- `Android/data/com.MA.LAC/files/wallpaper`
- `Android/data/com.MA.LAC/files/screenshots`

# Building
- Clone a tag, or the master branch but it might be unstable
- Open it in Android Studio
- Remove signingConfigs from `app/build.gradle` if needed
