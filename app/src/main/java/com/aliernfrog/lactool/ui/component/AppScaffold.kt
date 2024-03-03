package com.aliernfrog.lactool.ui.component

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LargeTopAppBar
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarColors
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.TopAppBarScrollBehavior
import androidx.compose.material3.TopAppBarState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.res.stringResource
import com.aliernfrog.lactool.R

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppScaffold(
    topBar: @Composable (scrollBehavior: TopAppBarScrollBehavior) -> Unit,
    topAppBarState: TopAppBarState = TopAppBarState(0F,0F,0F),
    scrollBehavior: TopAppBarScrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior(topAppBarState),
    floatingActionButton: @Composable () -> Unit = {},
    content: @Composable () -> Unit
) {
    Scaffold(
        modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
        topBar = { topBar(scrollBehavior) },
        floatingActionButton = floatingActionButton,
        contentWindowInsets = WindowInsets(0,0,0,0),
        content = {
            Box(modifier = Modifier.padding(it)) {
                content()
            }
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppTopBar(
    title: String,
    scrollBehavior: TopAppBarScrollBehavior,
    actions: @Composable RowScope.() -> Unit = {},
    colors: TopAppBarColors = TopAppBarDefaults.largeTopAppBarColors(),
    navigationIcon: ImageVector = Icons.AutoMirrored.Rounded.ArrowBack,
    onNavigationClick: (() -> Unit)? = null
) {
    LargeTopAppBar(
        title = { Text(title) },
        scrollBehavior = scrollBehavior,
        colors = colors,
        navigationIcon = {
            onNavigationClick?.let {
                IconButton(onClick = it) {
                    Icon(
                        imageVector = navigationIcon,
                        contentDescription = stringResource(R.string.action_back)
                    )
                }
            }
        },
        actions = actions
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppSmallTopBar(
    title: String,
    scrollBehavior: TopAppBarScrollBehavior,
    actions: @Composable RowScope.() -> Unit = {},
    colors: TopAppBarColors = TopAppBarDefaults.largeTopAppBarColors(),
    navigationIcon: ImageVector = Icons.AutoMirrored.Rounded.ArrowBack,
    onNavigationClick: (() -> Unit)? = null
) {
    TopAppBar(
        title = { Text(title) },
        scrollBehavior = scrollBehavior,
        colors = colors,
        navigationIcon = {
            onNavigationClick?.let {
                IconButton(onClick = it) {
                    Icon(
                        imageVector = navigationIcon,
                        contentDescription = stringResource(R.string.action_back)
                    )
                }
            }
        },
        actions = actions
    )
}