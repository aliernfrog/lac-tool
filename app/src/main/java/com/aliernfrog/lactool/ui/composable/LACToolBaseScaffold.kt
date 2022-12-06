package com.aliernfrog.lactool.ui.composable

import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.compose.currentBackStackEntryAsState
import com.aliernfrog.lactool.R
import com.aliernfrog.lactool.data.Screen
import com.aliernfrog.lactool.util.NavigationConstant

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LACToolBaseScaffold(screens: List<Screen>, navController: NavController, content: @Composable (PaddingValues) -> Unit) {
    val currentRoute = navController.currentBackStackEntryAsState().value?.destination?.route
    val currentScreen = screens.find { it.route == currentRoute }
    val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior(rememberTopAppBarState())
    Scaffold(
        modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection).imePadding(),
        topBar = { TopBar(navController, scrollBehavior, currentScreen) },
        bottomBar = { BottomBar(navController, screens, currentScreen) },
        contentWindowInsets = WindowInsets(0, 0, 0, 0)
    ) {
        content(it)
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun TopBar(navController: NavController, scrollBehavior: TopAppBarScrollBehavior, currentScreen: Screen?) {
    LargeTopAppBar(
        scrollBehavior = scrollBehavior,
        title = {
            Crossfade(targetState = currentScreen?.name) {
                Text(text = it ?: stringResource(NavigationConstant.LABEL_FALLBACK_ID))
            }
        },
        navigationIcon = {
            AnimatedVisibility(
                visible = navController.previousBackStackEntry != null,
                enter = slideInHorizontally() + expandHorizontally() + fadeIn(),
                exit = slideOutHorizontally() + shrinkHorizontally() + fadeOut()
            ) {
                IconButton(onClick = { currentScreen?.onNavigationBack?.invoke() }) {
                    Icon(
                        imageVector = Icons.Filled.ArrowBack,
                        contentDescription = stringResource(R.string.action_back),
                        modifier = Modifier.padding(horizontal = 8.dp)
                    )
                }
            }
        }
    )
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun BottomBar(navController: NavController, screens: List<Screen>, currentScreen: Screen?) {
    AnimatedVisibility(
        visible = !WindowInsets.isImeVisible && !(currentScreen?.isSubScreen ?: false),
        enter = slideInVertically(initialOffsetY = { it }, animationSpec = tween(durationMillis = 100)) + fadeIn(),
        exit = fadeOut(animationSpec = tween(durationMillis = 0))
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