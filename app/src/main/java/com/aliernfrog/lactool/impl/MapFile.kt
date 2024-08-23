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
import com.aliernfrog.lactool.util.extension.showErrorToast
import com.aliernfrog.lactool.util.manager.ContextUtils
import com.aliernfrog.lactool.util.staticutil.FileUtil
import com.aliernfrog.toptoast.state.TopToastState
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import java.io.File

class MapFile(
    /**
     * Map file.
     */
    val file: FileWrapper
): KoinComponent {
    val mapsViewModel by inject<MapsViewModel>()
    val mapsEditViewModel by inject<MapsEditViewModel>()
    val mapsMergeViewModel by inject<MapsMergeViewModel>()
    val topToastState by inject<TopToastState>()
    private val contextUtils by inject<ContextUtils>()

    /**
     * Name of the map.
     */
    val name: String = file.nameWithoutExtension

    /**
     * Name of the map file.
     */
    private val fileName: String = file.name

    /**
     * Name of the map thumbnail file. Does not check if it exists.
     */
    private val thumbnailFileName = "$name.jpg"

    /**
     * Path of the map. Can be a [File] path or uri.
     */
    val path: String = file.path

    /**
     * Size of the map file.
     */
    val size: Long = file.size

    /**
     * Last modified time of the map.
     */
    val lastModified: Long = file.lastModified

    /**
     * [MapImportedState] of the map.
     */
    val importedState: MapImportedState = if (path.startsWith(mapsViewModel.mapsDir)) MapImportedState.IMPORTED
    else if (path.startsWith(mapsViewModel.exportedMapsDir)) MapImportedState.EXPORTED
    else MapImportedState.NONE

    /**
     * Thumbnail model of the map.
     */
    var thumbnailModel: Any? = getThumbnailFile()?.painterModel
        private set

    /**
     * Files related to the map (thumbnail file, data folder).
     */
    private val relatedFiles: List<FileWrapper>
        get() {
            val files: MutableList<FileWrapper> = mutableListOf()
            if (importedState != MapImportedState.IMPORTED) return files
            listOf(
                name, // Folder containing inventory data etc.
                thumbnailFileName // Thumbnail file
            ).forEach { relatedFileName ->
                file.parentFile?.findFile(relatedFileName)?.let {
                    if (it.exists()) files.add(it)
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
        val outputName = fileName.replaceFirst(name, newName)
        if (file.parentFile?.findFile(outputName)?.exists() == true) return MapActionResult(
            successful = false,
            message = R.string.maps_alreadyExists
        )
        relatedFiles.plus(file).forEach { relatedFile ->
            val newFileName = relatedFile.name.replaceFirst(name, newName)
            relatedFile.rename(newFileName)
        }
        return MapActionResult(
            successful = true,
            newFile = file.parentFile!!.findFile(outputName)
        )
    }

    /**
     * Duplicates the map.
     */
    fun duplicate(
        context: Context,
        newName: String = mapsViewModel.resolveMapNameInput()
    ): MapActionResult {
        val outputName = fileName.replaceFirst(name, newName)
        if (file.parentFile?.findFile(outputName)?.exists() == true) return MapActionResult(
            successful = false,
            message = R.string.maps_alreadyExists
        )
        relatedFiles.plus(file).forEach { relatedFile ->
            val newFileName = relatedFile.name.replaceFirst(name, newName)
            val relatedFileOutput = if (relatedFile.isFile) file.parentFile!!.createFile(newFileName)
            else file.parentFile!!.createDirectory(newFileName)
            relatedFile.copyTo(relatedFileOutput!!, context)
        }
        return MapActionResult(
            successful = true,
            newFile = file.parentFile!!.findFile(outputName)
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
            message = R.string.maps_alreadyExists
        )
        mapsViewModel.mapsFile.createFile(newFileName)!!.copyFrom(file, context)
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
            message = R.string.maps_alreadyExists
        )
        mapsViewModel.exportedMapsFile.createFile(newFileName)!!.copyFrom(file, context)
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
            it.delete()
        }
    }

    /**
     * Returns thumbnail file of the map if exists, null otherwise.
     */
    private fun getThumbnailFile(): FileWrapper? {
        return file.parentFile?.findFile(thumbnailFileName)
    }

    /**
     * Sets thumbnail file of the map to [file].
     */
    fun setThumbnailFile(
        context: Context,
        file: FileWrapper
    ) {
        val parentFile = this.file.parentFile!!
        val thumbnailFile = parentFile.findFile(thumbnailFileName).let { found ->
            if (found?.exists() == true) found else parentFile.createFile(thumbnailFileName)
        }
        thumbnailFile!!.copyFrom(file, context)
        thumbnailModel = getThumbnailFile()?.painterModel
    }

    /**
     * Deletes thumbnail file of the map.
     */
    fun deleteThumbnailFile() {
        file.parentFile?.findFile(thumbnailFileName)!!.delete()
        thumbnailModel = null
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
