package com.aliernfrog.lactool.ui.component.widget.media_overlay.wallpapers

import android.content.ClipData
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.AddToHomeScreen
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import androidx.compose.ui.platform.ClipEntry
import androidx.compose.ui.platform.LocalClipboard
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.aliernfrog.lactool.R
import com.aliernfrog.lactool.ui.viewmodel.WallpapersViewModel
import com.aliernfrog.lactool.util.staticutil.GeneralUtil
import io.github.aliernfrog.pftool_shared.impl.FileWrapper
import io.github.aliernfrog.pftool_shared.ui.dialog.CustomMessageDialog
import io.github.aliernfrog.shared.ui.component.ButtonIcon
import io.github.aliernfrog.shared.ui.component.IconButtonWithTooltip
import io.github.aliernfrog.shared.ui.dialog.DeleteConfirmationDialog
import kotlinx.coroutines.launch
import org.koin.androidx.compose.koinViewModel

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun WallpaperToolbarContent(
    wallpaper: FileWrapper,
    vm: WallpapersViewModel = koinViewModel(),
    onDismissMediaOverlayRequest: () -> Unit
) {
    val context = LocalContext.current
    val clipboard = LocalClipboard.current
    val scope = rememberCoroutineScope()
    var showHelpDialog by remember { mutableStateOf(false) }
    var showDeleteDialog by remember { mutableStateOf(false) }

    IconButtonWithTooltip(
        icon = rememberVectorPainter(Icons.Default.Delete),
        contentDescription = stringResource(R.string.action_delete),
        onClick = { showDeleteDialog = true },
        colors = IconButtonDefaults.iconButtonColors(
            contentColor = MaterialTheme.colorScheme.error
        )
    )

    Spacer(Modifier.width(4.dp))

    Button(
        onClick = {
            scope.launch {
                clipboard.setClipEntry(ClipEntry(ClipData.newPlainText(
                    null,
                    GeneralUtil.generateWallpaperImportUrl(
                        fileName = wallpaper.name,
                        wallpapersPath = vm.wallpapersDir
                    )
                )))
                showHelpDialog = true
            }
        },
        shapes = ButtonDefaults.shapes()
    ) {
        ButtonIcon(rememberVectorPainter(Icons.AutoMirrored.Filled.AddToHomeScreen))
        Text(stringResource(R.string.wallpapers_use))
    }

    Spacer(Modifier.width(4.dp))

    IconButton(
        onClick = { scope.launch {
            vm.shareImportedWallpaper(wallpaper, context)
        } },
        shapes = IconButtonDefaults.shapes()
    ) {
        Icon(
            imageVector = Icons.Default.Share,
            contentDescription = stringResource(R.string.action_share)
        )
    }

    if (showHelpDialog) CustomMessageDialog(
        title = stringResource(R.string.wallpapers_copied_title),
        text = stringResource(R.string.wallpapers_copied_description),
        dismissButtonText = stringResource(R.string.action_ok),
        icon = Icons.Default.ContentCopy,
        onDismissRequest = { showHelpDialog = false }
    )

    if (showDeleteDialog) DeleteConfirmationDialog(
        name = wallpaper.name,
        onDismissRequest = { showDeleteDialog = false },
        onConfirmDelete = {
            showDeleteDialog = false
            scope.launch {
                vm.deleteImportedWallpaper(wallpaper, context)
            }
            onDismissMediaOverlayRequest()
        }
    )
}