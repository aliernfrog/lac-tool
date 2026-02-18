package com.aliernfrog.lactool.ui.component.widget.media_overlay.maps

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddPhotoAlternate
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.aliernfrog.lactool.R
import com.aliernfrog.lactool.impl.MapFile
import com.aliernfrog.lactool.ui.dialog.BackupMapThumbnailDialog
import com.aliernfrog.lactool.ui.viewmodel.MapsViewModel
import com.aliernfrog.lactool.util.staticutil.FileUtil
import io.github.aliernfrog.pftool_shared.impl.FileWrapper
import io.github.aliernfrog.pftool_shared.impl.Progress
import io.github.aliernfrog.pftool_shared.util.staticutil.PFToolSharedUtil
import io.github.aliernfrog.shared.ui.component.ButtonIcon
import io.github.aliernfrog.shared.ui.component.IconButtonWithTooltip
import io.github.aliernfrog.shared.ui.dialog.DeleteConfirmationDialog
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun MapThumbnailToolbarContent(
    map: MapFile,
    vm: MapsViewModel,
    onDismissMediaOverlayRequest: () -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val hasThumbnail = map.thumbnailModel != null
    var showBackupReminderDialog by remember { mutableStateOf(false) }
    var showDeleteDialog by remember { mutableStateOf(false) }

    val thumbnailPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        if (uri != null) scope.launch {
            vm.activeProgress = Progress(context.getString(R.string.maps_thumbnail_setting))
            map.runInIOThreadSafe {
                val cachedFile = PFToolSharedUtil.cacheFile(uri, "maps", context)
                map.setThumbnailFile(context, FileWrapper(cachedFile!!))
                vm.viewMapDetails(map)
                vm.openMapThumbnailViewer(map)
                vm.topToastState.showToast(
                    text = R.string.maps_thumbnail_set_done,
                    icon = Icons.Default.Check
                )
            }
            vm.activeProgress = null
        }
    }

    fun launchThumbnailPicker() {
        thumbnailPickerLauncher.launch(
            PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
        )
    }

    if (hasThumbnail) IconButtonWithTooltip(
        icon = rememberVectorPainter(Icons.Default.Delete),
        contentDescription = stringResource(R.string.action_delete),
        onClick = { showDeleteDialog = true },
        colors = IconButtonDefaults.iconButtonColors(
            contentColor = MaterialTheme.colorScheme.error
        ),
        modifier = Modifier.padding(end = 4.dp)
    )

    Button(
        onClick = {
            if (hasThumbnail && vm.prefs.showMapThumbnailBackupReminder.value) showBackupReminderDialog = true
            else launchThumbnailPicker()
        },
        shapes = ButtonDefaults.shapes()
    ) {
        ButtonIcon(rememberVectorPainter(Icons.Default.AddPhotoAlternate))
        Text(stringResource(R.string.maps_thumbnail_set))
    }

    if (hasThumbnail) IconButtonWithTooltip(
        icon = rememberVectorPainter(Icons.Default.Share),
        contentDescription = stringResource(R.string.action_share),
        onClick = {
            scope.launch {
                vm.activeProgress = Progress(context.getString(R.string.info_sharing))
                map.runInIOThreadSafe {
                    FileUtil.shareFiles(map.getThumbnailFile()!!, context = context)
                }
                vm.activeProgress = null
            }
        },
        modifier = Modifier.padding(start = 4.dp)
    )

    if (showBackupReminderDialog) BackupMapThumbnailDialog(
        onConfirm = { doNotShowAgain ->
            vm.prefs.showMapThumbnailBackupReminder.value = !doNotShowAgain
            showBackupReminderDialog = false
            launchThumbnailPicker()
        },
        onDismissRequest = {
            showBackupReminderDialog = false
        }
    )

    if (showDeleteDialog) DeleteConfirmationDialog(
        name = stringResource(R.string.maps_thumbnail_id).replace("{MAP}", map.name),
        onDismissRequest = { showDeleteDialog = false },
        onConfirmDelete = {
            scope.launch {
                vm.activeProgress = Progress(context.getString(R.string.maps_thumbnail_deleting))
                map.runInIOThreadSafe {
                    map.deleteThumbnailFile()
                    vm.viewMapDetails(map)
                    onDismissMediaOverlayRequest()
                    showDeleteDialog = false
                    vm.topToastState.showToast(
                        text = R.string.maps_thumbnail_deleted,
                        icon = Icons.Default.Delete
                    )
                }
                vm.activeProgress = null
            }
        }
    )
}