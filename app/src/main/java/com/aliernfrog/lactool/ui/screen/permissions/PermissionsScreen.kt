package com.aliernfrog.lactool.ui.screen.permissions

import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import com.aliernfrog.lactool.ui.component.SettingsButton
import com.aliernfrog.lactool.util.AppSettingsDestination
import com.aliernfrog.lactool.util.staticutil.GeneralUtil
import io.github.aliernfrog.pftool_shared.data.PermissionData
import io.github.aliernfrog.pftool_shared.ui.screen.permissions.PermissionsScreen
import io.github.aliernfrog.shared.ui.screen.settings.SettingsDestination

@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun PermissionsScreen(
    vararg permissionsData: PermissionData,
    title: String,
    onNavigateRequest: (Any) -> Unit,
    content: @Composable () -> Unit
) {
    val context = LocalContext.current

    PermissionsScreen(
        permissionsData = permissionsData,
        title = title,
        onRestartAppRequest = {
            GeneralUtil.restartApp(context, withModules = true)
        },
        onNavigateStorageSettingsRequest = {
            onNavigateRequest(AppSettingsDestination.storage)
        },
        settingsButton = {
            SettingsButton {
                onNavigateRequest(SettingsDestination.root)
            }
        },
        content = content
    )
}