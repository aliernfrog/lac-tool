package com.aliernfrog.lactool.util.staticutil

import android.content.Context
import android.content.Intent
import android.text.format.DateUtils
import androidx.core.content.FileProvider
import com.aliernfrog.lactool.R
import com.lazygeniouz.dfc.file.DocumentFileCompat
import java.io.File

class FileUtil {
    companion object {
        fun removeExtension(path: String): String {
            val extensionIndex = path.lastIndexOf(".")
            if (extensionIndex == -1) return path
            return path.substring(0, extensionIndex)
        }

        fun lastModifiedFromLong(lastModified: Long?, context: Context): String {
            val lastModifiedTime = lastModified ?: System.currentTimeMillis()
            return DateUtils.getRelativeDateTimeString(
                /* c = */ context,
                /* time = */ lastModifiedTime,
                /* minResolution = */ DateUtils.SECOND_IN_MILLIS,
                /* transitionResolution = */ DateUtils.DAY_IN_MILLIS,
                /* flags = */ 0
            ).toString()
        }

        fun copyFile(source: String, destination: DocumentFileCompat, context: Context) {
            val input = File(source).inputStream()
            val output = context.contentResolver.openOutputStream(destination.uri)
            if (output != null) input.copyTo(output)
            input.close()
            output?.close()
        }

        fun copyFile(source: DocumentFileCompat, destination: DocumentFileCompat, context: Context) {
            val input = context.contentResolver.openInputStream(source.uri)
            context.contentResolver.openOutputStream(destination.uri)?.use { output ->
                input?.copyTo(output)
            }
            input?.close()
        }

        fun shareFile(file: Any, context: Context, title: String = context.getString(R.string.action_share)) {
            val sharedFile = moveToSharedCache(file, context)
            val uri = FileProvider.getUriForFile(context, "${context.packageName}.provider", sharedFile)
            val intent = Intent(Intent.ACTION_SEND)
                .addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                .setDataAndType(uri, context.contentResolver.getType(uri))
                .putExtra(Intent.EXTRA_STREAM, uri)
            context.startActivity(Intent.createChooser(intent, title))
        }

        private fun moveToSharedCache(file: Any, context: Context): File {
            val fileName = when (file) {
                is DocumentFileCompat -> file.name
                is File -> file.name
                else -> throw IllegalArgumentException()
            }
            val inputStream = when(file) {
                is DocumentFileCompat -> context.contentResolver.openInputStream(file.uri)
                is File -> file.inputStream()
                else -> throw IllegalArgumentException()
            }
            val targetFile = File("${context.cacheDir.absolutePath}/shared/$fileName")
            targetFile.parentFile?.mkdirs()
            if (targetFile.isFile) targetFile.delete()
            val output = targetFile.outputStream()
            inputStream?.copyTo(output)
            inputStream?.close()
            output.close()
            return File(targetFile.absolutePath)
        }
    }
}