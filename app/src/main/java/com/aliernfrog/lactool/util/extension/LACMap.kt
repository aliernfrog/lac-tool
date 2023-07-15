package com.aliernfrog.lactool.util.extension

import android.content.Context
import com.aliernfrog.lactool.data.LACMap
import com.aliernfrog.lactool.util.staticutil.FileUtil
import com.lazygeniouz.dfc.file.DocumentFileCompat
import java.io.File

fun LACMap.resolveFile(): Any {
    return this.documentFile ?: this.file!!
}

fun LACMap.resolvePath(mapsDir: String): String? {
    return when (val file = this.resolveFile()) {
        is File -> file.absolutePath
        // Right now, only imported maps can be DocumentFileCompat
        is DocumentFileCompat -> "$mapsDir/${this.fileName}"
        else -> null
    }
}

fun LACMap.getDetails(context: Context): String? {
    val details = mutableListOf<String>()
    if (this.fileSize != null) details.add(
        "${this.fileSize/1024} KB"
    )
    if (this.lastModified != null) details.add(
        FileUtil.lastModifiedFromLong(this.lastModified, context)
    )
    val result = if (details.isEmpty()) null else details.joinToString(" | ")
    this.details.value = result
    return result
}