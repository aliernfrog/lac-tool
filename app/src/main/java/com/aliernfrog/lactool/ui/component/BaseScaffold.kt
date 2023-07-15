package com.aliernfrog.lactool.ui.component

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.calculateEndPadding
import androidx.compose.foundation.layout.calculateStartPadding
import androidx.compose.material3.BottomAppBar
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.compose.currentBackStackEntryAsState
import com.aliernfrog.lactool.util.Destination

@Composable
fun BaseScaffold(navController: NavController, content: @Composable (PaddingValues) -> Unit) {
    val layoutDirection = LocalLayoutDirection.current
    val destinations = remember { Destination.values().toList() }
    val currentRoute = navController.currentBackStackEntryAsState().value?.destination?.route
    val currentDestination = destinations.find { it.route == currentRoute }
    Scaffold(
        modifier = Modifier.background(MaterialTheme.colorScheme.surface),
        bottomBar = {
            BottomBar(
                navController = navController,
                destinations = destinations,
                currentDestination = currentDestination
            )
        },
        contentWindowInsets = WindowInsets(0, 0, 0, 0)
    ) {
        val paddingValues = if (currentDestination?.isSubScreen != true) it
        else PaddingValues(
            start = it.calculateStartPadding(layoutDirection),
            top = it.calculateTopPadding(),
            end = it.calculateEndPadding(layoutDirection),
            bottom = 0.dp
        )
        content(paddingValues)
    }
}

@Composable
private fun BottomBar(
    navController: NavController,
    destinations: List<Destination>,
    currentDestination: Destination?
) {
    AnimatedVisibility(
        visible = currentDestination?.isSubScreen != true,
        enter = slideInVertically(animationSpec = tween(durationMillis = 150), initialOffsetY = { it }) + fadeIn(),
        exit = slideOutVertically(animationSpec = tween(durationMillis = 150), targetOffsetY = { it }) + fadeOut()
    ) {
        BottomAppBar {
            destinations.filter { !it.isSubScreen }.forEach {
                val selected = it.route == currentDestination?.route
                NavigationBarItem(
                    selected = selected,
                    icon = {
                        Icon(
                            painter = rememberVectorPainter(if (selected) it.vectorFilled!! else it.vectorOutlined!!),
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurface
                        )
                    },
                    label = { Text(stringResource(it.labelId)) },
                    onClick = {
                        if (!selected && currentDestination?.isSubScreen != true)
                            navController.navigate(it.route) { popUpTo(0) }
                    }
                )
            }
        }
    }
}