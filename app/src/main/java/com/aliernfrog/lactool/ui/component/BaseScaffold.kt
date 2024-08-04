package com.aliernfrog.lactool.ui.component

import android.app.Activity
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.calculateEndPadding
import androidx.compose.foundation.layout.calculateStartPadding
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.BottomAppBar
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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.compose.currentBackStackEntryAsState
import com.aliernfrog.lactool.R
import com.aliernfrog.lactool.util.Destination
import com.aliernfrog.lactool.util.extension.set

@OptIn(ExperimentalMaterial3WindowSizeClassApi::class)
@Composable
fun BaseScaffold(
    navController: NavController,
    content: @Composable (
        paddingValues: PaddingValues
    ) -> Unit
) {
    val context = LocalContext.current
    val density = LocalDensity.current
    val layoutDirection = LocalLayoutDirection.current

    val destinations = remember { Destination.entries.toList() }
    val mainDestinations = remember { destinations.filter { it.showInNavigationBar } }
    val currentRoute = navController.currentBackStackEntryAsState().value?.destination?.route
    val currentDestination = destinations.find { it.route == currentRoute }

    val windowSizeClass = calculateWindowSizeClass(context as Activity)
    val showNavigationRail: Boolean? = if (mainDestinations.size <= 1) null
    else currentDestination?.showNavigationBar != false && windowSizeClass.widthSizeClass != WindowWidthSizeClass.Compact
    var sideBarWidth by remember { mutableStateOf(0.dp) }

    fun isDestinationSelected(destination: Destination): Boolean {
        return destination.route == currentDestination?.route
    }

    fun changeDestination(destination: Destination) {
        if (!isDestinationSelected(destination) && currentDestination?.showNavigationBar != false)
            navController.set(destination)
    }

    fun toDp(pxs: Int): Dp {
        with(density) {
            return pxs.toDp()
        }
    }

    Scaffold(
        modifier = Modifier
            .background(MaterialTheme.colorScheme.surface)
            .padding(
                start = if (showNavigationRail == true) sideBarWidth else 0.dp
            ),
        bottomBar = {
            if (showNavigationRail == false) BottomBar(
                destinations = mainDestinations,
                currentDestination = currentDestination,
                isDestinationSelected = ::isDestinationSelected,
                onNavigateRequest = { changeDestination(it) }
            )
        },
        contentWindowInsets = WindowInsets(0, 0, 0, 0)
    ) {
        val paddingValues = if (showNavigationRail == true || currentDestination?.showNavigationBar != false) it
        else PaddingValues(
            start = it.calculateStartPadding(layoutDirection),
            top = it.calculateTopPadding(),
            end = it.calculateEndPadding(layoutDirection),
            bottom = 0.dp
        )
        content(paddingValues)
    }

    if (showNavigationRail == true) SideBarRail(
        destinations = mainDestinations,
        currentDestination = currentDestination,
        isDestinationSelected = ::isDestinationSelected,
        onWidthChange = { sideBarWidth = toDp(it) },
        onNavigateRequest = { changeDestination(it) }
    )

    LaunchedEffect(currentDestination) {
        if (currentDestination?.hasNotification?.value == true)
            currentDestination.hasNotification.value = false
    }
}

@Composable
private fun BottomBar(
    destinations: List<Destination>,
    currentDestination: Destination?,
    isDestinationSelected: (Destination) -> Boolean,
    onNavigateRequest: (Destination) -> Unit
) {
    AnimatedVisibility(
        visible = currentDestination?.showNavigationBar != false,
        enter = slideInVertically(animationSpec = tween(durationMillis = 150), initialOffsetY = { it }) + fadeIn(),
        exit = slideOutVertically(animationSpec = tween(durationMillis = 150), targetOffsetY = { it }) + fadeOut()
    ) {
        BottomAppBar {
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
                        Text(stringResource(it.labelId))
                    }
                )
            }
        }
    }
}

@Composable
private fun SideBarRail(
    destinations: List<Destination>,
    currentDestination: Destination?,
    isDestinationSelected: (Destination) -> Boolean,
    onWidthChange: (Int) -> Unit,
    onNavigateRequest: (Destination) -> Unit
) {
    AnimatedVisibility(
        visible = currentDestination?.showNavigationBar != false,
        enter = slideInHorizontally(animationSpec = tween(durationMillis = 150), initialOffsetX = { -it }) + fadeIn(),
        exit = slideOutHorizontally(animationSpec = tween(durationMillis = 150), targetOffsetX = { -it }) + fadeOut()
    ) {
        NavigationRail(
            modifier = Modifier
                .onSizeChanged { onWidthChange(it.width) }
        ) {
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
                        Text(stringResource(it.labelId))
                    }
                )
            }
        }
    }
}

@Composable
private fun NavigationItemIcon(
    destination: Destination,
    selected: Boolean
) {
    @Composable fun ItemIcon() {
        Icon(
            imageVector = if (selected) destination.vectorFilled!! else destination.vectorOutlined!!,
            contentDescription = null
        )
    }

    if (destination.hasNotification.value) BadgedBox(
        badge = { Badge() }
    ) {
        ItemIcon()
    }
    else ItemIcon()
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