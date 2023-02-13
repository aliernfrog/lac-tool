package com.aliernfrog.lactool.data

import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf

data class PrefEditItem(
    val key: String,
    val default: String = "",
    val labelResourceId: Int? = null,
    val mutableValue: MutableState<String> = mutableStateOf("")
)
