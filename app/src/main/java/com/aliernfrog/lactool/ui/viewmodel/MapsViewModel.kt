package com.aliernfrog.lactool.ui.viewmodel

import android.content.Context
import android.net.Uri
import androidx.compose.foundation.ScrollState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Delete
import androidx.compose.material.icons.rounded.Download
import androidx.compose.material.icons.rounded.Edit
import androidx.compose.material.icons.rounded.PriorityHigh
import androidx.compose.material.icons.rounded.Upload
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.SheetState
import androidx.compose.material3.TopAppBarState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.unit.Density
import androidx.lifecycle.ViewModel
import com.aliernfrog.lactool.R
import com.aliernfrog.lactool.data.LACMap
import com.aliernfrog.lactool.enum.MapImportedState
import com.aliernfrog.lactool.enum.PickMapSheetSegments
import com.aliernfrog.lactool.util.extension.cacheFile
import com.aliernfrog.lactool.util.extension.nameWithoutExtension
import com.aliernfrog.lactool.util.extension.resolveFile
import com.aliernfrog.lactool.util.extension.resolvePath
import com.aliernfrog.lactool.util.manager.PreferenceManager
import com.aliernfrog.lactool.util.staticutil.FileUtil
import com.aliernfrog.toptoast.enum.TopToastColor
import com.aliernfrog.toptoast.state.TopToastState
import com.lazygeniouz.dfc.file.DocumentFileCompat
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.koin.ext.getFullName
import java.io.File

@OptIn(ExperimentalMaterial3Api::class)
class MapsViewModel(
    context: Context,
    val topToastState: TopToastState,
    val prefs: PreferenceManager,
    private val mapsEditViewModel: MapsEditViewModel
) : ViewModel() {
    val pickMapSheetState = SheetState(skipPartiallyExpanded = false, Density(context))
    val topAppBarState = TopAppBarState(0F, 0F, 0F)
    val scrollState = ScrollState(0)

    private val mapsDir: String get() { return prefs.lacMapsDir }
    private val exportedMapsDir: String get() { return prefs.exportedMapsDir }
    private lateinit var mapsFile: DocumentFileCompat
    private lateinit var exportedMapsFile: DocumentFileCompat

    var importedMaps by mutableStateOf(emptyList<LACMap>())
    var exportedMaps by mutableStateOf(emptyList<LACMap>())
    var mapNameEdit by mutableStateOf("")
    var pendingMapDelete by mutableStateOf<String?>(null)
    var pickMapSheetSelectedSegment by mutableStateOf(PickMapSheetSegments.IMPORTED)

    var chosenMap by mutableStateOf<LACMap?>(null)

    fun chooseMap(map: Any?) {
        var mapToChoose: LACMap? = null
        when (map) {
            is File -> {
                val mapName = map.nameWithoutExtension
                if (map.exists()) mapToChoose = LACMap(
                    name = mapName,
                    fileName = map.name,
                    file = map
                )
                else fileDoesntExist()
            }
            is DocumentFileCompat -> {
                val mapName = FileUtil.removeExtension(map.name)
                if (map.exists()) mapToChoose = LACMap(
                    name = mapName,
                    fileName = map.name,
                    documentFile = map
                )
                else fileDoesntExist()
            }
            is LACMap -> {
                mapToChoose = map
            }
            null -> mapToChoose = null
            else -> throw IllegalArgumentException("Unhandled class: (${map::class.getFullName()})")
        }

        val mapPath = mapToChoose?.resolvePath() ?: ""
        chosenMap = mapToChoose?.copy(
            importedState = getMapImportedState(mapPath),
            thumbnailModel = getMapThumbnailModel(mapPath)
        )

        mapToChoose?.name?.let {
            mapNameEdit = it
        }
    }

    suspend fun renameChosenMap() {
        val newName = getMapNameEdit(false)
        val newNameTxt = "$newName.txt"
        val currentName = chosenMap?.name ?: return
        val output = mapsFile.findFile(newNameTxt)
        if (output != null && output.exists()) fileAlreadyExists()
        else withContext(Dispatchers.IO) {
            getChosenMapFiles().forEach { file ->
                val newFileName = file.name.replaceFirst(currentName, newName)
                file.renameTo(newFileName)
            }
            chooseMap(mapsFile.findFile(newNameTxt))
            topToastState.showToast(R.string.maps_rename_done, Icons.Rounded.Edit)
            fetchImportedMaps()
        }
    }

    suspend fun importChosenMap(context: Context) {
        val mapPath = when (val file = chosenMap?.resolveFile() ?: return) {
            is File -> file.absolutePath
            is DocumentFileCompat -> file.uri.cacheFile(context)?.absolutePath
            else -> null
        } ?: return
        val mapName = getMapNameEdit()
        var output = mapsFile.findFile(mapName)
        if (output?.exists() == true) fileAlreadyExists()
        else withContext(Dispatchers.IO) {
            output = mapsFile.createFile("", mapName) ?: return@withContext
            FileUtil.copyFile(mapPath, output ?: return@withContext, context)
            chooseMap(output)
            topToastState.showToast(R.string.maps_import_done, Icons.Rounded.Download)
            fetchImportedMaps()
        }
    }

    suspend fun exportChosenMap(context: Context) {
        val mapFile = chosenMap?.documentFile ?: return
        val outputName = getMapNameEdit()
        var output = exportedMapsFile.findFile(outputName)
        if (output?.exists() == true) fileAlreadyExists()
        else withContext(Dispatchers.IO) {
            output = exportedMapsFile.createFile("", outputName) ?: return@withContext
            FileUtil.copyFile(mapFile, output ?: return@withContext, context)
            chooseMap(output)
            topToastState.showToast(R.string.maps_export_done, Icons.Rounded.Upload)
            fetchExportedMaps()
        }
    }

    suspend fun editChosenMap(
        context: Context,
        onNavigateMapEditScreenRequest: () -> Unit
    ) {
        val map = chosenMap ?: return
        val mapFile = map.file ?: map.documentFile ?: return
        mapsEditViewModel.loadMap(mapFile, context)
        onNavigateMapEditScreenRequest()
    }

    suspend fun deleteChosenMap() {
        val map = chosenMap ?: return
        withContext(Dispatchers.IO) {
            map.file?.delete()
            map.documentFile?.delete()
            chooseMap(null)
            topToastState.showToast(R.string.maps_delete_done, Icons.Rounded.Delete)
            fetchAllMaps()
        }
    }

    fun getMapNameEdit(addTxtSuffix: Boolean = true): String {
        val suffix = if (addTxtSuffix) ".txt" else ""
        return mapNameEdit.ifBlank { chosenMap?.name }+suffix
    }

    private fun getMapImportedState(path: String): MapImportedState {
        return if (path.startsWith(mapsDir)) MapImportedState.IMPORTED
        else if (path.startsWith(exportedMapsDir)) MapImportedState.EXPORTED
        else MapImportedState.NONE
    }

    private fun getMapThumbnailModel(path: String): String? {
        val fileName = FileUtil.getFileName(path, removeExtension = true)
        return if (!path.startsWith(mapsDir)) null
        else mapsFile.findFile("$fileName.jpg")?.uri.toString()
    }

    private fun getChosenMapFiles(): List<DocumentFileCompat> {
        val chosenMapName = chosenMap?.name ?: return listOf()
        val list = mutableListOf<DocumentFileCompat>()
        val mapFile = mapsFile.findFile("${chosenMapName}.txt")
        val thumbnailFile = mapsFile.findFile("${chosenMapName}.jpg")
        val dataFile = mapsFile.findFile(chosenMapName)
        if (mapFile != null && mapFile.exists() && mapFile.isFile()) list.add(mapFile)
        if (thumbnailFile != null && thumbnailFile.exists() && thumbnailFile.isFile()) list.add(thumbnailFile)
        if (dataFile != null && dataFile.exists() && dataFile.isDirectory()) list.add(dataFile)
        return list
    }

    fun getMapsFile(context: Context): DocumentFileCompat {
        val isUpToDate = if (!::mapsFile.isInitialized) false
        else {
            val updatedPath = mapsFile.uri.resolvePath()
            val existingPath = Uri.parse(mapsDir).resolvePath()
            updatedPath == existingPath
        }
        if (isUpToDate) return mapsFile
        val treeUri = Uri.parse(mapsDir)
        mapsFile = DocumentFileCompat.fromTreeUri(context, treeUri)!!
        return mapsFile
    }

    fun getExportedMapsFile(context: Context): DocumentFileCompat {
        val isUpToDate = if (!::exportedMapsFile.isInitialized) false
        else {
            val updatedPath = exportedMapsFile.uri.resolvePath()
            val existingPath = Uri.parse(exportedMapsDir).resolvePath()
            updatedPath == existingPath
        }
        if (isUpToDate) return exportedMapsFile
        val treeUri = Uri.parse(exportedMapsDir)
        exportedMapsFile = DocumentFileCompat.fromTreeUri(context, treeUri)!!
        return exportedMapsFile
    }

    suspend fun fetchAllMaps() {
        fetchImportedMaps()
        fetchExportedMaps()
    }

    private suspend fun fetchImportedMaps() {
        withContext(Dispatchers.IO) {
            importedMaps = mapsFile.listFiles()
                .filter { it.isFile() && it.name.lowercase().endsWith(".txt") }
                .sortedBy { it.name.lowercase() }
                .map {
                    LACMap(
                        name = FileUtil.removeExtension(it.name),
                        fileName = it.name,
                        fileSize = it.length,
                        lastModified = it.lastModified,
                        documentFile = it,
                        thumbnailModel = getMapThumbnailModel("$mapsDir/${it.name}")
                    )
                }
        }
    }

    private suspend fun fetchExportedMaps() {
        withContext(Dispatchers.IO) {
            exportedMaps = exportedMapsFile.listFiles()
                .filter { it.isFile() && it.name.lowercase().endsWith(".txt") }
                .sortedBy { it.name.lowercase() }
                .map {
                    LACMap(
                        name = it.nameWithoutExtension,
                        fileName = it.name,
                        fileSize = it.length,
                        lastModified = it.lastModified,
                        documentFile = it
                    )
                }
        }
    }

    private fun fileAlreadyExists() {
        topToastState.showToast(R.string.maps_alreadyExists, Icons.Rounded.PriorityHigh, TopToastColor.ERROR)
    }

    private fun fileDoesntExist() {
        topToastState.showToast(R.string.warning_fileDoesntExist, Icons.Rounded.PriorityHigh, TopToastColor.ERROR)
    }
}