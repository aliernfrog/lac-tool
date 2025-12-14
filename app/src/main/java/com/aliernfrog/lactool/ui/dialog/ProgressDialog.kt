package com.aliernfrog.lactool.ui.dialog

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.AlertDialogDefaults
import androidx.compose.material3.BasicAlertDialog
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.surfaceColorAtElevation
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.aliernfrog.lactool.R
import com.aliernfrog.lactool.impl.Progress
import com.aliernfrog.lactool.ui.component.HorizontalProgressIndicatorWithText
import kotlinx.coroutines.delay

@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun ProgressDialog(
    progress: Progress?,
    onDismissRequest: () -> Unit
) {
    var showDismissButton by remember { mutableStateOf(false) }
    val isIndeterminate = progress?.percentage == null || progress.finished

    fun dismissDialog() {
        if (isIndeterminate) onDismissRequest()
    }

    LaunchedEffect(isIndeterminate) {
        if (!isIndeterminate) return@LaunchedEffect
        delay(10_000)
        showDismissButton = true
    }

    BasicAlertDialog(
        onDismissRequest = {},
        modifier = Modifier
            .clip(AlertDialogDefaults.shape)
            .background(MaterialTheme.colorScheme.surfaceColorAtElevation(AlertDialogDefaults.TonalElevation))
            .padding(
                horizontal = 24.dp,
                vertical = 16.dp
            )
    ) {
        Column(Modifier.animateContentSize()) {
            HorizontalProgressIndicatorWithText(
                progress = progress,
                textColor = MaterialTheme.colorScheme.onSurface
            )
            if (showDismissButton) TextButton(
                onClick = ::dismissDialog,
                shapes = ButtonDefaults.shapes(),
                colors = ButtonDefaults.textButtonColors(
                    contentColor = MaterialTheme.colorScheme.error
                ),
                modifier = Modifier.align(Alignment.End)
            ) {
                Text(stringResource(R.string.action_cancel))
            }
        }
    }
}