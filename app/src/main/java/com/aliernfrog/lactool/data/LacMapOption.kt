package com.aliernfrog.lactool.data

import androidx.compose.runtime.MutableState
import com.aliernfrog.lactool.enum.LACMapOptionType

data class LacMapOption(
    val type: LACMapOptionType,
    val label: String,
    val value: MutableState<String>,
    val line: Int
)
