package com.aliernfrog.lactool.ui.component

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.aliernfrog.lactool.R
import com.aliernfrog.lactool.domain.AppState
import io.github.aliernfrog.shared.ui.component.PlainTextTooltipContainer
import org.koin.compose.koinInject

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun SettingsButton(
    modifier: Modifier = Modifier,
    appState: AppState = koinInject(),
    onClick: () -> Unit
) {
    @Composable
    fun SettingsIcon() {
        Icon(
            imageVector = Icons.Default.Settings,
            contentDescription = stringResource(R.string.settings)
        )
    }

    PlainTextTooltipContainer(
        tooltipText = stringResource(R.string.settings),
        modifier = modifier
    ) {
        IconButton(
            shapes = IconButtonDefaults.shapes(),
            onClick = {
                onClick()
                appState.showUpdateNotification = false
            }
        ) {
            if (appState.showUpdateNotification) BadgedBox(
                badge = { Badge() }
            ) {
                SettingsIcon()
            }
            else SettingsIcon()
        }
    }
}