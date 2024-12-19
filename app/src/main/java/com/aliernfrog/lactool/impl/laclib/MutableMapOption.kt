package com.aliernfrog.lactool.impl.laclib

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.aliernfrog.laclib.data.LACMapOption

class MutableMapOption(option: LACMapOption) {
    val type = option.type
    val label = option.label
    var value by mutableStateOf(option.value)
    val line = option.line

    fun toImmutable() = LACMapOption(
        type = type,
        label = label,
        value = value,
        line = line
    )
}