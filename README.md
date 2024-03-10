<div align="center">

  <img alt="LAC Tool icon" src="images/icon.png" width="120px"/>
  
  # LAC Tool
  Do most [LAC](https://play.google.com/store/apps/details?id=com.MA.LAC) related stuff without having to mess with files

  <br>

  [![Download (Android 6.0 or above)](https://img.shields.io/github/v/tag/aliernfrog/lac-tool?style=for-the-badge&label=Download%20(Android%206.0%2B)&labelColor=green&color=grey)](https://github.com/aliernfrog/lac-tool/releases/latest/download/lactool.apk)
  [![Download legacy (Android 4.3 or above)](https://img.shields.io/github/v/tag/aliernfrog/lac-tool-legacy?style=for-the-badge&label=Download%20legacy%20(Android%204.3%2B)&labelColor=blue&color=grey)](https://github.com/aliernfrog/lac-tool-legacy/releases/latest/download/lactool-legacy.apk)

  <br>

  ![Download count](https://img.shields.io/github/downloads/aliernfrog/lac-tool/total?style=for-the-badge&label=Download%20Count)
  ![Build status](https://img.shields.io/github/actions/workflow/status/aliernfrog/lac-tool/commit.yml?style=for-the-badge&label=Build%20status)

  ---
  
  <img alt="LAC Tool screenshot" src="images/maps.jpg" width="200px"/>
  
</div>

## 💡 Features
- LAC map management
- LAC map role & option management
- LAC map merging
- LAC in game cellphone wallpaper management
- Import LAC in game cellphone wallpapers without internet connection
- LAC in game screenshot management

## 🦝 Shizuku support
[Shizuku](https://play.google.com/store/apps/details?id=moe.shizuku.privileged.api) is an app which lets other apps elevate their permissions using wireless debugging (or root, if you have one).

Shizuku method in LAC Tool can be enabled or disabled anytime from settings.

Shizuku method will automatically be enabled if there is no other way for the app to access LAC data. The app will guide you to setup Shizuku if this mode is enabled.

## 🔧 Building
<details>
  <summary>Using GitHub Actions</summary>

  - Fork the repository
  - Add environment variables required for signing from **Repository settings > Secrets and variables > Actions > Repository secrets**:
    - `KEYSTORE_ALIAS`
    - `KEYSTORE_BASE64` this can be obtained using `openssl base64 keystore.jks`
    - `KEYSTORE_PASSWORD`
    - `KEY_PASSWORD`
  - Enable workflows
  - Trigger a build workflow and wait for it to build a release variant APK
</details>
<details>
  <summary>Locally</summary>

  - Clone the repository
  - Add a signing config (unless you only want to build debug variant or sign manually)
  - Obtain a GitHub PAT with `read:packages` scope
  - Put the PAT and your GitHub username in global/project `gradle.properties`:
    ```
    gpr.user=MyUserName
    gpr.key=MyPAT
    ```
    or supply `GITHUB_ACTOR` (username) and `GITHUB_TOKEN` (PAT) in environment variables
  - Build APK:
    - Release variant: `./gradlew assembleRelease`
    - Debug variant: `./gradlew assembleDebug`
</details>
