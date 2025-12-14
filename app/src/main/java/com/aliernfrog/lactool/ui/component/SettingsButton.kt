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
import com.aliernfrog.lactool.ui.viewmodel.MainViewModel
import org.koin.androidx.compose.koinViewModel

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun SettingsButton(
    modifier: Modifier = Modifier,
    mainViewModel: MainViewModel = koinViewModel(),
    onClick: () -> Unit
) {
    val hasNotification = mainViewModel.showUpdateNotification

    @Composable
    fun SettingsIcon() {
        Icon(
            imageVector = Icons.Default.Settings,
            contentDescription = stringResource(R.string.settings)
        )
    }

    IconButton(
        shapes = IconButtonDefaults.shapes(),
        modifier = modifier,
        onClick = {
            onClick()
            mainViewModel.showUpdateNotification = false
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