<div align="center">

  <img alt="LAC Tool icon" src="images/icon.png" width="120px"/>
  
  # LAC Tool
  Easily manage [LAC](https://play.google.com/store/apps/details?id=com.MA.LAC) maps, wallpapers and screenshots

  <br>

  [![Download (Android 6.0 or above)](https://img.shields.io/github/v/tag/aliernfrog/lac-tool?style=for-the-badge&label=Download%20(Android%205.0%2B)&labelColor=green&color=grey)](https://github.com/aliernfrog/lac-tool/releases/latest/download/lactool.apk)
  [![Download legacy (Android 4.3 or above)](https://img.shields.io/github/v/tag/aliernfrog/lac-tool-legacy?style=for-the-badge&label=Download%20legacy%20(Android%204.3%2B)&labelColor=blue&color=grey)](https://github.com/aliernfrog/lac-tool-legacy/releases/latest/download/lactool-legacy.apk)

  <br>

  ![Download count](https://img.shields.io/github/downloads/aliernfrog/lac-tool/total?style=for-the-badge&label=Download%20Count)
  ![Build status](https://img.shields.io/github/actions/workflow/status/aliernfrog/lac-tool/commit.yml?style=for-the-badge&label=Build%20status)

  ---
  
  <img alt="LAC Tool screenshot" src="images/maps.jpg" width="200px"/>
  
</div>

## 💡 Features
- 📥 Import, export and share maps
- 🏷️ Edit roles, options and filter objects of maps
- 🔀 Merge multiple maps into one
- 📱 Set custom wallpaper for the in-game cellphone
- 📷 View screenshots taken using the in-game cellphone
- 🎨 Material You Expressive design
- 🌐 Multiple language support (translate on [Crowdin](https://crowdin.com/project/lac-tool))

## 🌍 Translations
You can help translate LAC Tool on [Crowdin](https://crowdin.com/project/lac-tool).

[![Crowdin](https://badges.crowdin.net/lac-tool/localized.svg)](https://crowdin.com/project/lac-tool)

## 🦝 Shizuku support
[Shizuku](https://play.google.com/store/apps/details?id=moe.shizuku.privileged.api) is an app which lets other apps elevate their permissions using wireless debugging or root access.

Shizuku method in LAC Tool can be enabled or disabled anytime from settings.

Shizuku method will automatically be enabled if there is no other way for the app to access LAC data. The app will guide you to setup Shizuku if this mode is enabled.

## 🩹 Project structure
LAC Tool uses mostly the same codebase as ["PF Tool"](https://github.com/aliernfrog/pf-tool). To avoid updating the codebase for each app, shared code was moved into 2 libraries in `pf-tool` repository: [pftool-shared-base](https://github.com/aliernfrog/pf-tool/tree/main/shared) and [pftool-shared-extra](https://github.com/aliernfrog/pf-tool/tree/main/pftool-shared).

## ⚖️ License
Since commit [`0cd1915`](https://github.com/aliernfrog/lac-tool/commit/0cd1915cf1830292ffef0e0617a068455e208c78), LAC Tool is licensed under the GPLv3 license.<br />
You must keep the source code public if you are distributing your own version of LAC Tool. See [LICENSE.md](LICENSE.md) file for more details.

## 🔧 Building
<details>
  <summary>Using GitHub Actions</summary>

  - Fork the repository
  - Add environment variables required for signing from **Repository settings > Secrets and variables > Actions > Repository secrets**:
    - `KEYSTORE_ALIAS`
    - `KEYSTORE_BASE64` this can be obtained using `openssl base64 -in keystore.jks`
    - `KEYSTORE_PASSWORD`
    - `KEY_PASSWORD`
  - Enable workflows
  - Trigger a build workflow and wait for it to build a release variant APK
</details>
<details>
  <summary>Locally</summary>

  - Clone the repository
  - Add a signing config (unless you only want to build debug variant or sign manually)
  - Build APK:
    - Release variant: `./gradlew assembleRelease`
    - Debug variant: `./gradlew assembleDebug`
</details>
<details>
  <summary>Properties</summary>
  
  Following can be set in `local.properties`:
  
  - `laclibPath` -> Path to a local [LACLib](https://github.com/aliernfrog/laclib) jar (optional)
  - `pftoolSharedBaseLibPath` -> Path to a local [pftool-shared-base](https://github.com/aliernfrog/pf-tool/tree/main/shared) aar (optional)
  - `pftoolSharedExtraLibPath` -> Path to a local [pftool-shared-extra](https://github.com/aliernfrog/pf-tool/tree/main/pftool-shared) aar (optional)
</details>
