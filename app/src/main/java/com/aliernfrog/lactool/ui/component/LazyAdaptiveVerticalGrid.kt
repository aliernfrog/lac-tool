package com.aliernfrog.lactool.ui.component

import android.app.Activity
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyGridScope
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.material3.windowsizeclass.ExperimentalMaterial3WindowSizeClassApi
import androidx.compose.material3.windowsizeclass.WindowWidthSizeClass
import androidx.compose.material3.windowsizeclass.calculateWindowSizeClass
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext

@Composable
fun LazyAdaptiveVerticalGrid(
    modifier: Modifier = Modifier,
    content: LazyGridScope.(maxLineSpan: Int) -> Unit
) {
    val maxLineSpan = getMaxLineSpan()
    LazyVerticalGrid(
        columns = GridCells.Fixed(maxLineSpan),
        modifier = modifier
    ) {
        content(maxLineSpan)
    }
}

@OptIn(ExperimentalMaterial3WindowSizeClassApi::class)
@Composable
private fun getMaxLineSpan(): Int {
    val context = LocalContext.current
    val windowSizeClass = calculateWindowSizeClass(context as Activity)
    return when (windowSizeClass.widthSizeClass) {
        WindowWidthSizeClass.Medium -> 4
        WindowWidthSizeClass.Expanded -> 5
        else -> 3
    }
}