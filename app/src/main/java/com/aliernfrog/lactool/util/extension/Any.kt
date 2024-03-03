package com.aliernfrog.lactool.util.extension

import com.aliernfrog.lactool.di.getKoinInstance
import com.aliernfrog.lactool.util.manager.ContextUtils

fun Any.resolveString(): String {
    val contextUtils = getKoinInstance<ContextUtils>()
    return when (this) {
        is String -> this
        is Int -> contextUtils.getString(this)
        else -> throw IllegalArgumentException("resolveString: unexpected class")
    }
}