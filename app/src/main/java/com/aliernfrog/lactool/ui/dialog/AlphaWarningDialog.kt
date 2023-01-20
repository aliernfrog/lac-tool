package com.aliernfrog.lactool.ui.dialog

import android.content.SharedPreferences
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import com.aliernfrog.lactool.ConfigKey
import com.aliernfrog.lactool.R
import com.aliernfrog.lactool.util.staticutil.GeneralUtil

@Composable
fun AlphaWarningDialog(config: SharedPreferences) {
    val context = LocalContext.current
    val currentVersion = remember { GeneralUtil.getAppVersionName(context) }
    var shown by remember { mutableStateOf(
        currentVersion.contains("alpha") &&
        config.getString(ConfigKey.KEY_APP_LAST_ALPHA_ACK, "") != currentVersion
    ) }

    if (shown) AlertDialog(
        onDismissRequest = { shown = false },
        text = {
            Text(stringResource(R.string.warning_alphaVersion))
        },
        confirmButton = {
            Button(
                onClick = {
                    config.edit().putString(ConfigKey.KEY_APP_LAST_ALPHA_ACK, currentVersion).apply()
                    shown = false
                }
            ) {
                Text(stringResource(R.string.action_ok))
            }
        }
    )
}