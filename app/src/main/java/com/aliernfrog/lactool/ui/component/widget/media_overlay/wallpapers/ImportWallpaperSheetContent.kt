package com.aliernfrog.lactool.ui.component.widget.media_overlay.wallpapers

import androidx.compose.animation.Crossfade
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Restore
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.aliernfrog.lactool.R
import com.aliernfrog.lactool.ui.viewmodel.WallpapersViewModel
import io.github.aliernfrog.pftool_shared.impl.FileWrapper
import io.github.aliernfrog.shared.ui.component.ButtonIcon
import io.github.aliernfrog.shared.ui.component.IconButtonWithTooltip
import kotlinx.coroutines.launch
import org.koin.androidx.compose.koinViewModel
import kotlin.text.ifEmpty

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun ImportWallpaperSheetContent(
    file: FileWrapper,
    vm: WallpapersViewModel = koinViewModel(),
    onDismissMediaOverlayRequest: () -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val originalName = remember { file.nameWithoutExtension }
    var importName by remember { mutableStateOf(originalName) }

    OutlinedTextField(
        value = importName,
        onValueChange = { importName = it },
        label = {
            Text(stringResource(R.string.wallpapers_chosen_name))
        },
        placeholder = {
            Text(originalName)
        },
        trailingIcon = {
            Crossfade(importName != originalName) { enabled ->
                IconButtonWithTooltip(
                    icon = rememberVectorPainter(Icons.Default.Restore),
                    contentDescription = stringResource(R.string.wallpapers_chosen_name_reset),
                    onClick = { importName = originalName },
                    enabled = enabled
                )
            }
        },
        singleLine = true,
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp)
    )

    Row(
        horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.End),
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 12.dp, end = 12.dp, top = 8.dp)
    ) {
        Button(
            shapes = ButtonDefaults.shapes(),
            onClick = { scope.launch {
                vm.importWallpaper(
                    file = file,
                    withName = importName.ifEmpty { originalName },
                    context = context
                )
                onDismissMediaOverlayRequest()
            } }
        ) {
            ButtonIcon(rememberVectorPainter(Icons.Default.Download))
            Text(stringResource(R.string.wallpapers_chosen_import))
        }
    }
}