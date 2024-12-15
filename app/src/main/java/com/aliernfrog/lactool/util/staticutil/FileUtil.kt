package com.aliernfrog.lactool.util.staticutil

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Environment
import android.provider.DocumentsContract
import android.text.format.DateUtils
import androidx.core.content.FileProvider
import com.aliernfrog.lactool.R
import com.aliernfrog.lactool.impl.FileWrapper
import com.aliernfrog.lactool.util.extension.toPath
import java.io.File
import java.io.OutputStream

class FileUtil {
    companion object {
        fun removeExtension(path: String): String {
            val extensionIndex = path.lastIndexOf(".")
            if (extensionIndex == -1) return path
            return path.substring(0, extensionIndex)
        }

        fun getFilePath(path: String): String {
            return if (path.startsWith("/")) path
            else Uri.parse(path).toPath()
        }

        fun getUriForPath(path: String): Uri {
            return DocumentsContract.buildDocumentUri(
                "com.android.externalstorage.documents",
                "primary:"+path.removePrefix("${Environment.getExternalStorageDirectory()}/")
            )
        }

        fun getTreeUriForPath(path: String): Uri {
            return DocumentsContract.buildTreeDocumentUri(
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

        fun writeFile(outputStream: OutputStream, content: String) {
            val writer = outputStream.writer(Charsets.UTF_8)
            writer.write(content)
            writer.flush()
            writer.close()
        }

        fun openTextAsFile(text: String, context: Context) {
            val targetFile = File("${context.externalCacheDir}/shared/temptext.txt")
            targetFile.parentFile?.mkdirs()
            if (targetFile.isFile) targetFile.delete()
            val output = targetFile.outputStream()
            writeFile(output, text)
            val uri = FileProvider.getUriForFile(context, "${context.packageName}.provider", targetFile)
            val intent = Intent(Intent.ACTION_VIEW)
                .setDataAndType(uri, "text/plain")
                .addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            context.startActivity(intent)
        }

        fun shareFiles(vararg files: FileWrapper, context: Context, title: String = context.getString(R.string.action_share)) {
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

        private fun moveToSharedCache(file: FileWrapper, context: Context): File {
            val inputStream = file.inputStream(context)
            val targetFile = File("${context.externalCacheDir}/shared/${file.name}")
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