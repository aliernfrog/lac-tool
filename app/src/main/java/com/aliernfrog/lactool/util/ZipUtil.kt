package com.aliernfrog.lactool.util

import android.content.Context
import com.lazygeniouz.filecompat.file.DocumentFileCompat
import java.io.BufferedOutputStream
import java.io.FileOutputStream
import java.util.*
import java.util.zip.ZipEntry
import java.util.zip.ZipFile
import java.util.zip.ZipOutputStream

class ZipUtil {
    companion object {
        private val allowedMapFiles = arrayOf("colormap.jpg","heightmap.jpg","map.txt","thumbnail.jpg")

        fun zipMap(folder: DocumentFileCompat, zipPath: String, context: Context) {
            ZipOutputStream(BufferedOutputStream(FileOutputStream(zipPath))).use { zos ->
                val files = folder.listFiles().filter { it.isFile() && allowedMapFiles.contains(FileUtil.getFileName(it.name).lowercase(Locale.ROOT)) }
                files.forEach { file ->
                    val entry = ZipEntry(file.name)
                    zos.putNextEntry(entry)
                    context.contentResolver.openInputStream(file.uri)?.use { it.copyTo(zos) }
                }
            }
        }

        fun unzipMap(zipPath: String, destDocumentFile: DocumentFileCompat, context: Context) {
            val zip = ZipFile(zipPath)
            val entries = zip.entries().asSequence().filter { allowedMapFiles.contains(FileUtil.getFileName(it.name).lowercase(Locale.ROOT)) }
            entries.forEach { entry ->
                var entryName = entry.name
                if (entryName.contains("/")) entryName = FileUtil.getFileName(entry.name)
                var outputFile = destDocumentFile.findFile(entryName)
                if (outputFile == null) outputFile = destDocumentFile.createFile("", entryName)
                val input = zip.getInputStream(entry)
                val output = context.contentResolver.openOutputStream(outputFile?.uri!!)
                input.copyTo(output!!)
                input.close()
                output.close()
            }
        }
    }
}