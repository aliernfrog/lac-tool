package com.aliernfrog.lactool.ui.dialog

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.AlertDialogDefaults
import androidx.compose.material3.BasicAlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.surfaceColorAtElevation
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.aliernfrog.lactool.R
import com.aliernfrog.lactool.impl.Progress

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
        Row(
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (progress?.percentage == null || progress.finished) CircularProgressIndicator()
            else progress.percentage.toFloat().let {
                val animated by animateFloatAsState(it/100)
                CircularProgressIndicator(
                    progress = { animated }
                )
            }
            Text(
                text = progress?.description ?: stringResource(R.string.info_pleaseWait),
                color = MaterialTheme.colorScheme.onSurface
            )
        }
    }
}