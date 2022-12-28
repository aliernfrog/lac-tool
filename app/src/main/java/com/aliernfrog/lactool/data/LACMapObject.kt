package com.aliernfrog.lactool.data

import com.aliernfrog.lactool.enum.LACOldObject

data class LACMapObject(
    val line: String,
    val lineNumber: Int,
    val canReplaceWith: LACOldObject? = null
)
