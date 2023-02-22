package com.aliernfrog.lactool.ui.component

import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.compose.currentBackStackEntryAsState
import com.aliernfrog.lactool.data.Screen

@Composable
fun BaseScaffold(screens: List<Screen>, navController: NavController, content: @Composable (PaddingValues) -> Unit) {
    val layoutDirection = LocalLayoutDirection.current
    val currentRoute = navController.currentBackStackEntryAsState().value?.destination?.route
    val currentScreen = screens.find { it.route == currentRoute }
    Scaffold(
        modifier = Modifier.background(MaterialTheme.colorScheme.surface),
        bottomBar = { BottomBar(navController, screens, currentScreen) },
        contentWindowInsets = WindowInsets(0, 0, 0, 0)
    ) {
        val paddingValues = if (currentScreen?.isSubScreen != true) it
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
private fun BottomBar(navController: NavController, screens: List<Screen>, currentScreen: Screen?) {
    AnimatedVisibility(
        visible = !(currentScreen?.isSubScreen ?: false),
        enter = slideInVertically(animationSpec = tween(durationMillis = 150), initialOffsetY = { it }) + fadeIn(),
        exit = slideOutVertically(animationSpec = tween(durationMillis = 150), targetOffsetY = { it }) + fadeOut()
    ) {
        BottomAppBar {
            screens.filter { !it.isSubScreen }.forEach {
                val selected = it.route == currentScreen?.route
                NavigationBarItem(
                    selected = selected,
                    icon = {
                        Icon(
                            painter = if (selected) it.iconFilled!! else it.iconOutlined!!,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurface
                        )
                    },
                    label = { Text(it.name) },
                    onClick = {
                        if (it.route != currentScreen?.route) navController.navigate(it.route) { popUpTo(0) }
                    }
                )
            }
        }
    }
}