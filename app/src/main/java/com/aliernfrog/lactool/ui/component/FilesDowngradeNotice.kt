package com.aliernfrog.lactool.ui.component

import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.res.stringResource
import com.aliernfrog.lactool.R
import dev.jeziellago.compose.markdowntext.MarkdownText

@Composable
fun FilesDowngradeNotice(
    modifier: Modifier = Modifier
) {
    val uriHandler = LocalUriHandler.current
    var showMoreInfoDialog by remember { mutableStateOf(false) }
    val onDismissMoreInfoDialogRequest = { showMoreInfoDialog = false }

    CardWithActions(
        modifier = modifier,
        title = stringResource(R.string.permissions_filesApp),
        buttons = {
            TextButton(
                onClick = { showMoreInfoDialog = true }
            ) {
                Text(stringResource(R.string.permissions_filesApp_moreInfo))
            }
        }
    ) {
        Text(stringResource(R.string.permissions_filesApp_description))
    }

    if (showMoreInfoDialog) AlertDialog(
        onDismissRequest = onDismissMoreInfoDialogRequest,
        confirmButton = {
            TextButton(
                onClick = onDismissMoreInfoDialogRequest
            ) {
                Text(stringResource(R.string.action_dismiss))
            }
        },
        text = {
            MarkdownText(
                modifier = Modifier.verticalScroll(rememberScrollState()),
                markdown = stringResource(R.string.permissions_filesApp_moreInfo_text)
                    .replace("{MORE_INFO_URL}", "https://aliernfrog.github.io/android-data-access"),
                color = LocalContentColor.current,
                linkColor = MaterialTheme.colorScheme.primary,
                onLinkClicked = {
                    uriHandler.openUri(it)
                }
            )
        }
    )
}