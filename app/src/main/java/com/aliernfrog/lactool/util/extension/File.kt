package com.aliernfrog.lactool.util.extension

import java.io.File

val File.size: Long
    get() {
        if (!exists()) return 0
        if (!isDirectory) return length()

        var size: Long = 0
        listFiles()?.forEach { file ->
            size += file.size
        }

        return size
    }