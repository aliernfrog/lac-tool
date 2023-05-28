package com.aliernfrog.lactool.ui.sheet

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.ModalBottomSheetState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Update
import androidx.compose.material3.Button
import androidx.compose.material3.Divider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
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
import com.aliernfrog.lactool.util.extension.horizontalFadingEdge
import dev.jeziellago.compose.markdowntext.MarkdownText

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun UpdateSheet(
    sheetState: ModalBottomSheetState,
    latestVersionInfo: ReleaseInfo
) {
    val uriHandler = LocalUriHandler.current
    BaseModalBottomSheet(
        sheetState = sheetState
    ) {
        Actions(
            versionName = latestVersionInfo.versionName,
            preRelease = latestVersionInfo.preRelease,
            onGithubClick = { uriHandler.openUri(latestVersionInfo.htmlUrl) },
            onUpdateClick = { uriHandler.openUri(latestVersionInfo.downloadLink) }
        )
        Divider(
            modifier = Modifier.fillMaxWidth(),
            thickness = 1.dp,
            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
        )
        MarkdownText(
            modifier = Modifier
                .verticalScroll(rememberScrollState())
                .navigationBarsPadding()
                .padding(16.dp),
            markdown = latestVersionInfo.body,
            color = LocalContentColor.current,
            style = LocalTextStyle.current,
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
    onGithubClick: () -> Unit,
    onUpdateClick: () -> Unit,
) {
    val versionNameScrollState = rememberScrollState()
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 16.dp, end = 16.dp, bottom = 4.dp),
        horizontalArrangement = Arrangement.spacedBy(4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            Modifier
                .fillMaxWidth()
                .weight(1f)
                .horizontalFadingEdge(versionNameScrollState, MaterialTheme.colorScheme.surface)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
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
        }
        IconButton(onClick = onGithubClick) {
            Icon(
                painter = painterResource(R.drawable.github),
                contentDescription = stringResource(R.string.updates_openInGithub)
            )
        }
        Button(
            onClick = onUpdateClick
        ) {
            Icon(
                painter = rememberVectorPainter(Icons.Rounded.Update),
                contentDescription = null,
                modifier = Modifier.padding(end = 4.dp)
            )
            Text(stringResource(R.string.updates_update))
        }
    }
}