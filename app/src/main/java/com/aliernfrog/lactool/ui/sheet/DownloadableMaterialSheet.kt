package com.aliernfrog.lactool.ui.sheet

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.ModalBottomSheetState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ContentCopy
import androidx.compose.material.icons.rounded.Delete
import androidx.compose.material.icons.rounded.Report
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
import com.aliernfrog.laclib.data.LACMapDownloadableMaterial
import com.aliernfrog.lactool.R
import com.aliernfrog.lactool.ui.component.AppModalBottomSheet
import com.aliernfrog.lactool.ui.component.ButtonShapeless
import com.aliernfrog.lactool.ui.component.ErrorWithIcon
import com.aliernfrog.toptoast.state.TopToastState
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun DownloadableMaterialSheet(
    material: LACMapDownloadableMaterial?,
    failed: Boolean,
    state: ModalBottomSheetState,
    topToastState: TopToastState?,
    onDeleteRequest: (LACMapDownloadableMaterial) -> Unit,
    onError: () -> Unit
) {
    val clipboardManager = LocalClipboardManager.current
    val scope = rememberCoroutineScope()
    AppModalBottomSheet(sheetState = state) {
        Text(
            text = material?.url.toString(),
            modifier = Modifier.padding(top = 8.dp).padding(horizontal = 8.dp),
            color = MaterialTheme.colorScheme.onSurface,
            fontFamily = FontFamily.Monospace
        )
        AsyncImage(
            model = material?.url,
            contentDescription = null,
            modifier = Modifier.fillMaxWidth().padding(8.dp),
            contentScale = ContentScale.Crop,
            onError = { onError() }
        )
        AnimatedVisibility(
            visible = failed,
            exit = fadeOut(tween(0))
        ) {
            ErrorWithIcon(
                error = stringResource(R.string.mapsMaterials_material_failed),
                painter = rememberVectorPainter(Icons.Rounded.Report),
                contentColor = MaterialTheme.colorScheme.error
            )
        }
        Divider(
            modifier = Modifier.padding(8.dp).alpha(0.7f),
            thickness = 1.dp,
            color = MaterialTheme.colorScheme.surfaceVariant
        )
        ButtonShapeless(
            title = stringResource(R.string.mapsMaterials_material_copyUrl),
            painter = rememberVectorPainter(Icons.Rounded.ContentCopy)
        ) {
            clipboardManager.setText(AnnotatedString(material?.url.toString()))
            topToastState?.showToast(R.string.info_copiedToClipboard, Icons.Rounded.ContentCopy)
            scope.launch { state.hide() }
        }
        ButtonShapeless(
            title = stringResource(R.string.mapsMaterials_material_delete),
            description = stringResource(R.string.mapsMaterials_material_delete_description).replace("%n", material?.usedBy?.size.toString()),
            painter = rememberVectorPainter(Icons.Rounded.Delete),
            contentColor = MaterialTheme.colorScheme.error
        ) {
            if (material != null) onDeleteRequest(material)
            scope.launch { state.hide() }
        }
    }
}