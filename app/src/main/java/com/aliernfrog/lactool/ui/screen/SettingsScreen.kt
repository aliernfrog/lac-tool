package com.aliernfrog.lactool.ui.screen

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import androidx.compose.animation.*
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.aliernfrog.lactool.ConfigKey
import com.aliernfrog.lactool.Link
import com.aliernfrog.lactool.MainActivity
import com.aliernfrog.lactool.R
import com.aliernfrog.lactool.data.PrefEditItem
import com.aliernfrog.lactool.state.SettingsState
import com.aliernfrog.lactool.ui.component.*
import com.aliernfrog.lactool.ui.theme.supportsMaterialYou
import com.aliernfrog.lactool.util.staticutil.GeneralUtil
import com.aliernfrog.toptoast.state.TopToastState

private const val experimentalRequiredClicks = 10

@Composable
fun SettingsScreen(config: SharedPreferences, topToastState: TopToastState, settingsState: SettingsState) {
    Column(Modifier.fillMaxSize().verticalScroll(settingsState.scrollState)) {
        AppearanceOptions(settingsState)
        MapsOptions(settingsState)
        AboutApp(topToastState, settingsState)
        if (settingsState.aboutClickCount.value >= experimentalRequiredClicks) ExperimentalSettings(config, settingsState)
    }
}

@Composable
private fun AppearanceOptions(settingsState: SettingsState) {
    val themeOptions = listOf(
        stringResource(R.string.settings_appearance_theme_system),
        stringResource(R.string.settings_appearance_theme_light),
        stringResource(R.string.settings_appearance_theme_dark)
    )
    ColumnDivider(title = stringResource(R.string.settings_appearance)) {
        ButtonShapeless(
            title = stringResource(R.string.settings_appearance_theme),
            description = stringResource(R.string.settings_appearance_theme_description),
            expanded = settingsState.themeOptionsExpanded.value
        ) {
            settingsState.themeOptionsExpanded.value = !settingsState.themeOptionsExpanded.value
        }
        AnimatedVisibility(
            visible = settingsState.themeOptionsExpanded.value,
            enter = expandVertically() + fadeIn(),
            exit = shrinkVertically() + fadeOut()
        ) {
            ColumnRounded(Modifier.padding(horizontal = 8.dp)) {
                RadioButtons(
                    options = themeOptions,
                    initialIndex = settingsState.theme.value,
                    optionsRounded = true
                ) {
                    settingsState.setTheme(it)
                }
            }
        }
        if (settingsState.forceShowMaterialYouOption.value || supportsMaterialYou) Switch(
            title = stringResource(R.string.settings_appearance_materialYou),
            description = stringResource(R.string.settings_appearance_materialYou_description),
            checked = settingsState.materialYou.value
        ) {
            settingsState.setMaterialYou(it)
        }
    }
}

@Composable
private fun MapsOptions(settingsState: SettingsState) {
    ColumnDivider(title = stringResource(R.string.settings_maps)) {
        Switch(
            title = stringResource(R.string.settings_maps_showMapThumbnailsInList),
            description = stringResource(R.string.settings_maps_showMapThumbnailsInList_description),
            checked = settingsState.showMapThumbnailsInList.value
        ) {
            settingsState.setShowMapThumbnailsInList(it)
        }
    }
}

@Composable
private fun AboutApp(topToastState: TopToastState, settingsState: SettingsState) {
    val context = LocalContext.current
    val version = "v${GeneralUtil.getAppVersionName(context)} (${GeneralUtil.getAppVersionCode(context)})"
    ColumnDivider(title = stringResource(R.string.settings_about), bottomDivider = false) {
        ButtonShapeless(title = stringResource(R.string.settings_about_version), description = version) {
            settingsState.aboutClickCount.value++
            if (settingsState.aboutClickCount.value == experimentalRequiredClicks) topToastState.showToast(R.string.setings_experimental_enabled)
        }
        Links(settingsState)
    }
}

@Composable
private fun Links(settingsState: SettingsState) {
    val uriHandler = LocalUriHandler.current
    ButtonShapeless(title = stringResource(R.string.settings_about_links), description = stringResource(R.string.settings_about_links_description), expanded = settingsState.linksExpanded.value) {
        settingsState.linksExpanded.value = !settingsState.linksExpanded.value
    }
    AnimatedVisibility(
        visible = settingsState.linksExpanded.value,
        enter = expandVertically() + fadeIn(),
        exit = shrinkVertically() + fadeOut()
    ) {
        ColumnRounded(Modifier.padding(horizontal = 8.dp)) {
            Link.socials.forEach {
                val icon = when(it.url.split("/")[2]) {
                    "discord.gg" -> painterResource(id = R.drawable.discord)
                    "github.com" -> painterResource(id = R.drawable.github)
                    else -> null
                }
                ButtonShapeless(title = it.name, painter = icon, rounded = true, contentColor = MaterialTheme.colorScheme.onSurfaceVariant) { uriHandler.openUri(it.url) }
            }
        }
    }
}

@Composable
private fun ExperimentalSettings(config: SharedPreferences, settingsState: SettingsState) {
    val context = LocalContext.current
    val configEditor = config.edit()
    val prefEdits = listOf(
        PrefEditItem(ConfigKey.KEY_MAPS_DIR, ConfigKey.DEFAULT_MAPS_DIR),
        PrefEditItem(ConfigKey.KEY_MAPS_EXPORT_DIR, ConfigKey.DEFAULT_MAPS_EXPORT_DIR),
        PrefEditItem(ConfigKey.KEY_WALLPAPERS_DIR, ConfigKey.DEFAULT_WALLPAPERS_DIR),
        PrefEditItem(ConfigKey.KEY_SCREENSHOTS_DIR, ConfigKey.DEFAULT_SCREENSHOTS_DIR)
    )
    ColumnDivider(title = stringResource(R.string.settings_experimental), bottomDivider = false, topDivider = true) {
        Text(stringResource(R.string.settings_experimental_description), color = MaterialTheme.colorScheme.error, modifier = Modifier.padding(horizontal = 16.dp))
        Switch(
            title = stringResource(R.string.settings_experimental_forceShowMaterialYouOption),
            checked = settingsState.forceShowMaterialYouOption.value,
            onCheckedChange = {
                settingsState.forceShowMaterialYouOption.value = it
            }
        )
        prefEdits.forEach { prefEdit ->
            val value = remember { mutableStateOf(config.getString(prefEdit.key, prefEdit.default)!!) }
            TextField(
                label = { Text(text = "Prefs: ${prefEdit.key}") },
                value = value.value,
                modifier = Modifier.padding(horizontal = 8.dp),
                contentColor = MaterialTheme.colorScheme.onSurface,
                containerColor = MaterialTheme.colorScheme.surface,
                rounded = false,
                onValueChange = {
                    value.value = it
                    configEditor.putString(prefEdit.key, it)
                    configEditor.apply()
                }
            )
        }
        ButtonShapeless(title = stringResource(R.string.settings_experimental_resetPrefs), contentColor = MaterialTheme.colorScheme.error) {
            prefEdits.forEach {
                configEditor.remove(it.key)
                configEditor.apply()
            }
            restartApp(context)
        }
    }
}

private fun restartApp(context: Context) {
    val intent = Intent(context, MainActivity::class.java)
    (context as Activity).finish()
    context.startActivity(intent)
}