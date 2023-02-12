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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.aliernfrog.lactool.*
import com.aliernfrog.lactool.R
import com.aliernfrog.lactool.state.SettingsState
import com.aliernfrog.lactool.state.UpdateState
import com.aliernfrog.lactool.ui.component.*
import com.aliernfrog.lactool.ui.dialog.PathOptionsDialog
import com.aliernfrog.lactool.ui.theme.supportsMaterialYou
import com.aliernfrog.lactool.util.staticutil.GeneralUtil
import com.aliernfrog.toptoast.state.TopToastState
import kotlinx.coroutines.launch


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(config: SharedPreferences, topToastState: TopToastState, updateState: UpdateState, settingsState: SettingsState) {
    AppScaffold(
        title = stringResource(R.string.settings),
        topAppBarState = settingsState.topAppBarState
    ) {
        Column(Modifier.fillMaxSize().verticalScroll(settingsState.scrollState)) {
            AppearanceOptions(settingsState)
            GeneralOptions(settingsState)
            AboutApp(updateState, settingsState)
            if (settingsState.experimentalSettingsShown) ExperimentalSettings(config, updateState, settingsState)
        }
    }
    if (settingsState.pathOptionsDialogShown) PathOptionsDialog(
        config = config,
        onDismissRequest = { saved ->
            settingsState.pathOptionsDialogShown = false
            if (saved) topToastState.showToast(
                text = R.string.settings_general_pathOptions_saved,
                icon = Icons.Rounded.Check
            )
        }
    )
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
private fun GeneralOptions(settingsState: SettingsState) {
    ColumnDivider(title = stringResource(R.string.settings_general)) {
        Switch(
            title = stringResource(R.string.settings_general_showMapThumbnailsInList),
            description = stringResource(R.string.settings_general_showMapThumbnailsInList_description),
            checked = settingsState.showMapThumbnailsInList.value
        ) {
            settingsState.setShowMapThumbnailsInList(it)
        }
        ButtonShapeless(
            title = stringResource(R.string.settings_general_pathOptions),
            description = stringResource(R.string.settings_general_pathOptions_description),
            expanded = false,
            arrowRotation = 90f
        ) {
            settingsState.pathOptionsDialogShown = true
        }
    }
}

@Composable
private fun AboutApp(updateState: UpdateState, settingsState: SettingsState) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val version = "v${GeneralUtil.getAppVersionName(context)} (${GeneralUtil.getAppVersionCode(context)})"
    ColumnDivider(title = stringResource(R.string.settings_about), bottomDivider = false) {
        ButtonWithComponent(
            title = stringResource(R.string.settings_about_version),
            description = version,
            component = {
                OutlinedButton(
                    onClick = { scope.launch { updateState.checkUpdates(manuallyTriggered = true) } }
                ) {
                    Text(stringResource(R.string.settings_about_checkUpdates))
                }
            }
        ) {
            settingsState.onAboutClick()
        }
        Switch(
            title = stringResource(R.string.settings_about_autoCheckUpdates),
            description = stringResource(R.string.settings_about_autoCheckUpdates_description),
            checked = settingsState.autoCheckUpdates.value
        ) {
            settingsState.setAutoCheckUpdates(it)
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
            SettingsConstant.socials.forEach {
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
private fun ExperimentalSettings(config: SharedPreferences, updateState: UpdateState, settingsState: SettingsState) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val configEditor = config.edit()
    ColumnDivider(title = stringResource(R.string.settings_experimental), bottomDivider = false, topDivider = true) {
        Text(stringResource(R.string.settings_experimental_description), color = MaterialTheme.colorScheme.error, modifier = Modifier.padding(horizontal = 16.dp))
        Switch(
            title = stringResource(R.string.settings_experimental_forceShowMaterialYouOption),
            checked = settingsState.forceShowMaterialYouOption.value,
            onCheckedChange = {
                settingsState.forceShowMaterialYouOption.value = it
            }
        )
        ButtonShapeless(title = stringResource(R.string.settings_experimental_checkUpdateIgnoreVersion)) {
            scope.launch { updateState.checkUpdates(manuallyTriggered = true, ignoreVersion = true) }
        }
        ButtonShapeless(title = stringResource(R.string.settings_experimental_resetAckedAlpha)) {
            configEditor.remove(ConfigKey.KEY_APP_LAST_ALPHA_ACK).apply()
        }
        SettingsConstant.experimentalPrefOptions.forEach { prefEdit ->
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
            SettingsConstant.experimentalPrefOptions.forEach {
                configEditor.remove(it.key)
            }
            configEditor.apply()
            restartApp(context)
        }
    }
}

private fun restartApp(context: Context) {
    val intent = Intent(context, MainActivity::class.java)
    (context as Activity).finish()
    context.startActivity(intent)
}