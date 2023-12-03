package com.aliernfrog.lactool.ui.screen

import androidx.compose.animation.*
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Update
import androidx.compose.material.icons.rounded.Done
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.aliernfrog.lactool.*
import com.aliernfrog.lactool.R
import com.aliernfrog.lactool.data.ReleaseInfo
import com.aliernfrog.lactool.ui.component.*
import com.aliernfrog.lactool.ui.component.form.ButtonRow
import com.aliernfrog.lactool.ui.component.form.ExpandableRow
import com.aliernfrog.lactool.ui.component.form.FormSection
import com.aliernfrog.lactool.ui.component.form.SwitchRow
import com.aliernfrog.lactool.ui.dialog.FolderConfigurationDialog
import com.aliernfrog.lactool.ui.theme.AppComponentShape
import com.aliernfrog.lactool.ui.viewmodel.MainViewModel
import com.aliernfrog.lactool.ui.viewmodel.SettingsViewModel
import com.aliernfrog.lactool.util.staticutil.GeneralUtil
import com.aliernfrog.toptoast.enum.TopToastType
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
            UpdateNotification(
                isShown = mainViewModel.updateAvailable,
                versionInfo = mainViewModel.latestVersionInfo
            ) { scope.launch {
                mainViewModel.updateSheetState.show()
            } }

            AppearanceOptions()
            GeneralOptions()
            AboutApp()
            if (settingsViewModel.experimentalSettingsShown) ExperimentalSettings()
        }
    }
    if (settingsViewModel.folderConfigurationDialogShown) FolderConfigurationDialog(
        onDismissRequest = {
            settingsViewModel.folderConfigurationDialogShown = false
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
            title = stringResource(R.string.settings_general_folders),
            description = stringResource(R.string.settings_general_folders_description),
            expanded = false,
            arrowRotation = 90f
        ) {
            settingsViewModel.folderConfigurationDialogShown = true
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
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
                UpdateButton(
                    updateAvailable = mainViewModel.updateAvailable
                ) { updateAvailable -> scope.launch {
                    if (updateAvailable) mainViewModel.updateSheetState.show()
                    else mainViewModel.checkUpdates(manuallyTriggered = true)
                } }
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ExperimentalSettings(
    mainViewModel: MainViewModel = getViewModel(),
    settingsViewModel: SettingsViewModel = getViewModel()
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
            OutlinedTextField(
                value = prefEdit.getValue(settingsViewModel.prefs),
                onValueChange = {
                    prefEdit.setValue(it, settingsViewModel.prefs)
                },
                label = {
                    Text(stringResource(prefEdit.labelResourceId))
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp)
            )
        }
        ButtonRow(title = stringResource(R.string.settings_experimental_resetPrefs), contentColor = MaterialTheme.colorScheme.error) {
            SettingsConstant.experimentalPrefOptions.forEach {
                it.setValue(it.default, settingsViewModel.prefs)
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UpdateNotification(
    isShown: Boolean,
    versionInfo: ReleaseInfo,
    onClick: () -> Unit
) {
    AnimatedVisibility(
        visible = isShown,
        enter = fadeIn() + expandVertically(),
        exit = fadeOut() + shrinkVertically()
    ) {
        Card(
            onClick = onClick,
            shape = AppComponentShape,
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
            ) {
                Icon(imageVector = Icons.Default.Update, contentDescription = null)
                Column {
                    Text(
                        text = stringResource(R.string.settings_updateNotification_updateAvailable)
                            .replace("{VERSION}", versionInfo.versionName),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = stringResource(R.string.settings_updateNotification_description),
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }
        }
    }
}

@Composable
fun UpdateButton(
    updateAvailable: Boolean,
    onClick: (updateAvailable: Boolean) -> Unit
) {
    AnimatedContent(updateAvailable) {
        if (it) ElevatedButton(
            onClick = { onClick(true) }
        ) {
            ButtonIcon(
                rememberVectorPainter(Icons.Default.Update)
            )
            Text(stringResource(R.string.settings_about_update))
        }
        else OutlinedButton(
            onClick = { onClick(false) }
        ) {
            Text(stringResource(R.string.settings_about_checkUpdates))
        }
    }
}