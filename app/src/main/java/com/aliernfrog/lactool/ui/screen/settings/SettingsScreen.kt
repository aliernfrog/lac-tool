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
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material.icons.rounded.Book
import androidx.compose.material.icons.rounded.FolderOpen
import androidx.compose.material.icons.rounded.Info
import androidx.compose.material.icons.rounded.Palette
import androidx.compose.material.icons.rounded.PinDrop
import androidx.compose.material.icons.rounded.Science
import androidx.compose.material.icons.rounded.Translate
import androidx.compose.material.icons.rounded.Update
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
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
import com.aliernfrog.lactool.ui.component.VerticalSegmentor
import com.aliernfrog.lactool.ui.component.expressive.ExpressiveButtonRow
import com.aliernfrog.lactool.ui.component.expressive.ExpressiveRowIcon
import com.aliernfrog.lactool.ui.component.expressive.ExpressiveSection
import com.aliernfrog.lactool.ui.component.expressive.toRowFriendlyColor
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

            SettingsCategory.entries
                .forEach { category ->
                    val pages = SettingsPage.entries
                        .filter {
                            it.category == category && !(it == SettingsPage.EXPERIMENTAL && !mainViewModel.prefs.experimentalOptionsEnabled.value)
                        }
                    if (pages.isNotEmpty()) ExpressiveSection(
                        title = stringResource(category.title)
                    ) {
                        val buttons: List<@Composable () -> Unit> = pages.map { page -> {
                            ExpressiveButtonRow(
                                title = stringResource(page.title),
                                description = if (page == SettingsPage.ABOUT) mainViewModel.applicationVersionLabel else stringResource(page.description),
                                icon = {
                                    ExpressiveRowIcon(
                                        painter = rememberVectorPainter(page.icon),
                                        containerColor = page.iconContainerColor.toRowFriendlyColor
                                    )
                                }
                            ) {
                                onNavigateRequest(page)
                            }
                        } }

                        VerticalSegmentor(
                            *buttons.toTypedArray(),
                            modifier = Modifier.padding(horizontal = 12.dp)
                        )
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
        ExpressiveButtonRow(
            title = stringResource(R.string.settings_updateNotification_updateAvailable)
                .replace("{VERSION}", versionInfo.versionName),
            description = stringResource(R.string.settings_updateNotification_description),
            icon = { ExpressiveRowIcon(rememberVectorPainter(Icons.Rounded.Update)) },
            containerColor = MaterialTheme.colorScheme.primaryContainer,
            modifier = Modifier.padding(12.dp).clip(AppComponentShape),
            onClick = onClick
        )
    }
}

@Suppress("unused")
enum class SettingsPage(
    val id: String,
    @StringRes val title: Int,
    @StringRes val description: Int,
    val icon: ImageVector,
    val iconContainerColor: Color = Color.Blue,
    val category: SettingsCategory?,
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
        category = null,
        content = { onNavigateBackRequest, onNavigateRequest ->
            SettingsRootPage(
                onNavigateBackRequest = onNavigateBackRequest,
                onNavigateRequest = onNavigateRequest
            )
        }
    ),

    MAPS(
        id = "maps",
        title = R.string.settings_maps,
        description = R.string.settings_maps_description,
        icon = Icons.Rounded.PinDrop,
        iconContainerColor = Color.Green,
        category = SettingsCategory.GAME,
        content = { onNavigateBackRequest, _ ->
            MapsPage(onNavigateBackRequest = onNavigateBackRequest)
        }
    ),

    STORAGE(
        id = "files",
        title = R.string.settings_storage,
        description = R.string.settings_storage_description,
        icon = Icons.Rounded.FolderOpen,
        iconContainerColor = Color.Blue,
        category = SettingsCategory.GAME,
        content = { onNavigateBackRequest, _ ->
            StoragePage(
                onNavigateBackRequest = onNavigateBackRequest
            )
        }
    ),

    APPEARANCE(
        id = "appearance",
        title = R.string.settings_appearance,
        description = R.string.settings_appearance_description,
        icon = Icons.Rounded.Palette,
        iconContainerColor = Color.Yellow,
        category = SettingsCategory.APP,
        content = { onNavigateBackRequest, _ ->
            AppearancePage(onNavigateBackRequest = onNavigateBackRequest)
        }
    ),

    LANGUAGE(
        id = "language",
        title = R.string.settings_language,
        description = R.string.settings_language_description,
        icon = Icons.Rounded.Translate,
        iconContainerColor = Color.Magenta,
        category = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) SettingsCategory.APP else null,
        content = { onNavigateBackRequest, _ ->
            LanguagePage(onNavigateBackRequest = onNavigateBackRequest)
        }
    ),

    EXPERIMENTAL(
        id = "experimental",
        title = R.string.settings_experimental,
        description = R.string.settings_experimental_description,
        icon = Icons.Rounded.Science,
        iconContainerColor = Color.Black,
        category = SettingsCategory.APP,
        content = { onNavigateBackRequest, _ ->
            ExperimentalPage(onNavigateBackRequest = onNavigateBackRequest)
        }
    ),

    ABOUT(
        id = "about",
        title = R.string.settings_about,
        description = R.string.settings_about,
        icon = Icons.Rounded.Info,
        iconContainerColor = Color.Blue,
        category = SettingsCategory.APP,
        content = { onNavigateBackRequest, onNavigateRequest ->
            AboutPage(
                onNavigateLibsRequest = {
                    onNavigateRequest(LIBS)
                },
                onNavigateBackRequest = onNavigateBackRequest
            )
        }
    ),

    LIBS(
        id = "libs",
        title = R.string.settings_about_libs,
        description = R.string.settings_about_libs_description,
        icon = Icons.Rounded.Book,
        category = null,
        content = { onNavigateBackRequest, _ ->
            LibsPage(onNavigateBackRequest = onNavigateBackRequest)
        }
    )
}

enum class SettingsCategory(
    @StringRes val title: Int
) {
    GAME(
        title = R.string.settings_category_game
    ),

    APP(
        title = R.string.settings_category_app
    )
}