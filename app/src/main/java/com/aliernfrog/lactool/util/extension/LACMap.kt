package com.aliernfrog.lactool.util.extension

import android.content.Context
import com.aliernfrog.lactool.data.LACMap
import com.aliernfrog.lactool.util.staticutil.FileUtil

fun LACMap.resolveFile(): Any {
    return this.documentFile ?: this.file!!
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
    this.details = result
    return result
}