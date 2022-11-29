package com.aliernfrog.lactool.data

import com.aliernfrog.lactool.enum.LACMapOptionType

data class LacMapOption(
    val type: LACMapOptionType,
    val label: String,
    val value: String
)
