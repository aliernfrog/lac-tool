package com.aliernfrog.lactool.ui.screen

import android.os.Build
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.FolderOpen
import androidx.compose.material.icons.rounded.Info
import androidx.compose.material.icons.rounded.Palette
import androidx.compose.material.icons.rounded.PinDrop
import androidx.compose.material.icons.rounded.Science
import androidx.compose.material.icons.rounded.Translate
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.aliernfrog.lactool.R
import com.aliernfrog.lactool.SettingsConstant
import com.aliernfrog.lactool.crowdinURL
import com.aliernfrog.lactool.languages
import com.aliernfrog.lactool.ui.viewmodel.MainViewModel
import com.aliernfrog.lactool.util.extension.enable
import com.aliernfrog.lactool.util.settingsLibsDestination
import com.aliernfrog.lactool.util.staticutil.GeneralUtil
import io.github.aliernfrog.pftool_shared.impl.Progress
import io.github.aliernfrog.pftool_shared.ui.screen.settings.LanguagePage
import io.github.aliernfrog.pftool_shared.ui.screen.settings.MapsPage
import io.github.aliernfrog.pftool_shared.ui.screen.settings.StoragePage
import io.github.aliernfrog.shared.ui.component.VerticalSegmentor
import io.github.aliernfrog.shared.ui.component.expressive.ExpressiveButtonRow
import io.github.aliernfrog.shared.ui.component.expressive.ExpressiveSection
import io.github.aliernfrog.shared.ui.settings.AboutPage
import io.github.aliernfrog.shared.ui.settings.AppearancePage
import io.github.aliernfrog.shared.ui.settings.ExperimentalPage
import io.github.aliernfrog.shared.ui.settings.SettingsCategory
import io.github.aliernfrog.shared.ui.settings.SettingsDestination
import io.github.aliernfrog.shared.ui.settings.SettingsRootPage
import kotlinx.coroutines.launch
import org.koin.androidx.compose.koinViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    mainViewModel: MainViewModel = koinViewModel(),
    onNavigateBackRequest: () -> Unit,
    onNavigateRequest: (SettingsDestination) -> Unit
) {
    val context = LocalContext.current

    val updateAvailable = mainViewModel.updateAvailable.collectAsState()
    val latestVersionInfo = mainViewModel.latestVersionInfo.collectAsState()

    val categories = rememberSaveable { listOf(
        SettingsCategory(
            title = context.getString(R.string.settings_category_game),
            destinations = listOf(
                SettingsDestination(
                    title = context.getString(R.string.settings_maps),
                    description = context.getString(R.string.settings_maps_description),
                    icon = Icons.Rounded.PinDrop,
                    iconContainerColor = Color.Green,
                    content = { onNavigateBackRequest, _ ->
                        MapsPage(
                            showChosenMapThumbnailPref = mainViewModel.prefs.showChosenMapThumbnail,
                            showMapThumbnailsInListPref = mainViewModel.prefs.showMapThumbnailsInList,
                            stackupMapsPref = mainViewModel.prefs.stackupMaps,
                            onNavigateBackRequest = onNavigateBackRequest
                        )
                    }
                ),
                SettingsDestination(
                    title = context.getString(R.string.settings_storage),
                    description = context.getString(R.string.settings_storage_description),
                    icon = Icons.Rounded.FolderOpen,
                    iconContainerColor = Color.Blue,
                    content = { onNavigateBackRequest, _ ->
                        StoragePage(
                            storageAccessTypePref = mainViewModel.prefs.storageAccessType,
                            folderPrefs = mapOf(
                                context.getString(R.string.settings_storage_folders_maps) to mainViewModel.prefs.lacMapsDir,
                                context.getString(R.string.settings_storage_folders_exportedMaps) to mainViewModel.prefs.exportedMapsDir
                            ),
                            onEnableStorageAccessTypeRequest = { it.enable() },
                            onNavigateBackRequest = onNavigateBackRequest
                        )
                    }
                )
            )
        ),
        SettingsCategory(
            title = context.getString(R.string.settings_category_app),
            destinations = listOf(
                SettingsDestination(
                    title = context.getString(R.string.settings_appearance),
                    description = context.getString(R.string.settings_appearance_description),
                    icon = Icons.Rounded.Palette,
                    iconContainerColor = Color.Yellow,
                    content = { onNavigateBackRequest, _ ->
                        AppearancePage(
                            themePref = mainViewModel.prefs.theme,
                            materialYouPref = mainViewModel.prefs.materialYou,
                            pitchBlackPref = mainViewModel.prefs.pitchBlack,
                            onNavigateBackRequest = onNavigateBackRequest
                        )
                    }
                ),
                SettingsDestination(
                    title = context.getString(R.string.settings_language),
                    description = context.getString(R.string.settings_language_description),
                    icon = Icons.Rounded.Translate,
                    iconContainerColor = Color.Magenta,
                    shown = Build.VERSION.SDK_INT >= Build.VERSION_CODES.N,
                    content = { onNavigateBackRequest, _ ->
                        LanguagePage(
                            crowdinURL = crowdinURL,
                            currentLanguagePref = mainViewModel.prefs.language,
                            languages = languages,
                            appLanguage = mainViewModel.appLanguage,
                            deviceLanguage = mainViewModel.deviceLanguage,
                            baseLanguage = mainViewModel.baseLanguage,
                            onSetLanguageRequest = {
                                mainViewModel.appLanguage = it
                            },
                            onNavigateBackRequest = onNavigateBackRequest
                        )
                    }
                ),
                SettingsDestination(
                    title = context.getString(R.string.settings_experimental),
                    description = context.getString(R.string.settings_experimental_description),
                    icon = Icons.Rounded.Science,
                    iconContainerColor = Color.Black,
                    shown = mainViewModel.prefs.experimentalOptionsEnabled.value,
                    content = { onNavigateBackRequest, _ ->
                        val scope = rememberCoroutineScope()
                        ExperimentalPage(
                            experimentalPrefs = mainViewModel.prefs.experimentalPrefs,
                            experimentalOptionsEnabledPref = mainViewModel.prefs.experimentalOptionsEnabled,
                            onCheckUpdatesRequest = {
                                scope.launch {
                                    mainViewModel.checkUpdates(skipVersionCheck = true)
                                }
                            },
                            onShowUpdateSheetRequest = {
                                scope.launch {
                                    mainViewModel.updateSheetState.show()
                                }
                            },
                            onRestartAppRequest = {
                                GeneralUtil.restartApp(context)
                            },
                            onNavigateBackRequest = onNavigateBackRequest
                        ) {
                            ExpressiveSection(title = "Progress") {
                                VerticalSegmentor(
                                    {
                                        ExpressiveButtonRow(
                                            title = "Set indeterminate progress"
                                        ) {
                                            mainViewModel.progressState.currentProgress =
                                                Progress(description = context.getString(R.string.info_pleaseWait))
                                        }
                                    },
                                    modifier = Modifier.padding(horizontal = 12.dp)
                                )
                            }
                        }
                    }
                ),
                SettingsDestination(
                    title = context.getString(R.string.settings_about),
                    description = mainViewModel.applicationVersionLabel,
                    icon = Icons.Rounded.Info,
                    iconContainerColor = Color.Blue,
                    content = { onNavigateBackRequest, onNavigateRequest ->
                        val scope = rememberCoroutineScope()
                        AboutPage(
                            socials = SettingsConstant.socials,
                            credits = SettingsConstant.credits,
                            debugInfo = mainViewModel.debugInfo,
                            autoCheckUpdatesPref = mainViewModel.prefs.autoCheckUpdates,
                            experimentalOptionsEnabled = mainViewModel.prefs.experimentalOptionsEnabled.value,
                            onExperimentalOptionsEnabled = {
                                mainViewModel.prefs.experimentalOptionsEnabled.value = true
                            },
                            onShowUpdateSheetRequest = {
                                scope.launch {
                                    mainViewModel.updateSheetState.show()
                                }
                            },
                            onNavigateLibsRequest = {
                                onNavigateRequest(settingsLibsDestination)
                            },
                            onNavigateBackRequest = onNavigateBackRequest
                        )
                    }
                )
            )
        )
    ) }

    val rootPageScope = rememberCoroutineScope()
    SettingsRootPage(
        categories = categories,
        updateAvailable = updateAvailable.value,
        latestReleaseInfo = latestVersionInfo.value,
        onShowUpdateSheetRequest = { rootPageScope.launch {
            mainViewModel.updateSheetState.show()
        } },
        onNavigateBackRequest = onNavigateBackRequest,
        onNavigateRequest = onNavigateRequest
    )
}