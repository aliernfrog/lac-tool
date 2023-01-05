package com.aliernfrog.lactool.util.extension

import com.aliernfrog.lactool.data.LACMap

fun LACMap.resolveFile(): Any {
    return this.documentFile ?: this.file!!
}