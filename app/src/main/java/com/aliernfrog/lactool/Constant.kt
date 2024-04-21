package com.aliernfrog.lactool

import android.os.Build
import android.os.Environment
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Translate
import com.aliernfrog.lactool.data.CreditsData
import com.aliernfrog.lactool.data.PrefEditItem
import com.aliernfrog.lactool.data.Social
import com.aliernfrog.lactool.util.staticutil.GeneralUtil

const val TAG = "LACToolLogs"

const val experimentalSettingsRequiredClicks = 10
const val githubRepoURL = "https://github.com/aliernfrog/lac-tool"
const val crowdinURL = "https://crowdin.com/project/lac-tool"
const val documentsUIPackageName = "com.google.android.documentsui"

val externalStorageRoot = Environment.getExternalStorageDirectory().toString()+"/"
val supportsPerAppLanguagePreferences = Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU
val imeSupportsSyncAppContent = Build.VERSION.SDK_INT >= Build.VERSION_CODES.R
val folderPickerSupportsInitialUri = Build.VERSION.SDK_INT >= Build.VERSION_CODES.O
val hasAndroidDataRestrictions = Build.VERSION.SDK_INT >= Build.VERSION_CODES.R

object ConfigKey {
    const val PREF_NAME = "APP_CONFIG"
    const val KEY_APP_LANGUAGE = "appLanguage"
    const val KEY_APP_THEME = "appTheme"
    const val KEY_APP_MATERIAL_YOU = "materialYou"
    const val KEY_APP_AUTO_UPDATES = "autoUpdates"
    const val KEY_APP_UPDATES_URL = "updatesUrl"
    const val KEY_SHOW_MAP_THUMBNAILS_LIST = "showMapThumbnailsList"
    const val KEY_SHOW_CHOSEN_MAP_THUMBNAIL = "chosenMapThumbnail"
    const val KEY_MAPS_DIR = "mapsDir"
    const val KEY_WALLPAPERS_DIR = "wallpapersDir"
    const val KEY_SCREENSHOTS_DIR = "screenshotsDir"
    const val KEY_EXPORTED_MAPS_DIR = "mapsExportDir"
    const val DEFAULT_UPDATES_URL = "https://aliernfrog.github.io/lactool/latest.json"
    val RECOMMENDED_MAPS_DIR = "${externalStorageRoot}Android/data/com.MA.LAC/files/editor"
    val RECOMMENDED_WALLPAPERS_DIR = "${externalStorageRoot}Android/data/com.MA.LAC/files/wallpaper"
    val RECOMMENDED_SCREENSHOTS_DIR = "${externalStorageRoot}Android/data/com.MA.LAC/files/screenshots"
    val RECOMMENDED_EXPORTED_MAPS_DIR = "${Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS)}/LACTool/exported"
}

object SettingsConstant {
    val socials = listOf(
        Social(
            label = "LAC",
            icon = R.drawable.discord,
            url = "https://discord.gg/aQhGqHSc3W"
        ),
        Social(
            label = "LAC Tool",
            icon = R.drawable.github,
            url = githubRepoURL
        ),
        Social(
            label = "Crowdin",
            icon = Icons.Default.Translate,
            url = crowdinURL
        )
    )

    val folders = listOf(
        PrefEditItem(
            labelResourceId = R.string.settings_storage_folders_maps,
            getValue = { it.lacMapsDir },
            setValue = { newValue, prefs ->
                prefs.lacMapsDir = newValue
            },
            default = ConfigKey.RECOMMENDED_MAPS_DIR
        ),
        PrefEditItem(
            labelResourceId = R.string.settings_storage_folders_wallpapers,
            getValue = { it.lacWallpapersDir },
            setValue = { newValue, prefs ->
                prefs.lacWallpapersDir = newValue
            },
            default = ConfigKey.RECOMMENDED_WALLPAPERS_DIR
        ),
        PrefEditItem(
            labelResourceId = R.string.settings_storage_folders_screenshots,
            getValue = { it.lacScreenshotsDir },
            setValue = { newValue, prefs ->
                prefs.lacScreenshotsDir = newValue
            },
            default = ConfigKey.RECOMMENDED_SCREENSHOTS_DIR
        ),
        PrefEditItem(
            labelResourceId = R.string.settings_storage_folders_exportedMaps,
            getValue = { it.exportedMapsDir },
            setValue = { newValue, prefs ->
              prefs.exportedMapsDir = newValue
            },
            default = ConfigKey.RECOMMENDED_EXPORTED_MAPS_DIR,
        )
    )

    val credits = listOf(
        CreditsData(
            name = "Mohammad Alizadeh",
            description = R.string.settings_about_credits_gameDev,
            url = "https://discord.gg/aQhGqHSc3W"
        ),
        CreditsData(
            name = "alieRN",
            description = R.string.settings_about_credits_appDev,
            url = "https://github.com/aliernfrog"
        ),
        CreditsData(
            name = "infini0083",
            description = R.string.settings_about_credits_ui,
            url = "https://github.com/infini0083"
        ),
        CreditsData(
            name = R.string.settings_about_credits_crowdin,
            description = R.string.settings_about_credits_translations,
            url = crowdinURL
        ),
        CreditsData(
            name = "Vendetta Manager",
            description = R.string.settings_about_credits_inspiration,
            url = "https://github.com/vendetta-mod/VendettaManager"
        ),
        CreditsData(
            name = "ReVanced Manager",
            description = R.string.settings_about_credits_inspiration,
            url = "https://github.com/ReVanced/revanced-manager"
        )
    )

    val experimentalPrefOptions = listOf(
        PrefEditItem(
            labelResourceId = R.string.settings_experimental_updatesURL,
            getValue = { it.updatesURL },
            setValue = { newValue, prefs ->
                prefs.updatesURL = newValue
            },
            default = ConfigKey.DEFAULT_UPDATES_URL
        ),
        *folders.toTypedArray()
    )
}

val languages = BuildConfig.LANGUAGES.sorted().map { langCode ->
    GeneralUtil.getLanguageFromCode(langCode)!!
}