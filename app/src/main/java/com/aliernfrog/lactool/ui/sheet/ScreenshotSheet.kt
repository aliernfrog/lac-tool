package com.aliernfrog.lactool.ui.sheet

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Delete
import androidx.compose.material.icons.rounded.IosShare
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SheetState
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.aliernfrog.lactool.R
import com.aliernfrog.lactool.data.ImageFile
import com.aliernfrog.lactool.ui.component.AppModalBottomSheet
import com.aliernfrog.lactool.ui.component.form.ButtonRow
import com.aliernfrog.lactool.ui.dialog.DeleteConfirmationDialog
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ScreenshotsSheet(
    screenshot: ImageFile?,
    state: SheetState,
    onShareRequest: (ImageFile) -> Unit,
    onDeleteRequest: (ImageFile) -> Unit
) {
    val scope = rememberCoroutineScope()
    var deleteConfirmationShown by remember { mutableStateOf(false) }
    AppModalBottomSheet(sheetState = state) {
        Text(
            text = screenshot?.fileName.toString(),
            modifier = Modifier.padding(top = 8.dp).padding(horizontal = 8.dp),
            color = MaterialTheme.colorScheme.onSurface,
            fontFamily = FontFamily.Monospace
        )
        AsyncImage(
            model = screenshot?.painterModel,
            contentDescription = null,
            modifier = Modifier.fillMaxWidth().padding(8.dp),
            contentScale = ContentScale.Crop
        )
        HorizontalDivider(
            modifier = Modifier.padding(8.dp).alpha(0.7f),
            thickness = 1.dp,
            color = MaterialTheme.colorScheme.surfaceVariant
        )
        ButtonRow(
            title = stringResource(R.string.screenshots_share),
            painter = rememberVectorPainter(Icons.Rounded.IosShare)
        ) {
            if (screenshot != null) onShareRequest(screenshot)
        }
        ButtonRow(
            title = stringResource(R.string.screenshots_delete),
            painter = rememberVectorPainter(Icons.Rounded.Delete),
            contentColor = MaterialTheme.colorScheme.error
        ) {
            if (screenshot != null) deleteConfirmationShown = true
        }
    }

    if (deleteConfirmationShown) DeleteConfirmationDialog(
        name = screenshot?.name.toString(),
        onDismissRequest = { deleteConfirmationShown = false },
        onConfirmDelete = {
            deleteConfirmationShown = false
            if (screenshot != null) onDeleteRequest(screenshot)
            scope.launch { state.hide() }
        }
    )
}