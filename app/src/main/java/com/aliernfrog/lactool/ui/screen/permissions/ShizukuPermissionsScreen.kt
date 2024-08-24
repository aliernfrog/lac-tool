package com.aliernfrog.lactool.ui.screen.permissions

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.OpenInNew
import androidx.compose.material.icons.filled.RestartAlt
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.aliernfrog.lactool.R
import com.aliernfrog.lactool.enum.ShizukuStatus
import com.aliernfrog.lactool.ui.component.ButtonIcon
import com.aliernfrog.lactool.ui.component.CardWithActions
import com.aliernfrog.lactool.ui.viewmodel.ShizukuViewModel
import com.aliernfrog.lactool.util.staticutil.GeneralUtil
import org.koin.androidx.compose.koinViewModel
import rikka.shizuku.Shizuku

@Composable
fun ShizukuPermissionsScreen(
    shizukuViewModel: ShizukuViewModel = koinViewModel(),
    onUpdateStateRequest: () -> Unit
) {
    val context = LocalContext.current

    LaunchedEffect(Unit) {
        shizukuViewModel.checkAvailability(context)
    }

    LaunchedEffect(shizukuViewModel.fileServiceRunning) {
        onUpdateStateRequest()
    }

    AnimatedContent(
        shizukuViewModel.status == ShizukuStatus.AVAILABLE
    ) { isLoading ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
        ) {
            if (isLoading) {
                CircularProgressIndicator(Modifier.align(Alignment.CenterHorizontally))
                Text(
                    text = stringResource(R.string.info_shizuku_waitingService),
                    modifier = Modifier.align(Alignment.CenterHorizontally).padding(top = 8.dp)
                )
                AnimatedVisibility(
                    visible = shizukuViewModel.timedOut,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    CardWithActions(
                        title = null,
                        modifier = Modifier
                            .align(Alignment.CenterHorizontally)
                            .padding(16.dp),
                        buttons = {
                            TextButton(
                                onClick = {
                                    shizukuViewModel.launchManager(context)
                                }
                            ) {
                                ButtonIcon(rememberVectorPainter(Icons.AutoMirrored.Filled.OpenInNew))
                                Text(stringResource(R.string.permissions_shizuku_openShizuku))
                            }
                            Button(
                                onClick = {
                                    GeneralUtil.restartApp(context)
                                },
                                modifier = Modifier.padding(top = 8.dp)
                            ) {
                                ButtonIcon(rememberVectorPainter(Icons.Default.RestartAlt))
                                Text(stringResource(R.string.permissions_shizuku_timedOut_restart))
                            }
                        }
                    ) {
                        Text(stringResource(R.string.permissions_shizuku_timedOut))
                    }
                }
            } else ShizukuSetupGuide()
        }
    }
}

@Composable
private fun ShizukuSetupGuide(
    shizukuViewModel: ShizukuViewModel = koinViewModel()
) {
    val context = LocalContext.current
    val uriHandler = LocalUriHandler.current

    if (shizukuViewModel.installed) Text(
        text = stringResource(R.string.permissions_shizuku_introduction),
        style = MaterialTheme.typography.bodyMedium,
        modifier = Modifier.padding(8.dp)
    )

    AnimatedContent(shizukuViewModel.status) { status ->
        val title = when (status) {
            ShizukuStatus.UNKNOWN, ShizukuStatus.NOT_INSTALLED -> R.string.permissions_shizuku_install_title
            ShizukuStatus.WAITING_FOR_BINDER -> R.string.permissions_shizuku_notRunning
            ShizukuStatus.UNAUTHORIZED -> R.string.permissions_shizuku_permission
            else -> null
        }
        val description = when (status) {
            ShizukuStatus.UNKNOWN, ShizukuStatus.NOT_INSTALLED -> R.string.permissions_shizuku_introduction
            ShizukuStatus.WAITING_FOR_BINDER -> R.string.permissions_shizuku_notRunning_description
            ShizukuStatus.UNAUTHORIZED -> R.string.permissions_shizuku_permission_description
            else -> null
        }
        val button: @Composable () -> Unit = { when (status) {
            ShizukuStatus.UNKNOWN, ShizukuStatus.NOT_INSTALLED -> {
                Button(
                    onClick = {
                        uriHandler.openUri("https://play.google.com/store/apps/details?id=moe.shizuku.privileged.api")
                    }
                ) {
                    ButtonIcon(rememberVectorPainter(Icons.AutoMirrored.Filled.OpenInNew))
                    Text(stringResource(R.string.permissions_shizuku_install_install))
                }
            }
            ShizukuStatus.WAITING_FOR_BINDER -> {
                Button(
                    onClick = {
                        shizukuViewModel.launchManager(context)
                    }
                ) {
                    ButtonIcon(rememberVectorPainter(Icons.AutoMirrored.Filled.OpenInNew))
                    Text(stringResource(R.string.permissions_shizuku_openShizuku))
                }
            }
            ShizukuStatus.UNAUTHORIZED -> {
                Button(
                    onClick = { Shizuku.requestPermission(0) }
                ) {
                    Text(stringResource(R.string.permissions_shizuku_permission_grant))
                }
            }
            else -> {}
        } }

        CardWithActions(
            title = title?.let { stringResource(it) } ?: "",
            buttons = { button() },
            modifier = Modifier.fillMaxWidth().padding(8.dp)
        ) {
            description?.let {
                Text(
                    text = stringResource(it),
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }
    }

    Spacer(Modifier.navigationBarsPadding())
}