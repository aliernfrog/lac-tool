package com.aliernfrog.lactool.ui.composable

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.aliernfrog.lactool.LACToolComposableShape

@Composable
fun LACToolDialog(
    onDismissRequest: () -> Unit,
    title: String? = null,
    icon: Painter? = null,
    content: @Composable () -> Unit
) {
    Dialog(onDismissRequest = onDismissRequest) {
        Column(
            modifier = Modifier
                .clip(LACToolComposableShape)
                .background(AlertDialogDefaults.containerColor)
                .padding(24.dp)
        ) {
            if (icon != null) Icon(
                painter = icon,
                contentDescription = null,
                modifier = Modifier.padding(bottom = 16.dp).fillMaxWidth().align(Alignment.CenterHorizontally),
                tint = AlertDialogDefaults.iconContentColor
            )
            if (title != null) Text(
                text = title,
                modifier = Modifier.padding(bottom = 16.dp).fillMaxWidth(),
                textAlign = if (icon != null) TextAlign.Center else TextAlign.Start,
                style = MaterialTheme.typography.headlineSmall,
                color = AlertDialogDefaults.titleContentColor
            )
            content()
        }
    }
}