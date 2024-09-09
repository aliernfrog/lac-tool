package com.aliernfrog.lactool.ui.sheet

import androidx.compose.animation.AnimatedContent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Update
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.aliernfrog.lactool.R
import com.aliernfrog.lactool.data.ReleaseInfo
import com.aliernfrog.lactool.ui.component.BaseModalBottomSheet
import com.aliernfrog.lactool.ui.component.ButtonIcon
import com.aliernfrog.lactool.ui.component.form.DividerRow
import dev.jeziellago.compose.markdowntext.MarkdownText

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UpdateSheet(
    sheetState: SheetState,
    latestVersionInfo: ReleaseInfo,
    updateAvailable: Boolean,
    onCheckUpdatesRequest: () -> Unit,
    onIgnoreRequest: () -> Unit
) {
    val uriHandler = LocalUriHandler.current
    BaseModalBottomSheet(
        sheetState = sheetState
    ) { bottomPadding ->
        Actions(
            versionName = latestVersionInfo.versionName,
            preRelease = latestVersionInfo.preRelease,
            updateAvailable = updateAvailable,
            onGithubClick = { uriHandler.openUri(latestVersionInfo.htmlUrl) },
            onUpdateClick = { uriHandler.openUri(latestVersionInfo.downloadLink) },
            onCheckUpdatesRequest = onCheckUpdatesRequest,
            onIgnoreRequest = onIgnoreRequest
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
    preRelease: Boolean,
    updateAvailable: Boolean,
    onGithubClick: () -> Unit,
    onUpdateClick: () -> Unit,
    onCheckUpdatesRequest: () -> Unit,
    onIgnoreRequest: () -> Unit
) {
    var ignoreDialogShown by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 16.dp, end = 16.dp, bottom = 4.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = versionName,
                fontSize = 25.sp,
                fontWeight = FontWeight.SemiBold
            )
            Text(
                text = stringResource(
                    if (preRelease) R.string.updates_preRelease
                    else R.string.updates_stable
                ),
                fontSize = 15.sp,
                fontWeight = FontWeight.Light,
                color = LocalContentColor.current.copy(alpha = 0.7f),
                modifier = Modifier
                    .weight(1f)
                    .padding(horizontal = 6.dp)
                    .fillMaxWidth()
            )
            IconButton(onClick = onGithubClick) {
                Icon(
                    painter = painterResource(R.drawable.github),
                    contentDescription = stringResource(R.string.updates_openInGithub),
                    modifier = Modifier.size(28.dp)
                )
            }
        }
        AnimatedContent(updateAvailable) { showUpdate ->
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.End),
                modifier = Modifier.fillMaxWidth()
            ) {
                if (showUpdate) {
                    OutlinedButton(
                        onClick = { ignoreDialogShown = true },
                        modifier = Modifier.weight(1f).fillMaxWidth()
                    ) {
                        Text(stringResource(R.string.updates_ignore))
                    }
                    Button(
                        onClick = onUpdateClick,
                        modifier = Modifier.weight(1f).fillMaxWidth()
                    ) {
                        ButtonIcon(
                            painter = rememberVectorPainter(Icons.Default.Update)
                        )
                        Text(stringResource(R.string.updates_update))
                    }
                } else OutlinedButton(
                    onClick = onCheckUpdatesRequest,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    ButtonIcon(
                        painter = rememberVectorPainter(Icons.Default.Update)
                    )
                    Text(stringResource(R.string.updates_checkUpdates))
                }
            }
        }
    }

    if (ignoreDialogShown) AlertDialog(
        onDismissRequest = { ignoreDialogShown = false },
        confirmButton = {
            Button(onClick = onIgnoreRequest) {
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