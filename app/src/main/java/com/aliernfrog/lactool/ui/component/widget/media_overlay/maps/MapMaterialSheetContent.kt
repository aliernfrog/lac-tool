package com.aliernfrog.lactool.ui.component.widget.media_overlay.maps

import android.content.ClipData
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ContentCopy
import androidx.compose.material.icons.rounded.Delete
import androidx.compose.material3.ElevatedCard
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
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.aliernfrog.lactool.R
import com.aliernfrog.lactool.data.maps.MapMaterialData
import com.aliernfrog.lactool.ui.viewmodel.MapsEditViewModel
import io.github.aliernfrog.shared.ui.component.VerticalSegmentor
import io.github.aliernfrog.shared.ui.component.expressive.ExpressiveButtonRow
import io.github.aliernfrog.shared.ui.component.expressive.ExpressiveRowIcon
import io.github.aliernfrog.shared.ui.dialog.DeleteConfirmationDialog
import kotlinx.coroutines.launch

@Composable
fun MapMaterialSheetContent(
    vm: MapsEditViewModel,
    materialData: MapMaterialData,
) {
    val context = LocalContext.current
    val clipboard = LocalClipboard.current
    val scope = rememberCoroutineScope()

    val material = materialData.material
    val unused = material.usedBy.isEmpty()
    var showDeleteDialog by remember { mutableStateOf(false) }

    Column {
        ElevatedCard(
            modifier = Modifier.padding(start = 12.dp, end = 12.dp, bottom = 8.dp)
        ) {
            Text(
                text = material.url,
                style = MaterialTheme.typography.bodySmall,
                fontFamily = FontFamily.Monospace,
                maxLines = 4,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier
                    .padding(horizontal = 12.dp, vertical = 8.dp)
            )
        }

        VerticalSegmentor(
            {
                ExpressiveButtonRow(
                    title = stringResource(R.string.mapsMaterials_material_copyUrl),
                    icon = {
                        ExpressiveRowIcon(
                            painter = rememberVectorPainter(Icons.Rounded.ContentCopy)
                        )
                    }
                ) { scope.launch {
                    clipboard.setClipEntry(ClipEntry(ClipData.newPlainText(
                        null, material.url
                    )))
                    vm.topToastState.showToast(R.string.info_copiedToClipboard, Icons.Rounded.ContentCopy)
                } }
            },
            {
                ExpressiveButtonRow(
                    title = stringResource(R.string.mapsMaterials_material_delete),
                    description = if (unused) stringResource(R.string.mapsMaterials_unused)
                    else stringResource(R.string.mapsMaterials_material_delete_description)
                        .replace("%n", material.usedBy.size.toString()),
                    contentColor = if (unused) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error,
                    icon = {
                        ExpressiveRowIcon(
                            painter = rememberVectorPainter(Icons.Rounded.Delete),
                            containerColor = if (unused) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error
                        )
                    }
                ) {
                    showDeleteDialog = true
                }
            },
            modifier = Modifier.padding(horizontal = 12.dp)
        )
    }

    if (showDeleteDialog) DeleteConfirmationDialog(
        name = material.name,
        onDismissRequest = { showDeleteDialog = false }
    ) {
        vm.deleteDownloadableMaterial(material, context)
        showDeleteDialog = false
        vm.appState.mediaOverlayData = null
    }
}