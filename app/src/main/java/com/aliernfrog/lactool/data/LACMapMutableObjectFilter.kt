package com.aliernfrog.lactool.data

import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf

data class LACMapMutableObjectFilter(
    val query: MutableState<String> = mutableStateOf(""),
    val caseSensitive: MutableState<Boolean> = mutableStateOf(true),
    val exactMatch: MutableState<Boolean> = mutableStateOf(true),
    val labelStringId: Int? = null
)