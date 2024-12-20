package com.aliernfrog.lactool.ui.screen.permissions

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.aliernfrog.lactool.R
import com.aliernfrog.lactool.enum.StorageAccessType
import com.aliernfrog.lactool.ui.component.CardWithActions
import com.aliernfrog.lactool.ui.component.form.ButtonRow
import com.aliernfrog.lactool.ui.component.form.FormSection
import com.aliernfrog.lactool.ui.viewmodel.PermissionsViewModel
import com.aliernfrog.toptoast.enum.TopToastColor
import org.koin.androidx.compose.koinViewModel

@Composable
fun AllFilesPermissionsScreen(
    permissionsViewModel: PermissionsViewModel = koinViewModel(),
    onUpdateStateRequest: () -> Unit
) {
    val permissionLauncher = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) {
        if (it) onUpdateStateRequest()
        else permissionsViewModel.topToastState.showToast(
            text = R.string.permissions_allFiles_denied,
            icon = Icons.Default.Close,
            iconTintColor = TopToastColor.ERROR
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .navigationBarsPadding()
    ) {
        CardWithActions(
            title = stringResource(R.string.permissions_allFiles_title),
            buttons = {
                Button(
                    onClick = {
                        permissionLauncher.launch(android.Manifest.permission.READ_EXTERNAL_STORAGE)
                    }
                ) {
                    Text(stringResource(R.string.permissions_allFiles_grant))
                }
            },
            modifier = Modifier.fillMaxWidth().padding(8.dp)
        ) {
            Text(stringResource(R.string.permissions_allFiles_description))
        }

        FormSection(
            title = stringResource(R.string.permissions_other),
            topDivider = true,
            bottomDivider = false
        ) {
            ButtonRow(
                title = stringResource(R.string.permissions_allFiles_saf),
                description = stringResource(R.string.permissions_allFiles_saf_description)
            ) {
                StorageAccessType.SAF.enable(permissionsViewModel.prefs)
            }
        }
    }
}