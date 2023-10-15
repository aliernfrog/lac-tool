package com.aliernfrog.lactool.util.extension

import com.aliernfrog.lactool.util.staticutil.FileUtil
import com.lazygeniouz.dfc.file.DocumentFileCompat

val DocumentFileCompat.nameWithoutExtension
    get() = FileUtil.removeExtension(this.name)