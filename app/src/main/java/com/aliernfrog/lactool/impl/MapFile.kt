package com.aliernfrog.lactool.impl

import android.content.Context
import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import coil3.imageLoader
import coil3.memory.MemoryCache
import com.aliernfrog.lactool.R
import com.aliernfrog.lactool.TAG
import com.aliernfrog.lactool.ui.viewmodel.MainViewModel
import com.aliernfrog.lactool.ui.viewmodel.MapsEditViewModel
import com.aliernfrog.lactool.ui.viewmodel.MapsMergeViewModel
import com.aliernfrog.lactool.ui.viewmodel.MapsViewModel
import com.aliernfrog.lactool.util.extension.showErrorToast
import com.aliernfrog.toptoast.state.TopToastState
import com.lazygeniouz.dfc.file.DocumentFileCompat
import io.github.aliernfrog.pftool_shared.data.MapActionResult
import io.github.aliernfrog.pftool_shared.enum.MapImportedState
import io.github.aliernfrog.pftool_shared.impl.FileWrapper
import io.github.aliernfrog.pftool_shared.impl.IMapFile
import io.github.aliernfrog.shared.di.getKoinInstance
import io.github.aliernfrog.shared.impl.ContextUtils
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

class MapFile(
    override val file: FileWrapper
): IMapFile, KoinComponent {
    val vm by inject<MapsViewModel>()
    val mapsEditViewModel by inject<MapsEditViewModel>() // TODO remove MapsEditViewModel dependency
    val mapsMergeViewModel by inject<MapsMergeViewModel>()
    val topToastState by inject<TopToastState>()
    override val contextUtils by inject<ContextUtils>()

    override val importedState = if (path.startsWith(vm.mapsDir)) MapImportedState.IMPORTED
    else if (path.startsWith(vm.exportedMapsDir)) MapImportedState.EXPORTED
    else MapImportedState.NONE

    private val thumbnailFileName = "$name.jpg"
    private var thumbnailFile = if (importedState != MapImportedState.IMPORTED) null
    else file.parentFile?.findFile(thumbnailFileName)
    override var thumbnailModel by mutableStateOf(
        if (importedState != MapImportedState.IMPORTED) null
        else thumbnailFile?.painterModel
    )

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

    override fun rename(
        newName: String
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

    override fun duplicate(
        context: Context,
        newName: String
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

    override fun import(
        context: Context,
        withName: String
    ): MapActionResult {
        if (importedState == MapImportedState.IMPORTED) return MapActionResult(successful = false)
        val mapsFile = vm.getMapsFile(context)
        val newFileName = "$withName.txt"
        if (mapsFile?.findFile(newFileName)?.exists() == true) return MapActionResult(
            successful = false,
            message = R.string.maps_alreadyExists
        )
        mapsFile?.createFile(newFileName)!!.copyFrom(file, context)
        return MapActionResult(
            successful = true,
            newFile = mapsFile.findFile(newFileName)
        )
    }

    override fun export(
        context: Context,
        withName: String
    ): MapActionResult {
        if (importedState == MapImportedState.EXPORTED) return MapActionResult(successful = false)
        val exportedMapsFile = vm.getExportedMapsFile(context)
        val newFileName = "$withName.txt"
        if (exportedMapsFile?.findFile(newFileName)?.exists() == true) return MapActionResult(
            successful = false,
            message = R.string.maps_alreadyExists
        )
        exportedMapsFile?.createFile(newFileName)!!.copyFrom(file, context)
        return MapActionResult(
            successful = true,
            newFile = exportedMapsFile.findFile(newFileName)
        )
    }

    override suspend fun exportToCustomLocation(
        context: Context,
        withName: String
    ): MapActionResult {
        val uri = getKoinInstance<MainViewModel>().safTxtFileCreator.createFile(suggestedName = withName)
            ?: return MapActionResult(
                successful = false,
                message = R.string.maps_exportCustomTarget_cancelled
            )
        file.inputStream(context).use { input ->
            context.contentResolver.openOutputStream(uri)!!.use { output ->
                input?.copyTo(output)
            }
        }
        return MapActionResult(
            successful = true,
            newFile = DocumentFileCompat.fromSingleUri(context, uri)?.let {
                FileWrapper(it)
            }
        )
    }

    override fun delete() {
        relatedFiles.plus(file).forEach {
            it.delete()
        }
    }

    override fun getThumbnailFile() = thumbnailFile

    fun setThumbnailFile(
        context: Context,
        file: FileWrapper
    ) {
        getThumbnailFile().let { found ->
            if (found?.exists() == true) found else this.file.parentFile!!.createFile(thumbnailFileName)
        }!!.copyFrom(file, context)
        thumbnailFile = this.file.parentFile!!.findFile(thumbnailFileName)
        thumbnailModel = thumbnailFile?.painterModel

        thumbnailModel?.toString()?.let { key ->
            val coilLoader = context.imageLoader
            coilLoader.memoryCache?.remove(MemoryCache.Key(key))
            coilLoader.diskCache?.remove(key)
        }
    }

    /**
     * Deletes thumbnail file of the map.
     */
    fun deleteThumbnailFile() {
        thumbnailFile?.delete()
        thumbnailFile = null
        thumbnailModel = null
    }

    override suspend fun runInIOThreadSafe(block: suspend () -> Unit) {
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
