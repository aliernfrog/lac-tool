package com.aliernfrog.lactool.ui.component

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.aliernfrog.lactool.R
import com.aliernfrog.lactool.util.Destination

@Composable
fun SettingsButton(
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    val hasNotification = Destination.SETTINGS.hasNotification.value

    @Composable
    fun SettingsIcon() {
        Icon(
            imageVector = Icons.Default.Settings,
            contentDescription = stringResource(R.string.settings)
        )
    }

    IconButton(
        modifier = modifier,
        onClick = {
            onClick()
            Destination.SETTINGS.hasNotification.value = false
        }
    ) {
        if (hasNotification) BadgedBox(
            badge = { Badge() }
        ) {
            SettingsIcon()
        }
        else SettingsIcon()
    }
}