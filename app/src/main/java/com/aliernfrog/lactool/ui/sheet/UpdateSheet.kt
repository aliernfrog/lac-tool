package com.aliernfrog.lactool.ui.sheet

import androidx.compose.animation.AnimatedContent
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Update
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.SheetState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import com.aliernfrog.lactool.R
import com.aliernfrog.lactool.data.ReleaseInfo
import com.aliernfrog.lactool.ui.component.BaseModalBottomSheet
import com.aliernfrog.lactool.ui.component.ButtonIcon
import com.aliernfrog.lactool.ui.component.form.DividerRow
import com.aliernfrog.lactool.util.extension.horizontalFadingEdge
import dev.jeziellago.compose.markdowntext.MarkdownText

@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun UpdateSheet(
    sheetState: SheetState,
    latestVersionInfo: ReleaseInfo,
    updateAvailable: Boolean,
    onCheckUpdatesRequest: () -> Unit
) {
    val uriHandler = LocalUriHandler.current
    BaseModalBottomSheet(
        sheetState = sheetState
    ) { bottomPadding ->
        Actions(
            versionName = latestVersionInfo.versionName,
            preRelease = latestVersionInfo.preRelease,
            updateAvailable = updateAvailable,
            onUpdateClick = { uriHandler.openUri(latestVersionInfo.downloadLink) },
            onCheckUpdatesRequest = onCheckUpdatesRequest
        )
        DividerRow(
            alpha = 0.3f
        )
        Column(Modifier
            .fillMaxWidth()
            .verticalScroll(rememberScrollState())
            .padding(bottom = bottomPadding)
        ) {
            MarkdownText(
                modifier = Modifier
                    .padding(top = 8.dp)
                    .padding(horizontal = 16.dp),
                markdown = latestVersionInfo.body,
                linkColor = MaterialTheme.colorScheme.primary,
                syntaxHighlightColor = MaterialTheme.colorScheme.surfaceContainerHighest,
                syntaxHighlightTextColor = MaterialTheme.colorScheme.onSurface,
                style = LocalTextStyle.current.copy(
                    color = LocalContentColor.current
                ),
                onLinkClicked = {
                    uriHandler.openUri(it)
                }
            )
            TextButton(
                onClick = { uriHandler.openUri(latestVersionInfo.htmlUrl) },
                shapes = ButtonDefaults.shapes(),
                modifier = Modifier
                    .padding(horizontal = 16.dp)
                    .align(Alignment.End)
            ) {
                ButtonIcon(
                    painter = painterResource(R.drawable.github)
                )
                Text(
                    text = stringResource(R.string.updates_openInGithub)
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
private fun Actions(
    versionName: String,
    preRelease: Boolean,
    updateAvailable: Boolean,
    onUpdateClick: () -> Unit,
    onCheckUpdatesRequest: () -> Unit
) {
    val versionNameScrollState = rememberScrollState()
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 16.dp, end = 16.dp, bottom = 4.dp),
        horizontalArrangement = Arrangement.spacedBy(2.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .padding(end = 12.dp)
        ) {
            Row(
                modifier = Modifier
                    .horizontalFadingEdge(
                        scrollState = versionNameScrollState,
                        edgeColor = MaterialTheme.colorScheme.surfaceContainerLow,
                        isRTL = LocalLayoutDirection.current == LayoutDirection.Rtl
                    )
                    .horizontalScroll(versionNameScrollState),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = versionName,
                    style = MaterialTheme.typography.titleLarge
                )
            }
            Text(
                text = stringResource(
                    if (preRelease) R.string.updates_preRelease
                    else R.string.updates_stable
                ),
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Light,
                color = LocalContentColor.current.copy(alpha = 0.7f)
            )
        }
        AnimatedContent(
            targetState = updateAvailable
        ) { showUpdate ->
            if (showUpdate) Button(
                onClick = onUpdateClick,
                shapes = ButtonDefaults.shapes()
            ) {
                ButtonIcon(
                    painter = rememberVectorPainter(Icons.Default.Update)
                )
                Text(stringResource(R.string.updates_update))
            }
            else OutlinedButton(
                onClick = onCheckUpdatesRequest,
                shapes = ButtonDefaults.shapes()
            ) {
                ButtonIcon(
                    painter = rememberVectorPainter(Icons.Default.Refresh)
                )
                Text(stringResource(R.string.updates_checkUpdates))
            }
        }
    }
}