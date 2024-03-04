package com.aliernfrog.lactool.ui.screen.permissions

import androidx.compose.animation.AnimatedContent
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import com.aliernfrog.lactool.data.PermissionData
import com.aliernfrog.lactool.enum.StorageAccessType
import com.aliernfrog.lactool.ui.component.AppScaffold
import com.aliernfrog.lactool.ui.component.AppTopBar
import com.aliernfrog.lactool.ui.component.SettingsButton
import com.aliernfrog.lactool.ui.dialog.CustomMessageDialog
import com.aliernfrog.lactool.ui.viewmodel.PermissionsViewModel
import com.aliernfrog.lactool.ui.viewmodel.ShizukuViewModel
import org.koin.androidx.compose.koinViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PermissionsScreen(
    vararg permissionsData: PermissionData,
    title: String,
    permissionsViewModel: PermissionsViewModel = koinViewModel(),
    shizukuViewModel: ShizukuViewModel = koinViewModel(),
    onNavigateSettingsRequest: () -> Unit,
    content: @Composable () -> Unit
) {
    val context = LocalContext.current

    fun hasPermissions(): Boolean {
        return permissionsViewModel.hasPermissions(
            *permissionsData,
            isShizukuFileServiceRunning = shizukuViewModel.fileServiceRunning,
            context = context
        )
    }

    AnimatedContent(
        StorageAccessType.entries[permissionsViewModel.prefs.storageAccessType]
    ) { method ->
        var permissionsGranted by remember { mutableStateOf(hasPermissions()) }

        AnimatedContent(permissionsGranted) { showContent ->
            if (showContent) content()
            else AppScaffold(
                topBar = { AppTopBar(
                    title = title,
                    scrollBehavior = it,
                    actions = {
                        SettingsButton(onClick = onNavigateSettingsRequest)
                    }
                ) }
            ) {
                when (method) {
                    StorageAccessType.SAF -> SAFPermissionsScreen(
                        *permissionsData,
                        onUpdateStateRequest = {
                            permissionsGranted = hasPermissions()
                        }
                    )
                    StorageAccessType.SHIZUKU -> ShizukuPermissionsScreen(
                        onUpdateStateRequest = {
                            permissionsGranted = hasPermissions()
                        }
                    )
                }
            }
        }
    }

    if (permissionsViewModel.showSAFWorkaroundDialog) permissionsViewModel.safWorkaroundLevel.let { level ->
        CustomMessageDialog(
            title = level.title?.let { stringResource(it) },
            text = level.description?.let { stringResource(it) },
            confirmButton = level.button,
            onDismissRequest = { permissionsViewModel.showSAFWorkaroundDialog = false }
        )
    }
}