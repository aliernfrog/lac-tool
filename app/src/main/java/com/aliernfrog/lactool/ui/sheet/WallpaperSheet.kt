package com.aliernfrog.lactool.ui.sheet

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.ModalBottomSheetState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ContentCopy
import androidx.compose.material.icons.rounded.Delete
import androidx.compose.material.icons.rounded.IosShare
import androidx.compose.material3.Divider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.aliernfrog.lactool.R
import com.aliernfrog.lactool.data.ImageFile
import com.aliernfrog.lactool.ui.component.ButtonShapeless
import com.aliernfrog.lactool.ui.component.ModalBottomSheet
import com.aliernfrog.lactool.ui.dialog.DeleteConfirmationDialog
import com.aliernfrog.lactool.util.staticutil.GeneralUtil
import com.aliernfrog.toptoast.state.TopToastState
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun WallpaperSheet(
    wallpaper: ImageFile?,
    wallpapersPath: String,
    state: ModalBottomSheetState,
    topToastState: TopToastState? = null,
    onShareRequest: (ImageFile) -> Unit,
    onDeleteRequest: (ImageFile) -> Unit
) {
    val clipboardManager = LocalClipboardManager.current
    val scope = rememberCoroutineScope()
    var deleteConfirmationShown by remember { mutableStateOf(false) }
    ModalBottomSheet(sheetState = state) {
        Text(
            text = wallpaper?.fileName.toString(),
            modifier = Modifier.padding(top = 8.dp).padding(horizontal = 8.dp),
            color = MaterialTheme.colorScheme.onSurface,
            fontFamily = FontFamily.Monospace
        )
        AsyncImage(
            model = wallpaper?.painterModel,
            contentDescription = null,
            modifier = Modifier.fillMaxWidth().padding(8.dp),
            contentScale = ContentScale.Crop
        )
        Divider(
            modifier = Modifier.padding(8.dp).alpha(0.7f),
            thickness = 1.dp,
            color = MaterialTheme.colorScheme.surfaceVariant
        )
        ButtonShapeless(
            title = stringResource(R.string.wallpapers_copyImportUrl),
            description = stringResource(R.string.wallpapers_copyImportUrlDescription),
            painter = rememberVectorPainter(Icons.Rounded.ContentCopy)
        ) {
            clipboardManager.setText(AnnotatedString(GeneralUtil.generateWallpaperImportUrl(wallpaper?.fileName.toString(), wallpapersPath)))
            topToastState?.showToast(R.string.info_copiedToClipboard, Icons.Rounded.ContentCopy)
            scope.launch { state.hide() }
        }
        ButtonShapeless(
            title = stringResource(R.string.wallpapers_share),
            painter = rememberVectorPainter(Icons.Rounded.IosShare)
        ) {
            if (wallpaper != null) onShareRequest(wallpaper)
        }
        ButtonShapeless(
            title = stringResource(R.string.wallpapers_delete),
            painter = rememberVectorPainter(Icons.Rounded.Delete),
            contentColor = MaterialTheme.colorScheme.error
        ) {
            if (wallpaper != null) deleteConfirmationShown = true
        }
    }
    if (deleteConfirmationShown) DeleteConfirmationDialog(
        name = wallpaper?.name.toString(),
        onDismissRequest = { deleteConfirmationShown = false },
        onConfirmDelete = {
            deleteConfirmationShown = false
            if (wallpaper != null) onDeleteRequest(wallpaper)
            scope.launch { state.hide() }
        }
    )
}