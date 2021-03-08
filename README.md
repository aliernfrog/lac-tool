# LAC Tool
An open source app, lets you manage your <a href="https://play.google.com/store/apps/details?id=com.MA.LAC">LAC</a> maps and wallpapers.

# Requirements
- At least Android 4.2 to run
- Android 4.4 or above recommended

# Android 11 support
The app supports Android 11, and can manage LAC datas. But, you should create <a href="https://github.com/aliernfrog/lac-tool/blob/dev/README.md#required-folders">required folders</a> yourself, since the app can't do that.

# Required folders
- `/Android/data/com.MA.LAC/files/editor`
- `/Android/data/com.MA.LAC/files/editor`<br />
App should run without any problem if these folders are created.
No need to create these folders if you're not using Android 11 or these folders are already created.

# Notes
- The app needs `READ_EXTERNAL_STORAGE` and `WRITE_EXTERNAL_STORAGE` to access LAC & LAC tool data.
- On Android 11, the app will request specific access to <a href="https://github.com/aliernfrog/lac-tool/blob/dev/README.md#required-folders">required folders</a> so it can access LAC data.
