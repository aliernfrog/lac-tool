package com.aliernfrog.lactool.data

import androidx.compose.runtime.MutableState
import com.aliernfrog.lactool.enum.LACMapOptionType

data class LACMapOption(
    val type: LACMapOptionType,
    val label: String,
    val value: MutableState<String>,
    val line: Int
)
