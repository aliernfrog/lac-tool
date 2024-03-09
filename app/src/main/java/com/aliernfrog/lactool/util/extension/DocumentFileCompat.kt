package com.aliernfrog.lactool.util.extension

import com.aliernfrog.lactool.util.staticutil.FileUtil
import com.lazygeniouz.dfc.file.DocumentFileCompat

val DocumentFileCompat.nameWithoutExtension
    get() = FileUtil.removeExtension(this.name)

val DocumentFileCompat.size: Long
    get() {
        if (!exists()) return 0
        if (!isDirectory()) return length

        var size: Long = 0
        listFiles().forEach { file ->
            size += file.size
        }

        return size
    }