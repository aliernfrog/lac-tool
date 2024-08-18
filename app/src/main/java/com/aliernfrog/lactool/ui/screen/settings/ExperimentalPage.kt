package com.aliernfrog.lactool.ui.screen.settings

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Restore
import androidx.compose.material.icons.rounded.Done
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.aliernfrog.lactool.R
import com.aliernfrog.lactool.SettingsConstant
import com.aliernfrog.lactool.ui.component.form.ButtonRow
import com.aliernfrog.lactool.ui.component.form.FormSection
import com.aliernfrog.lactool.ui.component.form.SwitchRow
import com.aliernfrog.lactool.ui.theme.AppComponentShape
import com.aliernfrog.lactool.ui.viewmodel.MainViewModel
import com.aliernfrog.lactool.util.manager.base.BasePreferenceManager
import com.aliernfrog.lactool.util.staticutil.GeneralUtil
import kotlinx.coroutines.launch
import org.koin.androidx.compose.koinViewModel

@Suppress("UNCHECKED_CAST")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExperimentalPage(
    mainViewModel: MainViewModel = koinViewModel(),
    onNavigateBackRequest: () -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    SettingsPageContainer(
        title = stringResource(R.string.settings_experimental),
        onNavigateBackRequest = onNavigateBackRequest
    ) {
        SwitchRow(
            title = stringResource(R.string.settings_experimental),
            description = stringResource(R.string.settings_experimental_description),
            checked = mainViewModel.prefs.experimentalOptionsEnabled.value,
            shape = AppComponentShape,
            containerColor = MaterialTheme.colorScheme.primaryContainer,
            contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
            modifier = Modifier.padding(16.dp)
        ) {
            mainViewModel.prefs.experimentalOptionsEnabled.value = it
        }

        FormSection(title = "Updates") {
            ButtonRow(
                title = "Check updates (ignore version)"
            ) {
                scope.launch {
                    mainViewModel.checkUpdates(ignoreVersion = true)
                }
            }
            ButtonRow(
                title = "Show update toast"
            ) {
                mainViewModel.showUpdateToast()
            }
            ButtonRow(
                title = "Show update dialog"
            ) {
                scope.launch {
                    mainViewModel.updateSheetState.show()
                }
            }
        }

        FormSection(title = "Prefs", bottomDivider = false) {
            SettingsConstant.experimentalPrefOptions.forEach { prefEditItem ->
                val pref = prefEditItem.preference(mainViewModel.prefs)
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    IconButton(
                        onClick = { pref.resetValue() },
                        modifier = Modifier.padding(start = 8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Restore,
                            contentDescription = "Reset"
                        )
                    }
                    when (pref.defaultValue) {
                        is Boolean -> {
                            pref as BasePreferenceManager.Preference<Boolean>
                            SwitchRow(
                                title = pref.key,
                                checked = pref.value
                            ) {
                                pref.value = it
                            }
                        }
                        is String -> {
                            pref as BasePreferenceManager.Preference<String>
                            OutlinedTextField(
                                value = pref.value,
                                onValueChange = { pref.value = it },
                                label = { Text(pref.key) },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 16.dp, vertical = 8.dp)
                            )
                        }
                    }
                }
            }

            ButtonRow(
                title = "Reset experimental prefs",
                contentColor = MaterialTheme.colorScheme.error
            ) {
                scope.launch {
                    SettingsConstant.experimentalPrefOptions.forEach {
                        it.preference(mainViewModel.prefs).resetValue()
                    }
                    mainViewModel.topToastState.showAndroidToast(
                        text = "Restored default values for experimental prefs",
                        icon = Icons.Rounded.Done
                    )
                    GeneralUtil.restartApp(context)
                }
            }
        }
    }
}