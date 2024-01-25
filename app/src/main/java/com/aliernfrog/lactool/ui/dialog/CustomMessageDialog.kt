package com.aliernfrog.lactool.ui.dialog

import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import com.aliernfrog.lactool.R

@Composable
fun CustomMessageDialog(
    title: String,
    text: String,
    icon: ImageVector? = null,
    onDismissRequest: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismissRequest,
        confirmButton = {
            TextButton(onClick = onDismissRequest) {
                Text(stringResource(R.string.action_dismiss))
            }
        },
        title = {
            Text(title)
        },
        text = {
            Text(
                text = text,
                modifier = Modifier.verticalScroll(rememberScrollState())
            )
        },
        icon = icon?.let { {
            Icon(
                imageVector = it,
                contentDescription = null
            )
        } }
    )
}