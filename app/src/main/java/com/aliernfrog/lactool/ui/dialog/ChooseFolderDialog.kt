package com.aliernfrog.lactool.ui.dialog

import androidx.compose.animation.Crossfade
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.aliernfrog.lactool.R
import com.aliernfrog.lactool.data.PermissionData
import com.aliernfrog.lactool.externalStorageRoot
import com.aliernfrog.lactool.folderPickerSupportsInitialUri

@Composable
fun ChooseFolderIntroDialog(
    permissionData: PermissionData,
    onDismissRequest: () -> Unit,
    onConfirm: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismissRequest,
        confirmButton = {
            Button(
                onClick = onConfirm
            ) {
                Text(stringResource(R.string.action_ok))
            }
        },
        dismissButton = {
            TextButton(
                onClick = onDismissRequest
            ) {
                Text(stringResource(R.string.action_cancel))
            }
        },
        title = {
            Text(stringResource(R.string.permissions_recommendedFolder))
        },
        text = {
            Column(
                modifier = Modifier.verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(stringResource(permissionData.recommendedPathDescriptionId!!))

                PathCard(permissionData.recommendedPath!!)

                permissionData.doesntExistHintId?.let {
                    FolderDoesntExistHint(stringResource(it))
                }

                Text(
                    text = stringResource(
                        //? Folder picker on Android 7 or below doesn't support automatically navigating
                        if (folderPickerSupportsInitialUri) R.string.permissions_recommendedFolder_a8Hint
                        else R.string.permissions_recommendedFolder_a7Hint
                    ),
                    fontSize = 13.5.sp
                )
            }
        }
    )
}

@Composable
fun NotRecommendedFolderDialog(
    permissionData: PermissionData,
    onDismissRequest: () -> Unit,
    onUseUnrecommendedFolderRequest: () -> Unit,
    onChooseFolderRequest: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismissRequest,
        confirmButton = {
            Button(
                onClick = onChooseFolderRequest
            ) {
                Text(stringResource(R.string.permissions_notRecommendedFolder_chooseRecommendedFolder))
            }
        },
        dismissButton = {
            TextButton(
                onClick = onUseUnrecommendedFolderRequest
            ) {
                Text(stringResource(R.string.permissions_notRecommendedFolder_useUnrecommendedFolder))
            }
        },
        text = {
            Column(
                modifier = Modifier.verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(stringResource(R.string.permissions_notRecommendedFolder_description))

                PathCard(permissionData.recommendedPath!!)

                permissionData.doesntExistHintId?.let {
                    FolderDoesntExistHint(stringResource(it))
                }

                Text(stringResource(R.string.permissions_notRecommendedFolder_question))
            }
        }
    )
}

@Composable
private fun PathCard(path: String) {
    Card(
        modifier = Modifier.fillMaxWidth()
    ) {
        Text(
            text = path.removePrefix(externalStorageRoot),
            modifier = Modifier.padding(4.dp)
        )
    }
}

@Composable
private fun FolderDoesntExistHint(
    hint: String
) {
    var showMoreInfo by rememberSaveable { mutableStateOf(false) }
    Crossfade(showMoreInfo) { moreInfoShown ->
        if (!moreInfoShown) Text(
            text = stringResource(R.string.permissions_recommendedFolder_doesNotExist),
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier
                .fillMaxWidth()
                .clickable { showMoreInfo = true }
                .padding(vertical = 4.dp)
        )
        else Text(
            text = hint,
            fontSize = 13.5.sp,
            modifier = Modifier.alpha(0.8f)
        )
    }
}