package com.aliernfrog.lactool.util.extension

import com.lazygeniouz.dfc.file.DocumentFileCompat
import io.github.aliernfrog.pftool_shared.util.staticutil.PFToolSharedUtil

val DocumentFileCompat.nameWithoutExtension
    get() = PFToolSharedUtil.removeExtension(this.name)

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