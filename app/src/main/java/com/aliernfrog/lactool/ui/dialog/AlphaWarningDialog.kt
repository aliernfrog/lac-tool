package com.aliernfrog.lactool.ui.dialog

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.res.stringResource
import com.aliernfrog.lactool.R
import com.aliernfrog.lactool.ui.viewmodel.MainViewModel
import org.koin.androidx.compose.getViewModel

@Composable
fun AlphaWarningDialog(
    mainViewModel: MainViewModel = getViewModel()
) {
    val currentVersion = remember { mainViewModel.applicationVersionName }
    var shown by remember { mutableStateOf(
        currentVersion.contains("alpha") &&
        mainViewModel.prefs.lastAlphaAck != currentVersion
    ) }

    if (shown) AlertDialog(
        onDismissRequest = { shown = false },
        text = {
            Text(stringResource(R.string.warning_alphaVersion))
        },
        confirmButton = {
            TextButton(
                onClick = {
                    mainViewModel.prefs.lastAlphaAck = currentVersion
                    shown = false
                }
            ) {
                Text(stringResource(R.string.action_ok))
            }
        }
    )
}