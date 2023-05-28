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
import androidx.compose.material.icons.rounded.Done
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
import com.aliernfrog.lactool.ui.activity.MainActivity
import com.aliernfrog.lactool.ui.component.*
import com.aliernfrog.lactool.ui.dialog.PathOptionsDialog
import com.aliernfrog.lactool.ui.theme.supportsMaterialYou
import com.aliernfrog.lactool.ui.viewmodel.MainViewModel
import com.aliernfrog.lactool.ui.viewmodel.SettingsViewModel
import com.aliernfrog.toptoast.enum.TopToastType
import com.aliernfrog.toptoast.state.TopToastState
import kotlinx.coroutines.launch
import org.koin.androidx.compose.getViewModel


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    mainViewModel: MainViewModel = getViewModel(),
    settingsViewModel: SettingsViewModel = getViewModel()
) {
    val scope = rememberCoroutineScope()
    AppScaffold(
        title = stringResource(R.string.settings),
        topAppBarState = settingsViewModel.topAppBarState
    ) {
        Column(Modifier.fillMaxSize().verticalScroll(settingsViewModel.scrollState)) {
            AppearanceOptions(
                theme = settingsViewModel.prefs.theme,
                materialYou = settingsViewModel.prefs.materialYou,
                forceShowMaterialYouOption = settingsViewModel.forceShowMaterialYouOption,
                themeOptionsExpanded = settingsViewModel.themeOptionsExpanded,
                onThemeOptionsExpandedStateChange = { settingsViewModel.themeOptionsExpanded = it },
                onThemeChange = { settingsViewModel.prefs.theme = it },
                onMaterialYouChange = { settingsViewModel.prefs.materialYou = it }
            )
            GeneralOptions(
                showMapThumbnailsInList = settingsViewModel.prefs.showMapThumbnailsInList,
                onShowMapThumbnailsInListChange = { settingsViewModel.prefs.showMapThumbnailsInList = it },
                onPathOptionsDialogShowRequest = { settingsViewModel.pathOptionsDialogShown = true }
            )
            AboutApp(
                appVersionName = mainViewModel.applicationVersionName,
                appVersionCode = mainViewModel.applicationVersionCode,
                autoCheckUpdates = settingsViewModel.prefs.autoCheckUpdates,
                linksExpanded = settingsViewModel.linksExpanded,
                onCheckUpdatesRequest = {
                    scope.launch {
                        mainViewModel.checkForUpdates(manuallyTriggered = true)
                    }
                },
                onAboutClick = { settingsViewModel.onAboutClick() },
                onAutoCheckUpdatesChange = { settingsViewModel.prefs.autoCheckUpdates = it },
                onLinksExpandedStateChange = { settingsViewModel.linksExpanded = it }
            )
            if (settingsViewModel.experimentalSettingsShown) ExperimentalSettings(
                config = settingsViewModel.prefs.prefs,
                topToastState = settingsViewModel.topToastState,
                forceShowMaterialYouOption = settingsViewModel.forceShowMaterialYouOption,
                onForceShowMaterialYouOptionChange = { settingsViewModel.forceShowMaterialYouOption = it },
                onCheckUpdatesRequest = { manuallyTriggered, ignoreVersion ->
                    scope.launch {
                        mainViewModel.checkForUpdates(
                            manuallyTriggered = manuallyTriggered,
                            ignoreVersion = ignoreVersion
                        )
                    }
                }
            )
        }
    }
    if (settingsViewModel.pathOptionsDialogShown) PathOptionsDialog(
        topToastState = settingsViewModel.topToastState,
        config = settingsViewModel.prefs.prefs,
        onDismissRequest = {
            settingsViewModel.pathOptionsDialogShown = false
        }
    )
}

@Composable
private fun AppearanceOptions(
    theme: Int,
    materialYou: Boolean,
    forceShowMaterialYouOption: Boolean,
    themeOptionsExpanded: Boolean,
    onThemeOptionsExpandedStateChange: (Boolean) -> Unit,
    onThemeChange: (Int) -> Unit,
    onMaterialYouChange: (Boolean) -> Unit
) {
    val themeOptions = listOf(
        stringResource(R.string.settings_appearance_theme_system),
        stringResource(R.string.settings_appearance_theme_light),
        stringResource(R.string.settings_appearance_theme_dark)
    )
    ColumnDivider(title = stringResource(R.string.settings_appearance)) {
        ButtonShapeless(
            title = stringResource(R.string.settings_appearance_theme),
            description = stringResource(R.string.settings_appearance_theme_description),
            expanded = themeOptionsExpanded
        ) {
            onThemeOptionsExpandedStateChange(!themeOptionsExpanded)
        }
        AnimatedVisibility(
            visible = themeOptionsExpanded,
            enter = expandVertically() + fadeIn(),
            exit = shrinkVertically() + fadeOut()
        ) {
            ColumnRounded(Modifier.padding(horizontal = 8.dp)) {
                RadioButtons(
                    options = themeOptions,
                    initialIndex = theme,
                    optionsRounded = true
                ) {
                    onThemeChange(it)
                }
            }
        }
        if (forceShowMaterialYouOption || supportsMaterialYou) Switch(
            title = stringResource(R.string.settings_appearance_materialYou),
            description = stringResource(R.string.settings_appearance_materialYou_description),
            checked = materialYou
        ) {
            onMaterialYouChange(it)
        }
    }
}

@Composable
private fun GeneralOptions(
    showMapThumbnailsInList: Boolean,
    onShowMapThumbnailsInListChange: (Boolean) -> Unit,
    onPathOptionsDialogShowRequest: () -> Unit
) {
    ColumnDivider(title = stringResource(R.string.settings_general)) {
        Switch(
            title = stringResource(R.string.settings_general_showMapThumbnailsInList),
            description = stringResource(R.string.settings_general_showMapThumbnailsInList_description),
            checked = showMapThumbnailsInList
        ) {
            onShowMapThumbnailsInListChange(it)
        }
        ButtonShapeless(
            title = stringResource(R.string.settings_general_pathOptions),
            description = stringResource(R.string.settings_general_pathOptions_description),
            expanded = false,
            arrowRotation = 90f
        ) {
            onPathOptionsDialogShowRequest()
        }
    }
}

@Composable
private fun AboutApp(
    appVersionName: String,
    appVersionCode: Int,
    autoCheckUpdates: Boolean,
    linksExpanded: Boolean,
    onCheckUpdatesRequest: () -> Unit,
    onAboutClick: () -> Unit,
    onAutoCheckUpdatesChange: (Boolean) -> Unit,
    onLinksExpandedStateChange: (Boolean) -> Unit
) {
    val version = "$appVersionName ($appVersionCode)"
    ColumnDivider(title = stringResource(R.string.settings_about), bottomDivider = false) {
        ButtonWithComponent(
            title = stringResource(R.string.settings_about_version),
            description = version,
            component = {
                OutlinedButton(
                    onClick = { onCheckUpdatesRequest() }
                ) {
                    Text(stringResource(R.string.settings_about_checkUpdates))
                }
            }
        ) {
            onAboutClick()
        }
        Switch(
            title = stringResource(R.string.settings_about_autoCheckUpdates),
            description = stringResource(R.string.settings_about_autoCheckUpdates_description),
            checked = autoCheckUpdates
        ) {
            onAutoCheckUpdatesChange(it)
        }
        Links(
            linksExpanded = linksExpanded,
            onLinksExpandedStateChange = onLinksExpandedStateChange
        )
    }
}

@Composable
private fun Links(
    linksExpanded: Boolean,
    onLinksExpandedStateChange: (Boolean) -> Unit
) {
    val uriHandler = LocalUriHandler.current
    ButtonShapeless(
        title = stringResource(R.string.settings_about_links),
        description = stringResource(R.string.settings_about_links_description),
        expanded = linksExpanded
    ) {
        onLinksExpandedStateChange(!linksExpanded)
    }
    AnimatedVisibility(
        visible = linksExpanded,
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
private fun ExperimentalSettings(
    config: SharedPreferences,
    topToastState: TopToastState,
    forceShowMaterialYouOption: Boolean,
    onForceShowMaterialYouOptionChange: (Boolean) -> Unit,
    onCheckUpdatesRequest: (manuallyTriggered: Boolean, ignoreVersion: Boolean) -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val configEditor = config.edit()
    ColumnDivider(title = stringResource(R.string.settings_experimental), bottomDivider = false, topDivider = true) {
        Text(stringResource(R.string.settings_experimental_description), color = MaterialTheme.colorScheme.error, modifier = Modifier.padding(horizontal = 16.dp))
        Switch(
            title = stringResource(R.string.settings_experimental_forceShowMaterialYouOption),
            checked = forceShowMaterialYouOption,
            onCheckedChange = {
                onForceShowMaterialYouOptionChange(it)
            }
        )
        ButtonShapeless(title = stringResource(R.string.settings_experimental_checkUpdateIgnoreVersion)) {
            scope.launch { onCheckUpdatesRequest(false, true) }
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
            topToastState.showToast(
                text = R.string.settings_experimental_resetPrefsDone,
                icon = Icons.Rounded.Done,
                type = TopToastType.ANDROID
            )
            restartApp(context)
        }
    }
}

private fun restartApp(context: Context) {
    val intent = Intent(context, MainActivity::class.java)
    (context as Activity).finish()
    context.startActivity(intent)
}