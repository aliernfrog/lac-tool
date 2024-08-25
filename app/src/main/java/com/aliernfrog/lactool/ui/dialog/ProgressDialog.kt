package com.aliernfrog.lactool.ui.dialog

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.AlertDialogDefaults
import androidx.compose.material3.BasicAlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.surfaceColorAtElevation
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import com.aliernfrog.lactool.impl.Progress
import com.aliernfrog.lactool.ui.component.HorizontalProgressIndicator

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProgressDialog(
    progress: Progress?,
    onDismissRequest: () -> Unit
) {
    BasicAlertDialog(
        onDismissRequest = onDismissRequest,
        modifier = Modifier
            .clip(AlertDialogDefaults.shape)
            .background(MaterialTheme.colorScheme.surfaceColorAtElevation(AlertDialogDefaults.TonalElevation))
            .padding(
                horizontal = 24.dp,
                vertical = 16.dp
            )
    ) {
        HorizontalProgressIndicator(
            progress = progress,
            textColor = MaterialTheme.colorScheme.onSurface
        )
    }
}