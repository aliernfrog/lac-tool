package com.aliernfrog.lactool.ui.screen.permissions

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.OpenInNew
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.NotStarted
import androidx.compose.material.icons.filled.OpenInBrowser
import androidx.compose.material.icons.filled.RestartAlt
import androidx.compose.material.icons.filled.Security
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ContainedLoadingIndicator
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.aliernfrog.lactool.R
import com.aliernfrog.lactool.di.getKoinInstance
import com.aliernfrog.lactool.enum.ShizukuStatus
import com.aliernfrog.lactool.ui.screen.settings.SettingsDestination
import com.aliernfrog.lactool.ui.viewmodel.MainViewModel
import com.aliernfrog.lactool.ui.viewmodel.ShizukuViewModel
import com.aliernfrog.lactool.util.staticutil.GeneralUtil
import io.github.aliernfrog.pftool_shared.ui.component.ButtonIcon
import io.github.aliernfrog.pftool_shared.ui.component.CardWithActions
import io.github.aliernfrog.pftool_shared.ui.component.FadeVisibility
import io.github.aliernfrog.pftool_shared.ui.component.expressive.ExpressiveButtonRow
import io.github.aliernfrog.pftool_shared.ui.component.expressive.ExpressiveRowIcon
import io.github.aliernfrog.pftool_shared.ui.component.verticalSegmentedShape
import io.github.aliernfrog.pftool_shared.ui.theme.AppComponentShape
import org.koin.androidx.compose.koinViewModel
import rikka.shizuku.Shizuku

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
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
                .navigationBarsPadding(),
            verticalArrangement = if (isLoading) Arrangement.Center else Arrangement.Top
        ) {
            if (isLoading) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier
                        .align(Alignment.CenterHorizontally)
                        .padding(horizontal = 12.dp)
                        .clip(AppComponentShape)
                        .background(MaterialTheme.colorScheme.surfaceContainer)
                        .padding(horizontal = 24.dp, vertical = 16.dp)
                ) {
                    ContainedLoadingIndicator()
                    Text(
                        text = stringResource(R.string.permissions_shizuku_waitingService),
                        modifier = Modifier.padding(top = 16.dp)
                    )
                }
                AnimatedVisibility(
                    visible = shizukuViewModel.timedOut,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.align(Alignment.CenterHorizontally),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        if (shizukuViewModel.shizukuVersionProblematic) ProblematicManagerCard(
                            modifier = Modifier
                                .wrapContentWidth()
                                .padding(16.dp)
                        )

                        CardWithActions(
                            title = null,
                            modifier = Modifier
                                .wrapContentWidth()
                                .padding(16.dp),
                            buttons = {
                                if (shizukuViewModel.shizukuInstalled) TextButton(
                                    shapes = ButtonDefaults.shapes(),
                                    onClick = {
                                        shizukuViewModel.launchShizuku(context)
                                    }
                                ) {
                                    ButtonIcon(rememberVectorPainter(Icons.AutoMirrored.Filled.OpenInNew))
                                    Text(stringResource(R.string.permissions_shizuku_openShizuku))
                                }
                                Button(
                                    shapes = ButtonDefaults.shapes(),
                                    onClick = {
                                        shizukuViewModel.prefs.shizukuNeverLoad.value = false
                                        GeneralUtil.restartApp(context)
                                    }
                                ) {
                                    ButtonIcon(rememberVectorPainter(Icons.Default.RestartAlt))
                                    Text(stringResource(R.string.permissions_shizuku_waitingService_timedOut_restart))
                                }
                            }
                        ) {
                            Text(stringResource(R.string.permissions_shizuku_waitingService_timedOut))
                        }
                    }
                }
            } else ShizukuSetupGuide()
        }
    }
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
private fun ProblematicManagerCard(
    modifier: Modifier = Modifier,
    shizukuViewModel: ShizukuViewModel = koinViewModel()
) {
    val context = LocalContext.current
    val uriHandler = LocalUriHandler.current
    val currentManagerVersion = remember {
        "v" + shizukuViewModel.getCurrentShizukuVersionNameSimplified(context)
    }
    CardWithActions(
        title = null,
        modifier = modifier,
        buttons = {
            TextButton(
                shapes = ButtonDefaults.shapes(),
                onClick = {
                    uriHandler.openUri(ShizukuViewModel.SHIZUKU_RELEASES_URL)
                }
            ) {
                ButtonIcon(rememberVectorPainter(Icons.Default.OpenInBrowser))
                Text(stringResource(R.string.permissions_shizuku_problematicVersion_allVersions))
            }

            Button(
                shapes = ButtonDefaults.shapes(),
                onClick = {
                    uriHandler.openUri(ShizukuViewModel.SHIZUKU_RECOMMENDED_VERSION_DOWNLOAD_URL)
                }
            ) {
                ButtonIcon(rememberVectorPainter(Icons.Default.Download))
                Text(stringResource(R.string.permissions_shizuku_problematicVersion_downloadRecommended))
            }
        }
    ) {
        Text(
            text = stringResource(R.string.permissions_shizuku_problematicVersion)
                .replace("{CURRENT_VERSION}", currentManagerVersion)
                .replace("{RECOMMENDED_VERSION}", ShizukuViewModel.SHIZUKU_RECOMMENDED_VERSION_NAME)
        )
        Text(
            text = stringResource(R.string.permissions_shizuku_problematicVersion_note)
                .replace("{CURRENT_VERSION}", currentManagerVersion),
            style = MaterialTheme.typography.bodySmallEmphasized
        )
    }
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
private fun ShizukuSetupGuide(
    shizukuViewModel: ShizukuViewModel = koinViewModel()
) {
    val context = LocalContext.current
    val uriHandler = LocalUriHandler.current

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
        val icon = when (status) {
            ShizukuStatus.UNKNOWN, ShizukuStatus.NOT_INSTALLED -> Icons.Default.Download
            ShizukuStatus.WAITING_FOR_BINDER -> Icons.Default.NotStarted
            ShizukuStatus.UNAUTHORIZED -> Icons.Default.Security
            else -> null
        }
        val button: (@Composable () -> Unit)? = { when (status) {
            ShizukuStatus.UNKNOWN, ShizukuStatus.NOT_INSTALLED -> {
                Button(
                    shapes = ButtonDefaults.shapes(),
                    onClick = {
                        shizukuViewModel.launchShizuku(context)
                    }
                ) {
                    ButtonIcon(rememberVectorPainter(Icons.AutoMirrored.Filled.OpenInNew))
                    Text(stringResource(R.string.permissions_shizuku_installShizuku))
                }
            }
            ShizukuStatus.WAITING_FOR_BINDER -> {
                Button(
                    shapes = ButtonDefaults.shapes(),
                    onClick = {
                        shizukuViewModel.launchShizuku(context)
                    }
                ) {
                    ButtonIcon(rememberVectorPainter(Icons.AutoMirrored.Filled.OpenInNew))
                    Text(stringResource(R.string.permissions_shizuku_openShizuku))
                }
            }
            ShizukuStatus.UNAUTHORIZED -> {
                Button(
                    shapes = ButtonDefaults.shapes(),
                    onClick = { Shizuku.requestPermission(0) }
                ) {
                    Text(stringResource(R.string.permissions_shizuku_permission_grant))
                }
            }
            else -> null
        } }

        PermissionsScreenAction(
            title = title?.let { stringResource(it) },
            description = description?.let { stringResource(it) },
            icon = icon,
            button = button
        )
    }

    FadeVisibility(
        shizukuViewModel.deviceRooted && shizukuViewModel.status != ShizukuStatus.UNAUTHORIZED
    ) {
        CardWithActions(
            title = stringResource(R.string.permissions_shizuku_rooted),
            buttons = {
                OutlinedButton(
                    shapes = ButtonDefaults.shapes(),
                    onClick = {
                        uriHandler.openUri(ShizukuViewModel.SUI_GITHUB)
                    }
                ) {
                    ButtonIcon(rememberVectorPainter(Icons.AutoMirrored.Filled.OpenInNew))
                    Text(stringResource(R.string.permissions_shizuku_sui))
                }
                Button(
                    shapes = ButtonDefaults.shapes(),
                    onClick = {
                        shizukuViewModel.launchShizuku(context)
                    }
                ) {
                    ButtonIcon(rememberVectorPainter(Icons.AutoMirrored.Filled.OpenInNew))
                    Text(stringResource(
                        if (shizukuViewModel.shizukuInstalled) R.string.permissions_shizuku_openShizuku
                        else R.string.permissions_shizuku_installShizuku
                    ))
                }
            },
            modifier = Modifier.fillMaxWidth().padding(8.dp)
        ) {
            Text(
                text = stringResource(R.string.permissions_shizuku_rooted_description),
                style = MaterialTheme.typography.bodyMedium
            )
        }
    }

    if (shizukuViewModel.shizukuInstalled) ExpressiveButtonRow(
        title = stringResource(R.string.info),
        description = stringResource(R.string.permissions_shizuku_introduction),
        icon = {
            ExpressiveRowIcon(rememberVectorPainter(Icons.Default.Info))
        },
        modifier = Modifier
            .padding(12.dp)
            .verticalSegmentedShape()
    ) {
        val mainViewModel = getKoinInstance<MainViewModel>()
        mainViewModel.navigationBackStack.add(SettingsDestination.STORAGE)
    }

    Spacer(Modifier.navigationBarsPadding())
}