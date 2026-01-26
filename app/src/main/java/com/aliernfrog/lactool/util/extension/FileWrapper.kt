package com.aliernfrog.lactool.util.extension

import android.content.Context
import android.os.ParcelFileDescriptor
import com.aliernfrog.lactool.util.staticutil.FileUtil
import com.lazygeniouz.dfc.file.DocumentFileCompat
import io.github.aliernfrog.pftool_shared.data.ServiceFile
import io.github.aliernfrog.pftool_shared.impl.FileWrapper
import io.github.aliernfrog.pftool_shared.repository.ServiceFileRepository
import io.github.aliernfrog.shared.di.getKoinInstance
import java.io.File
import kotlin.io.outputStream

fun FileWrapper.writeFile(content: String, context: Context) {
    val serviceFileRepository = getKoinInstance<ServiceFileRepository>()
    var target = file
    if (exists()) {
        delete()
        target = parentFile?.createFile(name)?.file ?: return
    }
    when (target) {
        is File -> target.outputStream().use {
            FileUtil.writeFile(it, content)
        }
        is DocumentFileCompat -> context.contentResolver.openOutputStream(target.uri)?.use {
            FileUtil.writeFile(it, content)
        }
        is ServiceFile -> {
            val fd = serviceFileRepository.fileService.getFd(target.path)
            ParcelFileDescriptor.AutoCloseOutputStream(fd).use {
                FileUtil.writeFile(it, content)
            }
            fd.close()
        }
    }
}