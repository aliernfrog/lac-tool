package com.aliernfrog.lactool.ui.screen

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Science
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.aliernfrog.lactool.R
import com.aliernfrog.lactool.SettingsConstant.credits
import com.aliernfrog.lactool.SettingsConstant.socials
import com.aliernfrog.lactool.SettingsConstant.supportLinks
import com.aliernfrog.lactool.crowdinURL
import com.aliernfrog.lactool.ui.component.widget.settings.ExperimentalOptions
import com.aliernfrog.lactool.ui.viewmodel.SettingsViewModel
import com.aliernfrog.lactool.util.AppSettingsDestination
import com.aliernfrog.lactool.util.extension.enable
import com.aliernfrog.lactool.util.staticutil.GeneralUtil
import io.github.aliernfrog.pftool_shared.impl.Progress
import io.github.aliernfrog.pftool_shared.ui.screen.settings.LanguagePage
import io.github.aliernfrog.pftool_shared.ui.screen.settings.MapsPage
import io.github.aliernfrog.pftool_shared.ui.screen.settings.StoragePage
import io.github.aliernfrog.shared.ui.screen.settings.AboutPage
import io.github.aliernfrog.shared.ui.screen.settings.AppearancePage
import io.github.aliernfrog.shared.ui.screen.settings.ExperimentalPage
import io.github.aliernfrog.shared.ui.screen.settings.LibsPage
import io.github.aliernfrog.shared.ui.screen.settings.SettingsDestination
import io.github.aliernfrog.shared.ui.screen.settings.SettingsRootPage
import io.github.aliernfrog.shared.util.resolve
import org.koin.androidx.compose.koinViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    destination: SettingsDestination,
    vm: SettingsViewModel = koinViewModel(),
    onCheckUpdatesRequest: (skipVersionCheck: Boolean) -> Unit,
    onNavigateUpdatesScreenRequest: () -> Unit,
    onNavigateBackRequest: () -> Unit,
    onNavigateRequest: (SettingsDestination) -> Unit
) {
    val context = LocalContext.current
    val availableUpdates = vm.versionManager.availableUpdates.collectAsStateWithLifecycle().value

    when (destination) {
        SettingsDestination.root -> {
            SettingsRootPage(
                categories = vm.categories,
                availableUpdates = availableUpdates,
                experimentalOptionsEnabled = vm.prefs.experimentalOptionsEnabled.value,
                onShowUpdateSheetRequest = onNavigateUpdatesScreenRequest,
                onNavigateBackRequest = onNavigateBackRequest,
                onNavigateRequest = onNavigateRequest
            )
        }

        AppSettingsDestination.maps -> {
            MapsPage(
                showChosenMapThumbnailPref = vm.prefs.showChosenMapThumbnail,
                showMapThumbnailsInListPref = vm.prefs.showMapThumbnailsInList,
                stackupMapsPref = vm.prefs.stackupMaps,
                onNavigateBackRequest = onNavigateBackRequest
            )
        }

        AppSettingsDestination.storage -> {
            StoragePage(
                storageAccessTypePref = vm.prefs.storageAccessType,
                folderPrefs = mapOf(
                    stringResource(R.string.settings_storage_folders_maps) to vm.prefs.lacMapsDir,
                    stringResource(R.string.settings_storage_folders_wallpapers) to vm.prefs.lacWallpapersDir,
                    stringResource(R.string.settings_storage_folders_screenshots) to vm.prefs.lacScreenshotsDir,
                    stringResource(R.string.settings_storage_folders_exportedMaps) to vm.prefs.exportedMapsDir
                ),
                onEnableStorageAccessTypeRequest = { it.enable() },
                onNavigateBackRequest = onNavigateBackRequest
            )
        }

        SettingsDestination.appearance -> {
            AppearancePage(
                themePref = vm.prefs.theme,
                materialYouPref = vm.prefs.materialYou,
                pitchBlackPref = vm.prefs.pitchBlack,
                onNavigateBackRequest = onNavigateBackRequest
            )
        }

        AppSettingsDestination.language -> {
            LanguagePage(
                crowdinURL = crowdinURL,
                currentLanguagePref = vm.prefs.language,
                languages = vm.localeManager.languages,
                appLanguage = vm.localeManager.appLanguage,
                deviceLanguage = vm.localeManager.deviceLanguage,
                baseLanguage = vm.localeManager.baseLanguage,
                onSetLanguageRequest = {
                    vm.localeManager.appLanguage = it
                },
                onNavigateBackRequest = onNavigateBackRequest
            )
        }

        SettingsDestination.experimental -> {
            ExperimentalPage(
                experimentalPrefs = vm.prefs.experimentalPrefs,
                experimentalOptionsEnabledPref = vm.prefs.experimentalOptionsEnabled,
                onCheckUpdatesRequest = onCheckUpdatesRequest,
                onNavigateUpdatesScreenRequest = onNavigateUpdatesScreenRequest,
                onRestartAppRequest = { GeneralUtil.restartApp(context, withModules = true) },
                onNavigateBackRequest = onNavigateBackRequest
            ) {
                ExperimentalOptions(
                    onSetIndeterminateProgressRequest = {
                        vm.progressState.currentProgress =
                            Progress(description = context.getString(R.string.info_pleaseWait))
                    }
                )
            }
        }

        SettingsDestination.about -> {
            AboutPage(
                socials = socials,
                credits = credits,
                supportLinks = supportLinks,
                debugInfo = vm.debugInfo,
                autoCheckUpdatesPref = vm.prefs.autoCheckUpdates,
                experimentalOptionsEnabled = vm.prefs.experimentalOptionsEnabled.value,
                onExperimentalOptionsEnabled = {
                    vm.prefs.experimentalOptionsEnabled.value = true
                    vm.topToastState.showToast(
                        text = "Experimental options enabled",
                        icon = Icons.Rounded.Science
                    )
                },
                onShowUpdateSheetRequest = onNavigateUpdatesScreenRequest,
                onNavigateLibsRequest = { onNavigateRequest(SettingsDestination.libs) },
                onNavigateBackRequest = onNavigateBackRequest
            )
        }

        SettingsDestination.libs -> {
            LibsPage(
                librariesJSONRes = R.raw.aboutlibraries,
                onNavigateBackRequest = onNavigateBackRequest
            )
        }

        else -> {
            Text("UNKNOWN DESTINATION: ${destination.title.resolve()}")
        }
    }
}