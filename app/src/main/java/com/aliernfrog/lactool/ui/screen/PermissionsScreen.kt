package com.aliernfrog.lactool.ui.screen

import android.net.Uri
import android.os.Environment
import android.provider.DocumentsContract
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.Crossfade
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.aliernfrog.lactool.R
import com.aliernfrog.lactool.data.PermissionData
import com.aliernfrog.lactool.filesAppMightBlockAndroidData
import com.aliernfrog.lactool.ui.component.AppScaffold
import com.aliernfrog.lactool.ui.component.AppTopBar
import com.aliernfrog.lactool.ui.component.CardWithActions
import com.aliernfrog.lactool.ui.component.FilesDowngradeNotice
import com.aliernfrog.lactool.ui.component.SettingsButton
import com.aliernfrog.lactool.ui.dialog.ChooseFolderIntroDialog
import com.aliernfrog.lactool.ui.dialog.NotRecommendedFolderDialog
import com.aliernfrog.lactool.util.extension.appHasPermissions
import com.aliernfrog.lactool.util.extension.takePersistablePermissions
import com.aliernfrog.lactool.util.extension.toPath

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PermissionsScreen(
    vararg permissionsData: PermissionData,
    onNavigateSettingsRequest: () -> Unit,
    content: @Composable () -> Unit
) {
    val context = LocalContext.current
    fun getMissingPermissions(): List<PermissionData> {
        return permissionsData.filter {
            !Uri.parse(it.getUri()).appHasPermissions(context)
        }
    }

    var missingPermissions by remember { mutableStateOf(
        getMissingPermissions()
    ) }

    Crossfade(targetState = missingPermissions.isEmpty()) { hasPermissions ->
        if (hasPermissions) content()
        else AppScaffold(
            topBar = { scrollBehavior ->
                AppTopBar(
                    title = stringResource(R.string.permissions),
                    scrollBehavior = scrollBehavior,
                    actions = {
                        SettingsButton(onClick = onNavigateSettingsRequest)
                    }
                )
            }
        ) {
            PermissionsList(
                missingPermissions = missingPermissions,
                onUpdateState = {
                    missingPermissions = getMissingPermissions()
                }
            )
        }
    }
}

@Composable
private fun PermissionsList(
    missingPermissions: List<PermissionData>,
    onUpdateState: () -> Unit
) {
    val context = LocalContext.current
    var activePermissionData by remember { mutableStateOf<PermissionData?>(null) }
    var unrecommendedPathWarningUri by remember { mutableStateOf<Uri?>(null) }

    val showFilesAppWarning = filesAppMightBlockAndroidData && missingPermissions.any {
        it.recommendedPath?.startsWith("${Environment.getExternalStorageDirectory()}/Android/data") == true
    }

    fun takePersistableUriPermissions(uri: Uri) {
        uri.takePersistablePermissions(context)
        activePermissionData?.onUriUpdate?.invoke(uri)
        onUpdateState()
    }

    val uriPermsLauncher = rememberLauncherForActivityResult(contract = ActivityResultContracts.OpenDocumentTree(), onResult = {
        if (it == null) return@rememberLauncherForActivityResult
        val recommendedPath = activePermissionData?.recommendedPath
        if (recommendedPath != null) {
            val resolvedPath = it.toPath()
            val isRecommendedPath = resolvedPath.equals(recommendedPath, ignoreCase = true)
            if (!isRecommendedPath) unrecommendedPathWarningUri = it
            else takePersistableUriPermissions(it)
        } else {
            takePersistableUriPermissions(it)
        }
    })

    fun openFolderPicker(permissionData: PermissionData) {
        val starterUri = if (permissionData.recommendedPath != null) DocumentsContract.buildDocumentUri(
            "com.android.externalstorage.documents",
            "primary:"+permissionData.recommendedPath.removePrefix("${Environment.getExternalStorageDirectory()}/")
        ) else null
        uriPermsLauncher.launch(starterUri)
        activePermissionData = permissionData
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize()
    ) {
        if (showFilesAppWarning) item {
            FilesDowngradeNotice(
                Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
            )
        }

        items(missingPermissions) { permissionData ->
            var introDialogShown by remember { mutableStateOf(false) }
            if (introDialogShown) ChooseFolderIntroDialog(
                permissionData = permissionData,
                onDismissRequest = { introDialogShown = false },
                onConfirm = {
                    openFolderPicker(permissionData)
                    introDialogShown = false
                }
            )

            CardWithActions(
                title = stringResource(permissionData.titleId),
                buttons = {
                    Button(
                        onClick = {
                            if (permissionData.recommendedPath != null && permissionData.recommendedPathDescriptionId != null)
                                introDialogShown = true
                            else openFolderPicker(permissionData)
                        }
                    ) {
                        Text(stringResource(R.string.permissions_chooseFolder))
                    }
                },
                content = permissionData.content,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
            )
        }
    }

    unrecommendedPathWarningUri?.let { uri ->
        NotRecommendedFolderDialog(
            permissionData = activePermissionData!!,
            onDismissRequest = { unrecommendedPathWarningUri = null },
            onUseUnrecommendedFolderRequest = {
                takePersistableUriPermissions(uri)
                unrecommendedPathWarningUri = null
            },
            onChooseFolderRequest = {
                activePermissionData?.let { openFolderPicker(it) }
                unrecommendedPathWarningUri = null
            }
        )
    }
}