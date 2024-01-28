package com.aliernfrog.lactool.ui.dialog

import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.CloudOff
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import com.aliernfrog.lactool.R
import com.aliernfrog.lactool.util.staticutil.GeneralUtil

@Composable
fun MaterialsNoConnectionDialog() {
    val context = LocalContext.current
    var shown by remember {
        mutableStateOf(!GeneralUtil.isConnectedToInternet(context))
    }
    if (shown) AlertDialog(
        onDismissRequest = { shown = false },
        icon = {
            Icon(
                painter = rememberVectorPainter(Icons.Rounded.CloudOff),
                contentDescription = null
            )
        },
        title = {
            Text(stringResource(R.string.mapsMaterials_noConnection))
        },
        text = {
            Text(
                text = stringResource(R.string.mapsMaterials_noConnection_description),
                modifier = Modifier.verticalScroll(rememberScrollState())
            )
        },
        confirmButton = {
            TextButton(
                onClick = { shown = false }
            ) {
                Text(stringResource(R.string.action_ok))
            }
        }
    )
}