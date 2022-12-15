package com.aliernfrog.lactool.util.staticutil

import android.content.ContentUris
import android.content.Context
import android.net.Uri
import android.os.Environment
import android.provider.DocumentsContract
import android.provider.MediaStore
import java.io.File

/*
This is a simplified and improved version of https://github.com/HBiSoft/PickiT
Only for local files, at least for now
*/

class UriToFileUtil {
    companion object {
        fun getRealFilePath(uri: Uri, context: Context): String? {
            var docId: String? = null
            try { docId = DocumentsContract.getDocumentId(uri) } catch (_: Exception) {}
            if (docId != null && docId.startsWith("msf")) {
                //was selected from download provider
                val fileName = getFileNameFromContentResolver(uri, context)
                val file = File("${Environment.getExternalStorageDirectory()}/Download/$fileName")
                if (file.exists()) {
                    return file.absolutePath
                } else {
                    try {
                        var fd: Int?
                        context.contentResolver.openFileDescriptor(uri, "r").use { fd = it?.fd }
                        val pid = android.os.Process.myPid()
                        val mediaFile = File("/proc/$pid/fd/$fd")
                        if (mediaFile.exists()) return mediaFile.absolutePath
                    } catch (e: Exception) {
                        return e.toString()
                    }
                }
            } else {
                //local file was selected
                val returnedPath = getRealPathFromUriApi19(uri, context)
                if (returnedPath != null) {
                    val file = File(returnedPath)
                    if (file.exists()) return returnedPath
                }
            }
            return null
        }

        private fun getFileNameFromContentResolver(uri: Uri, context: Context): String? {
            val projection = arrayOf(MediaStore.Files.FileColumns.DISPLAY_NAME)
            context.contentResolver.query(uri, projection, null, null, null)?.use { cursor ->
                if (cursor.moveToFirst()) {
                    val index = cursor.getColumnIndexOrThrow(MediaStore.Files.FileColumns.DISPLAY_NAME)
                    val string = cursor.getString(index)
                    cursor.close()
                    return string
                }
            }
            return null
        }

        private fun getDataColumn(context: Context, uri: Uri, selection: String?, selectionArgs: Array<String>?): String? {
            val column = "_data"
            val projection = arrayOf(column)
            try {
                context.contentResolver.query(uri, projection, selection, selectionArgs, null)?.use { cursor ->
                    if (cursor.moveToFirst()) {
                        val index = cursor.getColumnIndexOrThrow(column)
                        return cursor.getString(index)
                    }
                }
            } catch (_: Exception) {}
            return null
        }

        private fun getSubFolders(uri: Uri): String {
            val replaceChars = uri.toString().replace("%2F", "/").replace("%20", " ").replace("%3A",":")
            val bits = replaceChars.split("/")
            val sub5 = bits[bits.size - 2]
            val sub4 = bits[bits.size - 3]
            val sub3 = bits[bits.size - 4]
            val sub2 = bits[bits.size - 5]
            val sub1 = bits[bits.size - 6]
            return if (sub1 == "Download") "$sub2/$sub3/$sub4/$sub5/"
            else if (sub2 == "Download") "$sub3/$sub4/$sub5/"
            else if (sub3 == "Download") "$sub4/$sub5/"
            else if (sub4 == "Download") "$sub5/"
            else ""
        }

        private fun getRealPathFromUriApi19(uri: Uri, context: Context): String? {
            if (DocumentsContract.isDocumentUri(context, uri)) {
                if (uri.authority.equals("com.android.externalstorage.documents")) {
                    //external storage document
                    val docId = DocumentsContract.getDocumentId(uri)
                    val split = docId.split(":")
                    when (split[0]) {
                        "primary" -> {
                            return if (split.size > 1) "${Environment.getExternalStorageDirectory()}/${split[1]}"
                            else "${Environment.getExternalStorageDirectory()}/"
                        }
                        "home" -> {
                            return if (split.size > 1) "${Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS)}/${split[1]}"
                            else "${Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS)}/"
                        }
                    }
                } else if (uri.toString().contains("com.android.providers.downloads.documents/document/raw")) {
                    //raw downloads document
                    val fileName = getFileNameFromContentResolver(uri, context)
                    val subFolderName = getSubFolders(uri)
                    if (fileName != null) return "${Environment.getExternalStorageDirectory()}/Download/$subFolderName$fileName"
                    val id = DocumentsContract.getDocumentId(uri)
                    val contentUri = ContentUris.withAppendedId(Uri.parse("content://downloads/public_downloads"), id.toLong())
                    return getDataColumn(context, contentUri, null, null)
                } else if (uri.authority.equals("com.android.providers.downloads.documents")) {
                    //downloads document
                    val fileName = getFileNameFromContentResolver(uri, context)
                    if (fileName != null) return "${Environment.getExternalStorageDirectory()}/Download/$fileName"
                    var id = DocumentsContract.getDocumentId(uri)
                    if (id.startsWith("raw:")) {
                        id = id.replaceFirst("raw:","")
                        val file = File(id)
                        if (file.exists()) return id
                    }
                    if (id.startsWith("raw%3A%2F")) {
                        id = id.replaceFirst("raw%3A%2F","")
                        val file = File(id)
                        if (file.exists()) return id
                    }
                    val contentUri = ContentUris.withAppendedId(Uri.parse("content://downloads/public_downloads"), id.toLong())
                    return getDataColumn(context, contentUri, null, null)
                } else if (uri.authority.equals("com.android.providers.media.documents")) {
                    //media document
                    val docId = DocumentsContract.getDocumentId(uri)
                    val split = docId.split(";")
                    val contentUri: Uri? = when(split[0]) {
                        "image" -> MediaStore.Images.Media.EXTERNAL_CONTENT_URI
                        "video" -> MediaStore.Video.Media.EXTERNAL_CONTENT_URI
                        "audio" -> MediaStore.Audio.Media.EXTERNAL_CONTENT_URI
                        else -> null
                    }
                    val selection = "_id=?"
                    val selectionArgs = try { arrayOf(split[1]) } catch (_: Exception) { null }
                    if (contentUri != null) return getDataColumn(context, contentUri, selection, selectionArgs)
                }
            } else if (uri.scheme.equals("content")) {
                if (uri.authority.equals("com.google.android.apps.photos.content")) return uri.lastPathSegment
                return getDataColumn(context, uri, null, null)
            } else if (uri.scheme.equals("file")) {
                return uri.path
            }
            return null
        }
    }
}