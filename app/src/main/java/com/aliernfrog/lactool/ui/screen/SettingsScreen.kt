package com.aliernfrog.lactool.ui.screen

import androidx.compose.animation.*
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.ExperimentalMaterialApi
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
import com.aliernfrog.lactool.ui.component.*
import com.aliernfrog.lactool.ui.component.form.ButtonRow
import com.aliernfrog.lactool.ui.component.form.ExpandableRow
import com.aliernfrog.lactool.ui.component.form.FormSection
import com.aliernfrog.lactool.ui.component.form.SwitchRow
import com.aliernfrog.lactool.ui.dialog.PathOptionsDialog
import com.aliernfrog.lactool.ui.viewmodel.MainViewModel
import com.aliernfrog.lactool.ui.viewmodel.SettingsViewModel
import com.aliernfrog.lactool.util.manager.PreferenceManager
import com.aliernfrog.lactool.util.staticutil.GeneralUtil
import com.aliernfrog.toptoast.enum.TopToastType
import kotlinx.coroutines.launch
import org.koin.androidx.compose.getViewModel


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    settingsViewModel: SettingsViewModel = getViewModel()
) {
    AppScaffold(
        title = stringResource(R.string.settings),
        topAppBarState = settingsViewModel.topAppBarState
    ) {
        Column(Modifier.fillMaxSize().verticalScroll(settingsViewModel.scrollState)) {
            AppearanceOptions()
            GeneralOptions()
            AboutApp()
            if (settingsViewModel.experimentalSettingsShown) ExperimentalSettings()
        }
    }
    if (settingsViewModel.pathOptionsDialogShown) PathOptionsDialog(
        topToastState = settingsViewModel.topToastState,
        prefs = settingsViewModel.prefs,
        onDismissRequest = {
            settingsViewModel.pathOptionsDialogShown = false
        }
    )
}

@Composable
private fun AppearanceOptions(
    settingsViewModel: SettingsViewModel = getViewModel()
) {
    val themeOptions = listOf(
        stringResource(R.string.settings_appearance_theme_system),
        stringResource(R.string.settings_appearance_theme_light),
        stringResource(R.string.settings_appearance_theme_dark)
    )
    FormSection(title = stringResource(R.string.settings_appearance)) {
        ExpandableRow(
            expanded = settingsViewModel.themeOptionsExpanded,
            title = stringResource(R.string.settings_appearance_theme),
            description = stringResource(R.string.settings_appearance_theme_description),
            onClickHeader = {
                settingsViewModel.themeOptionsExpanded = !settingsViewModel.themeOptionsExpanded
            }
        ) {
            RadioButtons(
                options = themeOptions,
                selectedOptionIndex = settingsViewModel.prefs.theme
            ) {
                settingsViewModel.prefs.theme = it
            }
        }
        if (settingsViewModel.showMaterialYouOption) SwitchRow(
            title = stringResource(R.string.settings_appearance_materialYou),
            description = stringResource(R.string.settings_appearance_materialYou_description),
            checked = settingsViewModel.prefs.materialYou
        ) {
            settingsViewModel.prefs.materialYou = it
        }
    }
}

@Composable
private fun GeneralOptions(
    settingsViewModel: SettingsViewModel = getViewModel()
) {
    FormSection(title = stringResource(R.string.settings_general)) {
        SwitchRow(
            title = stringResource(R.string.settings_general_showChosenMapThumbnail),
            description = stringResource(R.string.settings_general_showChosenMapThumbnail_description),
            checked = settingsViewModel.prefs.showChosenMapThumbnail,
            onCheckedChange = {
                settingsViewModel.prefs.showChosenMapThumbnail = it
            }
        )
        SwitchRow(
            title = stringResource(R.string.settings_general_showMapThumbnailsInList),
            description = stringResource(R.string.settings_general_showMapThumbnailsInList_description),
            checked = settingsViewModel.prefs.showMapThumbnailsInList
        ) {
            settingsViewModel.prefs.showMapThumbnailsInList = it
        }
        ButtonRow(
            title = stringResource(R.string.settings_general_pathOptions),
            description = stringResource(R.string.settings_general_pathOptions_description),
            expanded = false,
            arrowRotation = 90f
        ) {
            settingsViewModel.pathOptionsDialogShown = true
        }
    }
}

@Composable
private fun AboutApp(
    mainViewModel: MainViewModel = getViewModel(),
    settingsViewModel: SettingsViewModel = getViewModel()
) {
    val scope = rememberCoroutineScope()
    val version = "${mainViewModel.applicationVersionName} (${mainViewModel.applicationVersionCode})"
    FormSection(title = stringResource(R.string.settings_about), bottomDivider = false) {
        ButtonRow(
            title = stringResource(R.string.settings_about_version),
            description = version,
            trailingComponent = {
                OutlinedButton(
                    onClick = { scope.launch {
                        mainViewModel.checkUpdates(manuallyTriggered = true)
                    } }
                ) {
                    Text(stringResource(R.string.settings_about_checkUpdates))
                }
            }
        ) {
            settingsViewModel.onAboutClick()
        }
        SwitchRow(
            title = stringResource(R.string.settings_about_autoCheckUpdates),
            description = stringResource(R.string.settings_about_autoCheckUpdates_description),
            checked = settingsViewModel.prefs.autoCheckUpdates
        ) {
            settingsViewModel.prefs.autoCheckUpdates = it
        }
        Links(
            linksExpanded = settingsViewModel.linksExpanded,
            onLinksExpandedStateChange = { settingsViewModel.linksExpanded = it }
        )
    }
}

@Composable
private fun Links(
    linksExpanded: Boolean,
    onLinksExpandedStateChange: (Boolean) -> Unit
) {
    val uriHandler = LocalUriHandler.current
    ExpandableRow(
        expanded = linksExpanded,
        title = stringResource(R.string.settings_about_links),
        description = stringResource(R.string.settings_about_links_description),
        onClickHeader = {
            onLinksExpandedStateChange(!linksExpanded)
        }
    ) {
        SettingsConstant.socials.forEach {
            val icon = when (it.url.split("/")[2]) {
                "discord.gg" -> painterResource(id = R.drawable.discord)
                "github.com" -> painterResource(id = R.drawable.github)
                else -> null
            }
            ButtonRow(
                title = it.name,
                description = it.url,
                painter = icon,
                contentPadding = PaddingValues(horizontal = 8.dp),
                contentColor = MaterialTheme.colorScheme.onSurfaceVariant
            ) {
                uriHandler.openUri(it.url)
            }
        }
    }
}

@OptIn(ExperimentalMaterialApi::class)
@Composable
private fun ExperimentalSettings(
    mainViewModel: MainViewModel = getViewModel(),
    settingsViewModel: SettingsViewModel = getViewModel(),
    prefs: PreferenceManager = settingsViewModel.prefs
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    FormSection(title = stringResource(R.string.settings_experimental), bottomDivider = false, topDivider = true) {
        Text(stringResource(R.string.settings_experimental_description), color = MaterialTheme.colorScheme.error, modifier = Modifier.padding(horizontal = 16.dp))
        SwitchRow(
            title = stringResource(R.string.settings_experimental_showMaterialYouOption),
            checked = settingsViewModel.showMaterialYouOption,
            onCheckedChange = {
                settingsViewModel.showMaterialYouOption = it
            }
        )
        ButtonRow(
            title = stringResource(R.string.settings_experimental_checkUpdates)
        ) {
            scope.launch { mainViewModel.checkUpdates(ignoreVersion = true) }
        }
        ButtonRow(
            title = stringResource(R.string.settings_experimental_showUpdateToast)
        ) {
            mainViewModel.showUpdateToast()
        }
        ButtonRow(
            title = stringResource(R.string.settings_experimental_showUpdateDialog)
        ) {
            scope.launch { mainViewModel.updateSheetState.show() }
        }
        SettingsConstant.experimentalPrefOptions.forEach { prefEdit ->
            val value = remember { mutableStateOf(
                prefs.getString(prefEdit.key, prefEdit.default)
            ) }
            TextField(
                label = { Text(text = "Prefs: ${prefEdit.key}") },
                value = value.value,
                modifier = Modifier.padding(horizontal = 8.dp),
                contentColor = MaterialTheme.colorScheme.onSurface,
                containerColor = MaterialTheme.colorScheme.surface,
                rounded = false,
                onValueChange = {
                    value.value = it
                    prefs.putString(prefEdit.key, it)
                }
            )
        }
        ButtonRow(title = stringResource(R.string.settings_experimental_resetPrefs), contentColor = MaterialTheme.colorScheme.error) {
            SettingsConstant.experimentalPrefOptions.forEach {
                prefs.putString(it.key, it.default)
            }
            settingsViewModel.topToastState.showToast(
                text = R.string.settings_experimental_resetPrefsDone,
                icon = Icons.Rounded.Done,
                type = TopToastType.ANDROID
            )
            GeneralUtil.restartApp(context)
        }
    }
}