package com.aliernfrog.lactool

import android.os.Build
import android.os.Environment
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Translate
import com.aliernfrog.lactool.data.PrefEditItem
import com.aliernfrog.lactool.data.Social
import com.aliernfrog.lactool.impl.CreditData
import com.aliernfrog.lactool.util.staticutil.GeneralUtil

const val TAG = "LACToolLogs"

const val experimentalSettingsRequiredClicks = 10
const val githubRepoURL = "https://github.com/aliernfrog/lac-tool"
const val crowdinURL = "https://crowdin.com/project/lac-tool"
const val documentsUIPackageName = "com.google.android.documentsui"

val externalStorageRoot = Environment.getExternalStorageDirectory().toString()+"/"
val supportsPerAppLanguagePreferences = Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU
val folderPickerSupportsInitialUri = Build.VERSION.SDK_INT >= Build.VERSION_CODES.O
val hasAndroidDataRestrictions = Build.VERSION.SDK_INT >= Build.VERSION_CODES.R

object ConfigKey {
    const val PREF_NAME = "APP_CONFIG"
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
            preference = { it.lacMapsDir },
            label = { R.string.settings_storage_folders_maps }
        ),
        PrefEditItem(
            preference = { it.lacWallpapersDir },
            label = { R.string.settings_storage_folders_wallpapers }
        ),
        PrefEditItem(
            preference = { it.lacScreenshotsDir },
            label = { R.string.settings_storage_folders_screenshots }
        ),
        PrefEditItem(
            preference = { it.exportedMapsDir },
            label = { R.string.settings_storage_folders_exportedMaps }
        )
    )

    val credits = listOf(
        CreditData(
            name = "Mohammad Alizadeh",
            githubUsername = "Alizadev",
            description = R.string.settings_about_credits_gameDev,
            link = "https://discord.gg/aQhGqHSc3W"
        ),
        CreditData(
            name = "alieRN",
            githubUsername = "aliernfrog",
            description = R.string.settings_about_credits_appDev
        ),
        CreditData(
            name = "infini0083",
            githubUsername = "infini0083",
            description = R.string.settings_about_credits_ui
        ),
        CreditData(
            name = R.string.settings_about_credits_crowdin,
            githubUsername = "crowdin",
            description = R.string.settings_about_credits_translations,
            link = crowdinURL
        ),
        CreditData(
            name = "Vendetta Manager",
            githubUsername = "vendetta-mod",
            description = R.string.settings_about_credits_inspiration,
            link = "https://github.com/vendetta-mod/VendettaManager"
        ),
        CreditData(
            name = "ReVanced Manager",
            githubUsername = "ReVanced",
            description = R.string.settings_about_credits_inspiration,
            link = "https://github.com/ReVanced/revanced-manager"
        )
    )

    val experimentalPrefOptions = listOf(
        PrefEditItem(
            preference = { it.showMapNameFieldGuide }
        ),
        PrefEditItem(
            preference = { it.showMediaViewGuide }
        ),
        PrefEditItem(
            preference = { it.shizukuNeverLoad }
        ),
        PrefEditItem(
            preference = { it.updatesURL }
        ),
        *folders.toTypedArray()
    )
}

val languages = BuildConfig.LANGUAGES.sorted().map { langCode ->
    GeneralUtil.getLanguageFromCode(langCode)!!
}