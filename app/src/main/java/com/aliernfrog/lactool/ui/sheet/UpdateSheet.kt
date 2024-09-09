package com.aliernfrog.lactool.ui.sheet

import androidx.compose.animation.AnimatedContent
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.OpenInNew
import androidx.compose.material.icons.filled.Update
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.SheetState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.aliernfrog.lactool.R
import com.aliernfrog.lactool.data.ReleaseInfo
import com.aliernfrog.lactool.ui.component.BaseModalBottomSheet
import com.aliernfrog.lactool.ui.component.ButtonIcon
import com.aliernfrog.lactool.ui.component.form.DividerRow
import dev.jeziellago.compose.markdowntext.MarkdownText
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UpdateSheet(
    sheetState: SheetState,
    latestVersionInfo: ReleaseInfo,
    updateAvailable: Boolean,
    onCheckUpdatesRequest: () -> Unit,
    onIgnoreRequest: () -> Unit
) {
    val scope = rememberCoroutineScope()
    val uriHandler = LocalUriHandler.current
    BaseModalBottomSheet(
        sheetState = sheetState
    ) { bottomPadding ->
        Actions(
            versionName = latestVersionInfo.versionName,
            updateAvailable = updateAvailable,
            onUpdateClick = { uriHandler.openUri(latestVersionInfo.downloadLink) },
            onOpenInBrowserRequest = { uriHandler.openUri(latestVersionInfo.htmlUrl) },
            onCheckUpdatesRequest = onCheckUpdatesRequest,
            onIgnoreRequest = {
                scope.launch {
                    onIgnoreRequest()
                    sheetState.hide()
                }
            }
        )
        DividerRow(
            alpha = 0.3f
        )
        MarkdownText(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(bottom = bottomPadding)
                .padding(16.dp),
            markdown = latestVersionInfo.body,
            linkColor = MaterialTheme.colorScheme.primary,
            style = LocalTextStyle.current.copy(
                color = LocalContentColor.current
            ),
            onLinkClicked = {
                uriHandler.openUri(it)
            }
        )
    }
}

@Composable
private fun Actions(
    versionName: String,
    updateAvailable: Boolean,
    onUpdateClick: () -> Unit,
    onOpenInBrowserRequest: () -> Unit,
    onCheckUpdatesRequest: () -> Unit,
    onIgnoreRequest: () -> Unit
) {
    var ignoreDialogShown by remember { mutableStateOf(false) }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 16.dp, end = 16.dp, bottom = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.clickable(onClick = onOpenInBrowserRequest)
        ) {
            Text(
                text = versionName,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.SemiBold
            )
            Icon(
                imageVector = Icons.AutoMirrored.Filled.OpenInNew,
                contentDescription = stringResource(R.string.action_openInBrowser)
            )
        }
        Spacer(
            modifier = Modifier.weight(1f).fillMaxWidth()
        )
        AnimatedContent(updateAvailable) { showUpdate ->
            if (showUpdate) Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedButton(
                    onClick = { ignoreDialogShown = true }
                ) {
                    Text(stringResource(R.string.updates_ignore))
                }
                Button(
                    onClick = onUpdateClick
                ) {
                    ButtonIcon(
                        painter = rememberVectorPainter(Icons.Default.Update)
                    )
                    Text(stringResource(R.string.updates_update))
                }
            } else OutlinedButton(
                onClick = onCheckUpdatesRequest
            ) {
                ButtonIcon(
                    painter = rememberVectorPainter(Icons.Default.Update)
                )
                Text(stringResource(R.string.updates_checkUpdates))
            }
        }
    }

    if (ignoreDialogShown) AlertDialog(
        onDismissRequest = { ignoreDialogShown = false },
        confirmButton = {
            Button(
                onClick = {
                    onIgnoreRequest()
                    ignoreDialogShown = false
                }
            ) {
                Text(stringResource(R.string.updates_ignore_ignore))
            }
        },
        dismissButton = {
            TextButton(onClick = { ignoreDialogShown = false }) {
                Text(stringResource(R.string.action_cancel))
            }
        },
        text = {
            Text(stringResource(R.string.updates_ignore_question))
        }
    )
}