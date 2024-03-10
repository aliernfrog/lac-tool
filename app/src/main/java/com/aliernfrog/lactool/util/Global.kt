package com.aliernfrog.lactool.util

import com.aliernfrog.lactool.data.ServiceFile
import com.aliernfrog.lactool.util.extension.size
import java.io.File

fun getServiceFile(file: File): ServiceFile {
    return ServiceFile(
        name = file.name,
        path = file.path,
        parentPath = file.parent,
        size = file.size,
        lastModified = file.lastModified(),
        isFile = file.isFile
    )
}