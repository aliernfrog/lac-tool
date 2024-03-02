package com.aliernfrog.lactool.util.extension

import com.aliernfrog.lactool.di.get
import com.aliernfrog.lactool.util.manager.ContextUtils

fun Any.resolveString(): String {
    val contextUtils = get<ContextUtils>()
    return when (this) {
        is String -> this
        is Int -> contextUtils.getString(this)
        else -> throw IllegalArgumentException("resolveString: unexpected class")
    }
}