package com.aliernfrog.lactool.ui.component

import android.annotation.SuppressLint
import android.app.Activity
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.Crossfade
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateIntAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandHorizontally
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.shrinkHorizontally
import androidx.compose.animation.shrinkOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingToolbarDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationRail
import androidx.compose.material3.NavigationRailItem
import androidx.compose.material3.PlainTooltip
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TooltipAnchorPosition
import androidx.compose.material3.TooltipBox
import androidx.compose.material3.TooltipDefaults
import androidx.compose.material3.rememberTooltipState
import androidx.compose.material3.ripple
import androidx.compose.material3.windowsizeclass.ExperimentalMaterial3WindowSizeClassApi
import androidx.compose.material3.windowsizeclass.WindowWidthSizeClass
import androidx.compose.material3.windowsizeclass.calculateWindowSizeClass
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
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
import io.github.aliernfrog.shared.ui.theme.AppFABPadding
import io.github.aliernfrog.shared.util.toggledHazeBlur

@OptIn(ExperimentalMaterial3WindowSizeClassApi::class)
@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
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
    val isBottomBarVisible = navigationBarType == NavigationBarType.BOTTOM_BAR

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
            modifier = Modifier
                .background(MaterialTheme.colorScheme.surface)
                .padding(start = animatedSideBarWidth),
            contentWindowInsets = WindowInsets(0, 0, 0, 0)
        ) { _ ->
            Box {
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
                    val bottomPadding = if (isBottomBarVisible) AppFABPadding else 0.dp

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

                FloatingBottomBar(
                    visible = isBottomBarVisible,
                    hazeState = hazeState,
                    destinations = mainDestinations,
                    isDestinationSelected = ::isDestinationSelected,
                    onNavigateRequest = { changeDestination(it) },
                    modifier = Modifier.align(Alignment.BottomCenter)
                )
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun FloatingBottomBar(
    visible: Boolean,
    hazeState: HazeState,
    destinations: List<MainDestination>,
    isDestinationSelected: (MainDestination) -> Boolean,
    modifier: Modifier = Modifier,
    onNavigateRequest: (MainDestination) -> Unit
) {
    AnimatedVisibility(
        visible = visible,
        enter = slideInVertically(
            animationSpec = tween(durationMillis = 150),
            initialOffsetY = { it }
        ) + scaleIn() + fadeIn(),
        exit = slideOutVertically(
            animationSpec = tween(durationMillis = 150),
            targetOffsetY = { it }
        ) + shrinkOut() + fadeOut(),
        modifier = modifier
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .systemBarsPadding()
                .padding(start = 16.dp, end = 16.dp, bottom = 16.dp)
                .shadow(
                    elevation = 6.dp,
                    shape = FloatingToolbarDefaults.ContainerShape
                )
                .clip(FloatingToolbarDefaults.ContainerShape)
                .toggledHazeBlur(
                    containerColor = FloatingToolbarDefaults.standardFloatingToolbarColors().toolbarContainerColor,
                    containerOpacity = 0.7f,
                    input = HazeInput.Backdrop(hazeState),
                )
                .padding(8.dp)
        ) {
            destinations.forEachIndexed { index, destination ->
                val selected = isDestinationSelected(destination)
                val addTrailingSpace = index < destinations.lastIndex

                val interactionSource = remember { MutableInteractionSource() }
                val pressed by interactionSource.collectIsPressedAsState()
                val containerColor by animateColorAsState(
                    if (selected) MaterialTheme.colorScheme.secondaryContainer
                    else Color.Transparent
                )
                val contentColor by animateColorAsState(
                    if (selected) MaterialTheme.colorScheme.onSecondaryContainer
                    else FloatingToolbarDefaults.standardFloatingToolbarColors().toolbarContentColor
                )
                val shape by animateIntAsState(
                    if (pressed) 20 else 50
                )

                TooltipBox(
                    positionProvider = TooltipDefaults.rememberTooltipPositionProvider(
                        positioning = TooltipAnchorPosition.Above
                    ),
                    tooltip = {
                        PlainTooltip {
                            Text(stringResource(destination.label))
                        }
                    },
                    state = rememberTooltipState()
                ) {
                    CompositionLocalProvider(LocalContentColor provides contentColor) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .clip(RoundedCornerShape(shape))
                                .background(containerColor)
                                .clickable(
                                    interactionSource = interactionSource,
                                    indication = ripple()
                                ) {
                                    onNavigateRequest(destination)
                                }
                                .padding(
                                    vertical = 8.dp, horizontal = 8.dp
                                )
                        ) {
                            NavigationItemIcon(
                                destination = destination,
                                selected = selected,
                                modifier = Modifier.size(IconButtonDefaults.smallIconSize)
                            )

                            AnimatedVisibility(
                                visible = selected,
                                enter = expandHorizontally(expandFrom = Alignment.Start) + fadeIn(),
                                exit = shrinkHorizontally(shrinkTowards = Alignment.Start) + fadeOut()
                            ) {
                                Text(
                                    text = stringResource(destination.label),
                                    style = ButtonDefaults.textStyleFor(ButtonDefaults.ExtraSmallContainerHeight),
                                    maxLines = 1,
                                    modifier = Modifier.padding(start = 4.dp)
                                )
                            }
                        }
                    }
                }

                if (addTrailingSpace) Spacer(Modifier.width(8.dp))
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
    selected: Boolean,
    modifier: Modifier = Modifier
) {
    Crossfade(selected) {
        Icon(
            imageVector = if (it) destination.vectorFilled else destination.vectorOutlined,
            contentDescription = null,
            modifier = modifier
        )
    }
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