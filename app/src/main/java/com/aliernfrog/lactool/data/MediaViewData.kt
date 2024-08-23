package com.aliernfrog.lactool.data

import androidx.compose.runtime.Composable

data class MediaViewData(
    val model: Any?,
    val title: String? = null,
    val zoomEnabled: Boolean = true,
    val options: (@Composable () -> Unit)? = null
)