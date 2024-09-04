package com.aliernfrog.lactool.ui.sheet

import androidx.compose.animation.AnimatedContent
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Update
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
import androidx.compose.ui.unit.sp
import com.aliernfrog.lactool.R
import com.aliernfrog.lactool.data.ReleaseInfo
import com.aliernfrog.lactool.ui.component.BaseModalBottomSheet
import com.aliernfrog.lactool.ui.component.ButtonIcon
import com.aliernfrog.lactool.ui.component.form.DividerRow
import com.aliernfrog.lactool.util.extension.horizontalFadingEdge
import dev.jeziellago.compose.markdowntext.MarkdownText

@OptIn(ExperimentalMaterial3Api::class)
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
            onGithubClick = { uriHandler.openUri(latestVersionInfo.htmlUrl) },
            onUpdateClick = { uriHandler.openUri(latestVersionInfo.downloadLink) },
            onCheckUpdatesRequest = onCheckUpdatesRequest
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
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
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
                modifier = Modifier.padding(horizontal = 6.dp)
            )
        }
        IconButton(onClick = onGithubClick) {
            Icon(
                painter = painterResource(R.drawable.github),
                contentDescription = stringResource(R.string.updates_openInGithub),
                modifier = Modifier.padding(6.dp)
            )
        }
        AnimatedContent(updateAvailable) { showUpdate ->
            if (showUpdate) Button(
                onClick = onUpdateClick
            ) {
                ButtonIcon(
                    painter = rememberVectorPainter(Icons.Default.Update)
                )
                Text(stringResource(R.string.updates_update))
            }
            else OutlinedButton(
                onClick = onCheckUpdatesRequest
            ) {
                ButtonIcon(
                    painter = rememberVectorPainter(Icons.Default.Update)
                )
                Text(stringResource(R.string.updates_checkUpdates))
            }
        }
    }
}