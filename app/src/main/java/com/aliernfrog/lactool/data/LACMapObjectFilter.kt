package com.aliernfrog.lactool.data

import androidx.compose.runtime.MutableState

data class LACMapObjectFilter(
    val query: MutableState<String>,
    val caseSensitive: MutableState<Boolean>,
    val exactMatch: MutableState<Boolean>,
    val labelStringId: Int? = null
)