package com.aliernfrog.lactool.util.extension

import com.aliernfrog.lactool.data.LACMap
import java.io.File

fun LACMap.resolveFile(): Any {
    return this.documentFile ?: File(this.filePath)
}