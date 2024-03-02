package com.aliernfrog.lactool.util.staticutil

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Environment
import android.provider.DocumentsContract
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

        fun getUriForPath(path: String): Uri {
            return DocumentsContract.buildDocumentUri(
                "com.android.externalstorage.documents",
                "primary:"+path.removePrefix("${Environment.getExternalStorageDirectory()}/")
            )
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

        fun copyFile(source: File, target: File) {
            source.inputStream().use { inputStream ->
                target.outputStream().use { outputStream ->
                    inputStream.copyTo(outputStream)
                }
            }
        }

        fun copyFile(source: DocumentFileCompat, target: DocumentFileCompat, context: Context) {
            context.contentResolver.openInputStream(source.uri)?.use { inputStream ->
                context.contentResolver.openOutputStream(target.uri)?.use { outputStream ->
                    inputStream.copyTo(outputStream)
                }
            }
        }

        fun copyDirectory(source: File, target: File) {
            if (!target.isDirectory) target.mkdirs()
            source.listFiles()!!.forEach { file ->
                val targetFile = File("${target.absolutePath}/${file.name}")
                if (file.isDirectory) copyDirectory(file, targetFile)
                else file.inputStream().use { inputStream ->
                    targetFile.outputStream().use { outputStream ->
                        inputStream.copyTo(outputStream)
                    }
                }
            }
        }

        fun copyDirectory(source: DocumentFileCompat, target: DocumentFileCompat) {
            source.listFiles().forEach { file ->
                val targetFile = if (file.isDirectory()) target.createDirectory(file.name)
                else target.createFile("", file.name)
                if (file.isDirectory()) copyDirectory(file, targetFile!!)
                else file.copyTo(targetFile!!.uri)
            }
        }

        fun shareFiles(vararg files: Any, context: Context, title: String = context.getString(R.string.action_share)) {
            val isSingle = files.size <= 1
            val sharedFileUris = files.map {
                FileProvider.getUriForFile(context, "${context.packageName}.provider", moveToSharedCache(it, context))
            }
            val firstUri = sharedFileUris.first()
            val intent = Intent(
                if (sharedFileUris.size > 1) Intent.ACTION_SEND_MULTIPLE else Intent.ACTION_SEND
            )
                .addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                .setDataAndType(firstUri, context.contentResolver.getType(firstUri))
            if (isSingle) intent.putExtra(Intent.EXTRA_STREAM, firstUri)
            else intent.putExtra(Intent.EXTRA_STREAM, ArrayList(sharedFileUris))
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