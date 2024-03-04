package com.aliernfrog.lactool.service

import com.aliernfrog.lactool.IFileService
import com.aliernfrog.lactool.data.ServiceFile
import com.aliernfrog.lactool.util.getServiceFile
import com.aliernfrog.lactool.util.staticutil.FileUtil
import java.io.File
import kotlin.system.exitProcess

class FileService : IFileService.Stub() {
    override fun destroy() {
        exitProcess(0)
    }

    override fun exit() {
        destroy()
    }

    override fun copy(sourcePath: String, targetPath: String) {
        val source = File(sourcePath)
        val output = File(targetPath)
        if (source.isFile) source.inputStream().use { inputStream ->
            output.outputStream().use { outputStream ->
                inputStream.copyTo(outputStream)
            }
        }
        else FileUtil.copyDirectory(source, output)
    }

    override fun delete(path: String) {
        File(path).deleteRecursively()
    }

    override fun exists(path: String): Boolean {
        return File(path).exists()
    }

    override fun getByteArray(path: String): ByteArray {
        return File(path).readBytes()
    }

    override fun getFile(path: String): ServiceFile {
        return getServiceFile(File(path))
    }

    override fun listFiles(path: String): Array<ServiceFile> {
        val files = File(path).listFiles() ?: emptyArray()
        return files.map {
            getServiceFile(it)
        }.toTypedArray()
    }

    override fun renameFile(oldPath: String, newPath: String) {
        File(oldPath).renameTo(File(newPath))
    }
}