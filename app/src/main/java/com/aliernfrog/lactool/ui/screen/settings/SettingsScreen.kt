package com.aliernfrog.lactool.ui.screen.settings

import android.os.Build
import androidx.annotation.StringRes
import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Book
import androidx.compose.material.icons.filled.Update
import androidx.compose.material.icons.outlined.FolderOpen
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.Palette
import androidx.compose.material.icons.outlined.PinDrop
import androidx.compose.material.icons.outlined.Science
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material.icons.outlined.Translate
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.aliernfrog.lactool.R
import com.aliernfrog.lactool.data.ReleaseInfo
import com.aliernfrog.lactool.ui.component.AppScaffold
import com.aliernfrog.lactool.ui.component.AppSmallTopBar
import com.aliernfrog.lactool.ui.component.AppTopBar
import com.aliernfrog.lactool.ui.component.form.ButtonRow
import com.aliernfrog.lactool.ui.theme.AppComponentShape
import com.aliernfrog.lactool.ui.viewmodel.MainViewModel
import com.aliernfrog.lactool.util.extension.popBackStackSafe
import kotlinx.coroutines.launch
import org.koin.androidx.compose.koinViewModel

@Composable
fun SettingsScreen(
    onNavigateBackRequest: () -> Unit
) {
    val navController = rememberNavController()

    NavHost(
        navController = navController,
        startDestination = SettingsPage.ROOT.id,
        enterTransition = {
            slideIntoContainer(AnimatedContentTransitionScope.SlideDirection.Start) + fadeIn()
        },
        exitTransition = {
            slideOutOfContainer(AnimatedContentTransitionScope.SlideDirection.Start) + fadeOut()
        },
        popEnterTransition = {
            slideIntoContainer(AnimatedContentTransitionScope.SlideDirection.End) + fadeIn()
        },
        popExitTransition = {
            slideOutOfContainer(AnimatedContentTransitionScope.SlideDirection.End) + fadeOut()
        }
    ) {
        SettingsPage.entries.forEach { page ->
            composable(page.id) {
                page.content (
                    { navController.popBackStackSafe(onNoBackStack = onNavigateBackRequest) },
                    { navController.navigate(it.id) }
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SettingsRootPage(
    mainViewModel: MainViewModel = koinViewModel(),
    onNavigateBackRequest: () -> Unit,
    onNavigateRequest: (SettingsPage) -> Unit
) {
    val scope = rememberCoroutineScope()
    val appVersion = remember {
        "${mainViewModel.applicationVersionName} (${mainViewModel.applicationVersionCode})"
    }

    AppScaffold(
        topBar = { scrollBehavior ->
            AppTopBar(
                title = stringResource(R.string.settings),
                scrollBehavior = scrollBehavior,
                onNavigationClick = onNavigateBackRequest
            )
        }
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .navigationBarsPadding()
        ) {
            UpdateNotification(
                isShown = mainViewModel.updateAvailable,
                versionInfo = mainViewModel.latestVersionInfo,
                onClick = { scope.launch {
                    mainViewModel.updateSheetState.show()
                } }
            )

            SettingsPage.entries
                .filter {
                    it.showInSettingsHome && !(it == SettingsPage.EXPERIMENTAL && !mainViewModel.prefs.experimentalOptionsEnabled)
                }
                .forEach { page ->
                    ButtonRow(
                        title = stringResource(page.title),
                        description = if (page == SettingsPage.ABOUT) appVersion else stringResource(page.description),
                        painter = rememberVectorPainter(page.icon)
                    ) {
                        onNavigateRequest(page)
                    }
                }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsPageContainer(
    title: String,
    onNavigateBackRequest: () -> Unit,
    content: @Composable ColumnScope.() -> Unit
) {
    AppScaffold(
        topBar = { scrollBehavior ->
            AppSmallTopBar(
                title = title,
                scrollBehavior = scrollBehavior,
                onNavigationClick = onNavigateBackRequest
            )
        },
        scrollBehavior = TopAppBarDefaults.pinnedScrollBehavior()
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .navigationBarsPadding(),
            content = content
        )
    }
}

@Composable
private fun UpdateNotification(
    isShown: Boolean,
    versionInfo: ReleaseInfo,
    onClick: () -> Unit
) {
    AnimatedVisibility(
        visible = isShown,
        enter = fadeIn() + expandVertically(),
        exit = fadeOut() + shrinkVertically()
    ) {
        ButtonRow(
            title = stringResource(R.string.settings_updateNotification_updateAvailable)
                .replace("{VERSION}", versionInfo.versionName),
            description = stringResource(R.string.settings_updateNotification_description),
            painter = rememberVectorPainter(Icons.Default.Update),
            shape = AppComponentShape,
            containerColor = MaterialTheme.colorScheme.primaryContainer,
            onClick = onClick,
            modifier = Modifier.padding(16.dp)
        )
    }
}

@Suppress("unused")
enum class SettingsPage(
    val id: String,
    @StringRes val title: Int,
    @StringRes val description: Int,
    val icon: ImageVector,
    val showInSettingsHome: Boolean = true,
    val content: @Composable (
        onNavigateBackRequest: () -> Unit,
        onNavigateRequest: (SettingsPage) -> Unit
    ) -> Unit
) {
    ROOT(
        id = "root",
        title = R.string.settings,
        description = R.string.settings,
        icon = Icons.Outlined.Settings,
        showInSettingsHome = false,
        content = { onNavigateBackRequest, onNavigateRequest ->
            SettingsRootPage(
                onNavigateBackRequest = onNavigateBackRequest,
                onNavigateRequest = onNavigateRequest
            )
        }
    ),

    APPEARANCE(
        id = "appearance",
        title = R.string.settings_appearance,
        description = R.string.settings_appearance_description,
        icon = Icons.Outlined.Palette,
        content = { onNavigateBackRequest, _ ->
            AppearancePage(onNavigateBackRequest = onNavigateBackRequest)
        }
    ),

    MAPS(
        id = "maps",
        title = R.string.settings_maps,
        description = R.string.settings_maps_description,
        icon = Icons.Outlined.PinDrop,
        content = { onNavigateBackRequest, _ ->
            MapsPage(onNavigateBackRequest = onNavigateBackRequest)
        }
    ),

    STORAGE(
        id = "files",
        title = R.string.settings_storage,
        description = R.string.settings_storage_description,
        icon = Icons.Outlined.FolderOpen,
        content = { onNavigateBackRequest, _ ->
            StoragePage(
                onNavigateBackRequest = onNavigateBackRequest
            )
        }
    ),

    LANGUAGE(
        id = "language",
        title = R.string.settings_language,
        description = R.string.settings_language_description,
        icon = Icons.Outlined.Translate,
        showInSettingsHome = Build.VERSION.SDK_INT >= Build.VERSION_CODES.N,
        content = { onNavigateBackRequest, _ ->
            LanguagePage(onNavigateBackRequest = onNavigateBackRequest)
        }
    ),

    EXPERIMENTAL(
        id = "experimental",
        title = R.string.settings_experimental,
        description = R.string.settings_experimental_description,
        icon = Icons.Outlined.Science,
        content = { onNavigateBackRequest, _ ->
            ExperimentalPage(onNavigateBackRequest = onNavigateBackRequest)
        }
    ),

    LIBS(
        id = "libs",
        title = R.string.settings_about_libs,
        description = R.string.settings_about_libs_description,
        icon = Icons.Default.Book,
        showInSettingsHome = false,
        content = { onNavigateBackRequest, _ ->
            LibsPage(onNavigateBackRequest = onNavigateBackRequest)
        }
    ),

    ABOUT(
        id = "about",
        title = R.string.settings_about,
        description = R.string.settings_about,
        icon = Icons.Outlined.Info,
        content = { onNavigateBackRequest, onNavigateRequest ->
            AboutPage(
                onNavigateLibsRequest = {
                    onNavigateRequest(LIBS)
                },
                onNavigateBackRequest = onNavigateBackRequest
            )
        }
    )
}