package com.aliernfrog.lactool.data

import android.os.Parcelable
import com.aliernfrog.lactool.di.getKoinInstance
import com.aliernfrog.lactool.ui.viewmodel.ShizukuViewModel
import com.aliernfrog.lactool.util.staticutil.FileUtil
import kotlinx.parcelize.Parcelize

@Parcelize
data class ServiceFile(
    val name: String,
    val path: String,
    val parentPath: String?,
    val size: Long,
    val lastModified: Long,
    val isFile: Boolean
): Parcelable

val ServiceFile.nameWithoutExtension
    get() = FileUtil.removeExtension(this.name)

fun ServiceFile.delete() {
    val shizukuViewModel = getKoinInstance<ShizukuViewModel>()
    return shizukuViewModel.fileService!!.delete(path)
}

fun ServiceFile.exists(): Boolean {
    val shizukuViewModel = getKoinInstance<ShizukuViewModel>()
    return shizukuViewModel.fileService!!.exists(path)
}

fun ServiceFile.listFiles(): Array<ServiceFile>? {
    val shizukuViewModel = getKoinInstance<ShizukuViewModel>()
    return shizukuViewModel.fileService!!.listFiles(path)
}

fun ServiceFile.renameTo(newPath: String) {
    val shizukuViewModel = getKoinInstance<ShizukuViewModel>()
    shizukuViewModel.fileService!!.renameFile(path, newPath)
}

fun ServiceFile.mkdirs() {
    val shizukuViewModel = getKoinInstance<ShizukuViewModel>()
    shizukuViewModel.fileService!!.mkdirs(path)
}