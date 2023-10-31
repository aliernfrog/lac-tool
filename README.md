> [!WARNING]
> App may not work on devices using **October 2023 or newer security patches**, as those security patches prevent apps from requesting access to files inside `Android/data` folder.
> 
> If you are affected by this, please use the following methods until a solution is found:
> - Connect your device to a PC with USB or use wireless debugging, you'll be able to manually manage files inside `Android/data/com.MA.LAC/files`
>   - `editor` folder contains maps
>   - `wallpaper` folder contains wallpapers
>   - `screenshots` folder contains screenshots
> - Copy map .txt link and paste it on map name field when creating the map in LAC, this will download the map
> - Copy wallpaper link and paste it on in-game cellphone settings to download a wallpaper

<div align="center">

  <img alt="LAC Tool icon" src="images/icon.png" width="120px"/>
  
  # LAC Tool
  Do most [LAC](https://play.google.com/store/apps/details?id=com.MA.LAC) related stuff without having to mess with files

  <br>

  [![Download stable (Android 4.3 or above)](https://img.shields.io/badge/v2.0-green?style=for-the-badge&label=Download%20(Android%204.3%2B)&labelColor=green&color=grey)](https://github.com/aliernfrog/lac-tool/releases/tag/20)
  [![Download alpha (Android 6.0 or above)](https://img.shields.io/github/v/tag/aliernfrog/lac-tool?style=for-the-badge&label=Alpha%20(Android%206.0%2B)&labelColor=blue&color=grey)](https://github.com/aliernfrog/lac-tool/releases)

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

## 🔧 Building
- Clone the repository
- Do your changes
- `./gradlew assembleRelease`
