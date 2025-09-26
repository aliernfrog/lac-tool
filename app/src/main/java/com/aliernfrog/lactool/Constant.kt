package com.aliernfrog.lactool

import android.os.Build
import android.os.Environment
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Translate
import androidx.compose.ui.graphics.Color
import com.aliernfrog.lactool.data.PrefEditItem
import com.aliernfrog.lactool.data.Social
import com.aliernfrog.lactool.impl.CreditData
import com.aliernfrog.lactool.util.staticutil.GeneralUtil

const val TAG = "LACToolLogs"

const val experimentalSettingsRequiredClicks = 10
const val githubRepoURL = "https://github.com/aliernfrog/lac-tool"
const val crowdinURL = "https://crowdin.com/project/lac-tool"

val externalStorageRoot = Environment.getExternalStorageDirectory().toString()+"/"
val supportsPerAppLanguagePreferences = Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU
val folderPickerSupportsInitialUri = Build.VERSION.SDK_INT >= Build.VERSION_CODES.O
val hasAndroidDataRestrictions = Build.VERSION.SDK_INT >= Build.VERSION_CODES.R

object SettingsConstant {
    val socials = listOf(
        Social(
            label = "LAC",
            icon = R.drawable.discord,
            iconContainerColor = Color(0xFF5865F2),
            url = "https://discord.gg/aQhGqHSc3W"
        ),
        Social(
            label = "LAC Tool",
            icon = R.drawable.github,
            iconContainerColor = Color(0xFF104C35),
            url = githubRepoURL
        ),
        Social(
            label = "Crowdin",
            icon = Icons.Default.Translate,
            iconContainerColor = Color(0xFF263238),
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
}

val languages = BuildConfig.LANGUAGES.sorted().map { langCode ->
    GeneralUtil.getLanguageFromCode(langCode)!!
}