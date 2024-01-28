package com.aliernfrog.lactool.impl

import android.content.Context
import android.util.Log
import com.aliernfrog.lactool.R
import com.aliernfrog.lactool.TAG
import com.aliernfrog.lactool.data.MapActionResult
import com.aliernfrog.lactool.enum.MapImportedState
import com.aliernfrog.lactool.ui.viewmodel.MapsEditViewModel
import com.aliernfrog.lactool.ui.viewmodel.MapsMergeViewModel
import com.aliernfrog.lactool.ui.viewmodel.MapsViewModel
import com.aliernfrog.lactool.util.extension.nameWithoutExtension
import com.aliernfrog.lactool.util.extension.showErrorToast
import com.aliernfrog.lactool.util.manager.ContextUtils
import com.aliernfrog.lactool.util.staticutil.FileUtil
import com.aliernfrog.toptoast.state.TopToastState
import com.lazygeniouz.dfc.file.DocumentFileCompat
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import java.io.File

class MapFile(
    /**
     * Map file. Can be a [File] or [DocumentFileCompat].
     */
    val file: Any
): KoinComponent {
    val mapsViewModel by inject<MapsViewModel>()
    val mapsEditViewModel by inject<MapsEditViewModel>()
    val mapsMergeViewModel by inject<MapsMergeViewModel>()
    val topToastState by inject<TopToastState>()
    private val contextUtils by inject<ContextUtils>()

    /**
     * Name of the map.
     */
    val name: String = when (file) {
        is File -> if (file.isFile) file.nameWithoutExtension else file.name
        is DocumentFileCompat -> if (file.isFile()) file.nameWithoutExtension else file.name
        else -> throw IllegalArgumentException("Unknown class for a map. Supply File or DocumentFileCompat.")
    }

    /**
     * Name of the map file.
     */
    private val fileName: String = when (file) {
        is File -> file.name
        is DocumentFileCompat -> file.name
        else -> ""
    }

    /**
     * Path of the map. Can be a [File] path or uri.
     */
    val path: String = when (file) {
        is File -> file.absolutePath
        is DocumentFileCompat -> file.uri.toString()
        else -> ""
    }

    /**
     * Size of the map file.
     */
    val size: Long = when (file) {
        is File -> file.length()
        is DocumentFileCompat -> file.length
        else -> -1
    }

    /**
     * Last modified time of the map.
     */
    val lastModified: Long = when (file) {
        is File -> file.lastModified()
        is DocumentFileCompat -> file.lastModified
        else -> -1
    }

    /**
     * [MapImportedState] of the map.
     */
    val importedState: MapImportedState = if (path.startsWith(mapsViewModel.mapsDir)) MapImportedState.IMPORTED
    else if (path.startsWith(mapsViewModel.exportedMapsDir)) MapImportedState.EXPORTED
    else MapImportedState.NONE

    /**
     * Thumbnail model of the map.
     */
    val thumbnailModel: String? = if (importedState != MapImportedState.IMPORTED) null else when (file) {
        is File -> (file.parent?.plus("/") ?: "")+name+".jpg"
        is DocumentFileCompat -> file.parentFile?.findFile("$name.jpg")?.uri?.toString()
        else -> null
    }

    /**
     * Files related to the map (thumbnail file, data folder).
     */
    private val relatedFiles: List<Any>
        get() {
            val files: MutableList<Any> = mutableListOf()
            if (importedState != MapImportedState.IMPORTED) return files
            listOf(
                name, // Folder containing inventory data etc.
                "$name.jpg" // Thumbnail file
            ).forEach { relatedFileName ->
                @Suppress("IMPLICIT_CAST_TO_ANY")
                when (file) {
                    is File -> {
                        val check = File("${file.parent?.plus("/") ?: ""}$relatedFileName")
                        if (check.exists()) check else null
                    }
                    is DocumentFileCompat -> file.parentFile?.findFile(relatedFileName)
                    else -> throw IllegalArgumentException("File class was somehow unknown")
                }?.let {
                    files.add(it)
                }
            }
            return files
        }

    /**
     * Details of the map. Includes size (KB) and modified time.
     */
    val details: String = contextUtils.stringFunction { context ->
        "${size / 1024} KB | ${FileUtil.lastModifiedFromLong(this.lastModified, context)}"
    }

    /**
     * Renames the map.
     */
    fun rename(
        newName: String = resolveMapNameInput()
    ): MapActionResult {
        val newFileName = fileName.replaceFirst(name, newName)
        val newFile: Any = when (file) {
            is File -> {
                val output = File((file.parent?.plus("/") ?: "") + newFileName)
                if (output.exists()) return MapActionResult(
                    successful = false,
                    messageId = R.string.maps_alreadyExists
                )
                file.renameTo(output)
                File(output.absolutePath)
            }
            is DocumentFileCompat -> {
                if (file.parentFile?.findFile(newFileName)?.exists() == true) return MapActionResult(
                    successful = false,
                    messageId = R.string.maps_alreadyExists
                )
                file.renameTo(newFileName)
                file.parentFile!!.findFile(newFileName)!!
            }
            else -> throw IllegalArgumentException("File class was somehow unknown")
        }
        relatedFiles.forEach { relatedFile ->
            when (relatedFile) {
                is File -> relatedFile.renameTo(
                    File("${relatedFile.parent?.plus("/") ?: ""}${relatedFile.name.replace(name, newName)}")
                )
                is DocumentFileCompat -> relatedFile.renameTo(relatedFile.name.replace(name, newName))
            }
        }
        return MapActionResult(
            successful = true,
            newFile = newFile
        )
    }

    /**
     * Duplicates the map.
     */
    fun duplicate(
        context: Context,
        newName: String = mapsViewModel.resolveMapNameInput()
    ): MapActionResult {
        val newFileName = fileName.replaceFirst(name, newName)
        val newFile: Any = when (file) {
            is File -> {
                val output = File((file.parent?.plus("/") ?: "") + newFileName)
                if (output.exists()) return MapActionResult(
                    successful = false,
                    messageId = R.string.maps_alreadyExists
                )
                file.inputStream().use { inputStream ->
                    output.outputStream().use { outputStream ->
                        inputStream.copyTo(outputStream)
                    }
                }
                File(output.absolutePath)
            }
            is DocumentFileCompat -> {
                if (file.parentFile!!.findFile(newFileName)?.exists() == true) return MapActionResult(
                    successful = false,
                    messageId = R.string.maps_alreadyExists
                )
                val output = file.parentFile!!.createFile("", newFileName)!!
                context.contentResolver.openInputStream(file.uri)?.use { inputStream ->
                    context.contentResolver.openOutputStream(output.uri)?.use { outputStream ->
                        inputStream.copyTo(outputStream)
                    }
                }
                file.parentFile!!.findFile(newFileName)!!
            }
            else -> throw IllegalArgumentException("File class was somehow unknown")
        }
        relatedFiles.forEach { relatedFile -> when (relatedFile) {
            is File -> {
                val outputName = relatedFile.name.replaceFirst(name, newName)
                val output = File((relatedFile.parent?.plus("/") ?: "") + outputName)
                if (output.exists()) output.deleteRecursively()
                if (relatedFile.isFile) relatedFile.copyTo(output)
                else FileUtil.copyDirectory(relatedFile, output)
            }
            is DocumentFileCompat -> {
                val outputName = relatedFile.name.replaceFirst(name, newName)
                val parentFile = relatedFile.parentFile!!
                val check = parentFile.findFile(outputName)
                if (check?.exists() == true) check.delete()
                if (relatedFile.isFile()) relatedFile.copyTo(parentFile.createFile("", outputName)!!.uri)
                else FileUtil.copyDirectory(relatedFile, parentFile.createDirectory(outputName)!!)
            }
        } }
        return MapActionResult(
            successful = true,
            newFile = newFile
        )
    }

    /**
     * Imports the map.
     */
    fun import(
        context: Context,
        withName: String = resolveMapNameInput()
    ): MapActionResult {
        if (importedState == MapImportedState.IMPORTED) return MapActionResult(successful = false)
        val newFileName = "$withName.txt"
        if (mapsViewModel.mapsFile.findFile(newFileName)?.exists() == true) return MapActionResult(
            successful = false,
            messageId = R.string.maps_alreadyExists
        )
        when (file) {
            is File -> file.inputStream()
            is DocumentFileCompat -> context.contentResolver.openInputStream(file.uri)
            else -> throw IllegalArgumentException("File class was somehow unknown")
        }!!.use { inputStream ->
            val newFile = mapsViewModel.mapsFile.createFile("", newFileName)
            context.contentResolver.openOutputStream(newFile!!.uri)!!.use { outputStream ->
                inputStream.copyTo(outputStream)
            }
        }
        return MapActionResult(
            successful = true,
            newFile = mapsViewModel.mapsFile.findFile(newFileName)
        )
    }

    /**
     * Exports the map.
     */
    fun export(
        context: Context,
        withName: String = resolveMapNameInput()
    ): MapActionResult {
        if (importedState == MapImportedState.EXPORTED) return MapActionResult(successful = false)
        val newFileName = "$withName.txt"
        if (mapsViewModel.exportedMapsFile.findFile(newFileName)?.exists() == true) return MapActionResult(
            successful = false,
            messageId = R.string.maps_alreadyExists
        )
        when (file) {
            is File -> file.inputStream()
            is DocumentFileCompat -> context.contentResolver.openInputStream(file.uri)
            else -> throw IllegalArgumentException("File class was somehow unknown")
        }!!.use { inputStream ->
            val newFile = mapsViewModel.exportedMapsFile.createFile("", newFileName)
            context.contentResolver.openOutputStream(newFile!!.uri)!!.use { outputStream ->
                inputStream.copyTo(outputStream)
            }
        }
        return MapActionResult(
            successful = true,
            newFile = mapsViewModel.exportedMapsFile.findFile(newFileName)
        )
    }

    /**
     * Deletes the map without confirmation.
     */
    fun delete() {
        relatedFiles.plus(file).forEach {
            when (it) {
                is File -> it.delete()
                is DocumentFileCompat -> it.delete()
            }
        }
    }

    /**
     * Returns the user-provided map name if this map is chosen.
     */
    fun resolveMapNameInput(): String {
        return if (mapsViewModel.chosenMap?.path == path) mapsViewModel.resolveMapNameInput() else name
    }

    suspend fun runInIOThreadSafe(block: () -> Unit) {
        withContext(Dispatchers.IO) {
            try {
                block()
            } catch (e: Exception) {
                topToastState.showErrorToast()
                Log.e(TAG, this.toString(), e)
            }
        }
    }
}