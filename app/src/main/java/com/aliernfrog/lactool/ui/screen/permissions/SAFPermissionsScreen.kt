package com.aliernfrog.lactool.ui.screen.permissions

import android.net.Uri
import android.os.Environment
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.aliernfrog.lactool.R
import com.aliernfrog.lactool.data.PermissionData
import com.aliernfrog.lactool.data.requiresAndroidData
import com.aliernfrog.lactool.enum.SAFWorkaroundLevel
import com.aliernfrog.lactool.ui.component.form.DividerRow
import com.aliernfrog.lactool.ui.dialog.ChooseFolderIntroDialog
import com.aliernfrog.lactool.ui.dialog.UnrecommendedFolderDialog
import com.aliernfrog.lactool.ui.viewmodel.PermissionsViewModel
import com.aliernfrog.lactool.util.extension.takePersistablePermissions
import com.aliernfrog.lactool.util.extension.toPath
import com.aliernfrog.lactool.util.staticutil.FileUtil
import org.koin.androidx.compose.koinViewModel

@Composable
fun SAFPermissionsScreen(
    vararg permissionsData: PermissionData,
    permissionsViewModel: PermissionsViewModel = koinViewModel(),
    onUpdateStateRequest: () -> Unit
) {
    val context = LocalContext.current
    var missingPermissions by remember { mutableStateOf(
        permissionsViewModel.getMissingUriPermissions(*permissionsData, context = context)
    ) }

    fun onUpdateState() {
        missingPermissions = permissionsViewModel.getMissingUriPermissions(*permissionsData, context = context)
        onUpdateStateRequest()
    }

    var activePermissionData by remember { mutableStateOf<PermissionData?>(null) }
    var unrecommendedPathWarningUri by remember { mutableStateOf<Uri?>(null) }

    fun takePersistableUriPermissions(uri: Uri) {
        uri.takePersistablePermissions(context)
        activePermissionData?.onUriUpdate?.invoke(uri)
        onUpdateState()
    }

    val uriPermsLauncher = rememberLauncherForActivityResult(contract = ActivityResultContracts.OpenDocumentTree(), onResult = {
        if (it == null) return@rememberLauncherForActivityResult if (activePermissionData?.requiresAndroidData != true) {}
        else unrecommendedPathWarningUri = Uri.EMPTY

        if (activePermissionData?.forceRecommendedPath == true) {
            val recommendedPath = activePermissionData?.recommendedPath
            val resolvedPath = it.toPath()
            val isRecommendedPath = resolvedPath.equals(recommendedPath, ignoreCase = true)
            if (!isRecommendedPath) unrecommendedPathWarningUri = it
            else takePersistableUriPermissions(it)
        } else takePersistableUriPermissions(it)
    })

    fun openFolderPicker(permissionData: PermissionData) {
        val starterUri = permissionData.recommendedPath?.let {
            FileUtil.getUriForPath(it)
        }
        uriPermsLauncher.launch(starterUri)
        activePermissionData = permissionData
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize()
    ) {
        itemsIndexed(missingPermissions) { index, permissionData ->
            var introDialogShown by remember { mutableStateOf(false) }
            if (introDialogShown) ChooseFolderIntroDialog(
                permissionData = permissionData,
                onDismissRequest = { introDialogShown = false },
                onConfirm = {
                    openFolderPicker(permissionData)
                    introDialogShown = false
                }
            )

            fun onClick() {
                if (permissionData.recommendedPath != null && permissionData.recommendedPathDescription != null)
                    introDialogShown = true
                else openFolderPicker(permissionData)
            }

            if (index != 0) DividerRow(Modifier.fillMaxWidth())
            ListItem(
                headlineContent = { Text(stringResource(permissionData.title)) },
                supportingContent = {
                    Column(Modifier.fillMaxWidth()) {
                        permissionData.content()

                        val isAndroidData = permissionData.forceRecommendedPath && permissionData.recommendedPath
                            ?.startsWith("${Environment.getExternalStorageDirectory()}/Android/data") == true
                        if (isAndroidData) Guide(
                            level = permissionsViewModel.safWorkaroundLevel,
                            permissionData = permissionData
                        )

                        Button(
                            onClick = ::onClick,
                            modifier = Modifier
                                .align(Alignment.End)
                                .padding(top = 4.dp)
                        ) {
                            Text(stringResource(R.string.permissions_chooseFolder))
                        }
                    }
                },
                modifier = Modifier.clickable(onClick = ::onClick)
            )
        }

        item {
            Spacer(Modifier.navigationBarsPadding())
        }
    }

    unrecommendedPathWarningUri?.let { uri ->
        UnrecommendedFolderDialog(
            permissionData = activePermissionData!!,
            chosenUri = uri,
            onDismissRequest = { unrecommendedPathWarningUri = null },
            onFolderDoesNotExist = {
                unrecommendedPathWarningUri = null
                permissionsViewModel.pushSAFWorkaroundLevel()
                permissionsViewModel.showSAFWorkaroundDialog = true
            },
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

@Composable
private fun Guide(
    level: SAFWorkaroundLevel,
    permissionData: PermissionData
) {
    val title = level.title
    val description = if (level == SAFWorkaroundLevel.MAKE_SURE_FOLDER_EXISTS) permissionData.createFolderHint
    else level.description

    if (title != null || description != null) Card(
        modifier = Modifier.padding(vertical = 4.dp)
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            title?.let {
                Text(
                    text = stringResource(it),
                    style = MaterialTheme.typography.titleMedium
                )
            }
            description?.let {
                Text(
                    text = stringResource(it)
                )
            }
            level.button?.let { button ->
                Row(
                    horizontalArrangement = Arrangement.aligned(Alignment.End),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    button()
                }
            }
        }
    }
}