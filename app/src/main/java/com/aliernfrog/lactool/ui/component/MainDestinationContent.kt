package com.aliernfrog.lactool.ui.component

import android.app.Activity
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.BottomAppBar
import androidx.compose.material3.BottomAppBarDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationRail
import androidx.compose.material3.NavigationRailItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.windowsizeclass.ExperimentalMaterial3WindowSizeClassApi
import androidx.compose.material3.windowsizeclass.WindowWidthSizeClass
import androidx.compose.material3.windowsizeclass.calculateWindowSizeClass
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.aliernfrog.lactool.R
import com.aliernfrog.lactool.ui.screen.maps.MapsScreen
import com.aliernfrog.lactool.ui.screen.screenshots.ScreenshotsPermissionsScreen
import com.aliernfrog.lactool.ui.screen.wallpapers.WallpapersPermissionsScreen
import com.aliernfrog.lactool.ui.viewmodel.MainViewModel
import com.aliernfrog.lactool.util.MainDestination
import com.aliernfrog.lactool.util.NavigationBarType
import dev.chrisbanes.haze.HazeInput
import dev.chrisbanes.haze.HazeState
import dev.chrisbanes.haze.hazeSource
import dev.chrisbanes.haze.rememberHazeState
import io.github.aliernfrog.shared.util.toggledHazeBlur

@OptIn(ExperimentalMaterial3WindowSizeClassApi::class)
@Composable
fun MainDestinationContent(
    vm: MainViewModel
) {
    val context = LocalContext.current
    val density = LocalDensity.current
    val hazeState = rememberHazeState()

    val mainDestinations = remember { MainDestination.entries }
    val currentMainDestination = vm.currentMainDestination
    val isAtMainDestination = vm.isAtMainDestination

    val windowSizeClass = calculateWindowSizeClass(context as Activity)
    val navigationBarType = if (mainDestinations.size <= 1) NavigationBarType.HIDDEN
    else if (windowSizeClass.widthSizeClass == WindowWidthSizeClass.Compact) NavigationBarType.BOTTOM_BAR
    else NavigationBarType.SIDE_RAIL

    var sideBarWidth by remember { mutableStateOf(0.dp) }
    val animatedSideBarWidth by animateDpAsState(
        if (navigationBarType == NavigationBarType.SIDE_RAIL) sideBarWidth else 0.dp
    )

    fun onNavigateRequest(entry: Any) {
        vm.navigationBackStack.add(entry)
    }

    fun isDestinationSelected(destination: MainDestination): Boolean {
        return destination == currentMainDestination
    }

    fun changeDestination(destination: MainDestination) {
        if (!isDestinationSelected(destination) && isAtMainDestination)
            vm.currentMainDestination = destination
    }

    Box {
        Scaffold(
            bottomBar = {
                BottomBar(
                    visible = navigationBarType == NavigationBarType.BOTTOM_BAR,
                    hazeState = hazeState,
                    destinations = mainDestinations,
                    isDestinationSelected = ::isDestinationSelected,
                    onNavigateRequest = { changeDestination(it) }
                )
            },
            modifier = Modifier
                .background(MaterialTheme.colorScheme.surface)
                .padding(start = animatedSideBarWidth),
            contentWindowInsets = WindowInsets(0, 0, 0, 0)
        ) { paddingValues ->
            val bottomPadding = paddingValues.calculateBottomPadding()

            AnimatedContent(
                targetState = currentMainDestination,
                transitionSpec = {
                    scaleIn(
                        animationSpec = tween(delayMillis = 100),
                        initialScale = 0.95f
                    ) + fadeIn(
                        animationSpec = tween(delayMillis = 100)
                    ) togetherWith fadeOut(tween(100))
                },
                modifier = Modifier
                    .fillMaxSize()
                    .hazeSource(hazeState)
            ) { destination ->
                when (destination) {
                    MainDestination.MAPS -> {
                        MapsScreen(
                            bottomPadding = bottomPadding,
                            onNavigateRequest = ::onNavigateRequest
                        )
                    }
                    MainDestination.WALLPAPERS -> {
                        WallpapersPermissionsScreen(
                            bottomPadding = bottomPadding,
                            onNavigateRequest = ::onNavigateRequest
                        )
                    }
                    MainDestination.SCREENSHOTS -> {
                        ScreenshotsPermissionsScreen(
                            bottomPadding = bottomPadding,
                            onNavigateRequest = ::onNavigateRequest
                        )
                    }
                }
            }
        }

        SideBarRail(
            visible = navigationBarType == NavigationBarType.SIDE_RAIL,
            destinations = mainDestinations,
            isDestinationSelected = ::isDestinationSelected,
            onNavigateRequest = { changeDestination(it) },
            modifier = Modifier
                .align(Alignment.TopStart)
                .onSizeChanged {
                    sideBarWidth = with(density) { it.width.toDp() }
                }
        )
    }
}

@Composable
private fun BottomBar(
    visible: Boolean,
    hazeState: HazeState,
    destinations: List<MainDestination>,
    isDestinationSelected: (MainDestination) -> Boolean,
    modifier: Modifier = Modifier,
    onNavigateRequest: (MainDestination) -> Unit
) {
    AnimatedVisibility(
        visible = visible,
        enter = slideInVertically(animationSpec = tween(durationMillis = 150), initialOffsetY = { it }) + fadeIn(),
        exit = slideOutVertically(animationSpec = tween(durationMillis = 150), targetOffsetY = { it }) + fadeOut(),
        modifier = modifier
    ) {
        BottomAppBar(
            containerColor = Color.Transparent,
            modifier = modifier.toggledHazeBlur(
                containerColor = BottomAppBarDefaults.containerColor,
                containerOpacity = 0.7f,
                input = HazeInput.Backdrop(hazeState)
            )
        ) {
            destinations.forEach {
                val selected = isDestinationSelected(it)
                NavigationBarItem(
                    selected = selected,
                    onClick = {
                        onNavigateRequest(it)
                    },
                    icon = {
                        NavigationItemIcon(
                            destination = it,
                            selected = selected
                        )
                    },
                    label = {
                        Text(stringResource(it.label))
                    }
                )
            }
        }
    }
}

@Composable
private fun SideBarRail(
    visible: Boolean,
    destinations: List<MainDestination>,
    isDestinationSelected: (MainDestination) -> Boolean,
    modifier: Modifier = Modifier,
    onNavigateRequest: (MainDestination) -> Unit
) {
    AnimatedVisibility(
        visible = visible,
        enter = slideInHorizontally(animationSpec = tween(durationMillis = 150), initialOffsetX = { -it }) + fadeIn(),
        exit = slideOutHorizontally(animationSpec = tween(durationMillis = 150), targetOffsetX = { -it }) + fadeOut(),
        modifier = modifier
    ) {
        NavigationRail {
            AppIcon()
            destinations.forEach {
                val selected = isDestinationSelected(it)
                NavigationRailItem(
                    selected = selected,
                    onClick = {
                        onNavigateRequest(it)
                    },
                    icon = {
                        NavigationItemIcon(
                            destination = it,
                            selected = selected
                        )
                    },
                    label = {
                        Text(stringResource(it.label))
                    }
                )
            }
        }
    }
}

@Composable
private fun NavigationItemIcon(
    destination: MainDestination,
    selected: Boolean
) {
    Icon(
        imageVector = if (selected) destination.vectorFilled else destination.vectorOutlined,
        contentDescription = null
    )
}

@Composable
private fun AppIcon() {
    Icon(
        painter = painterResource(R.drawable.lactool),
        contentDescription = stringResource(R.string.app_name),
        tint = MaterialTheme.colorScheme.onSurface,
        modifier = Modifier
            .padding(bottom = 12.dp)
            .height(64.dp)
            .scale(1.5f)
    )
}