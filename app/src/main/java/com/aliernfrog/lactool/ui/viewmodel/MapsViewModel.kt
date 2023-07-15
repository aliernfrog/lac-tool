package com.aliernfrog.lactool.ui.viewmodel

import android.content.Context
import androidx.compose.foundation.ScrollState
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.ModalBottomSheetState
import androidx.compose.material.ModalBottomSheetValue
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Delete
import androidx.compose.material.icons.rounded.Download
import androidx.compose.material.icons.rounded.Edit
import androidx.compose.material.icons.rounded.PriorityHigh
import androidx.compose.material.icons.rounded.Upload
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.TopAppBarState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.unit.Density
import androidx.lifecycle.ViewModel
import com.aliernfrog.lactool.R
import com.aliernfrog.lactool.data.LACMap
import com.aliernfrog.lactool.enum.MapImportedState
import com.aliernfrog.lactool.util.extension.resolvePath
import com.aliernfrog.lactool.util.manager.PreferenceManager
import com.aliernfrog.lactool.util.staticutil.FileUtil
import com.aliernfrog.lactool.util.staticutil.GeneralUtil
import com.aliernfrog.toptoast.enum.TopToastColor
import com.aliernfrog.toptoast.state.TopToastState
import com.lazygeniouz.dfc.file.DocumentFileCompat
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File

@OptIn(ExperimentalMaterialApi::class, ExperimentalMaterial3Api::class)
class MapsViewModel(
    context: Context,
    val topToastState: TopToastState,
    val prefs: PreferenceManager,
    private val mapsEditViewModel: MapsEditViewModel
) : ViewModel() {
    val pickMapSheetState = ModalBottomSheetState(initialValue = ModalBottomSheetValue.Hidden, density = Density(context))
    val topAppBarState = TopAppBarState(0F, 0F, 0F)
    val scrollState = ScrollState(0)

    val mapsDir = prefs.lacMapsDir
    private val exportedMapsDir = prefs.exportedMapsDir
    private lateinit var mapsFile: DocumentFileCompat
    private val exportedMapsFile = File(exportedMapsDir)

    var importedMaps by mutableStateOf(emptyList<LACMap>())
    var exportedMaps by mutableStateOf(emptyList<LACMap>())
    var mapNameEdit by mutableStateOf("")
    var pendingMapDelete by mutableStateOf<String?>(null)

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
            else -> mapToChoose = null
        }

        val mapPath = mapToChoose?.resolvePath(mapsDir) ?: ""
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
        val mapPath = chosenMap?.file?.absolutePath ?: return
        val mapName = getMapNameEdit()
        var output = mapsFile.findFile(mapNameEdit)
        if (output != null && output.exists()) fileAlreadyExists()
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
        val output = File("${exportedMapsDir}/${getMapNameEdit()}")
        if (output.exists()) fileAlreadyExists()
        else withContext(Dispatchers.IO) {
            if (output.parentFile?.isDirectory != true) output.parentFile?.mkdirs()
            FileUtil.copyFile(mapFile, output.absolutePath, context)
            chooseMap(map = output)
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
            if (map.documentFile != null) {
                getChosenMapFiles().forEach { it.delete() }
                fetchImportedMaps()
            } else {
                map.file?.delete()
                fetchExportedMaps()
            }
            chooseMap(null)
            topToastState.showToast(R.string.maps_delete_done, Icons.Rounded.Delete)
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
        if (!::mapsFile.isInitialized)
            mapsFile = GeneralUtil.getDocumentFileFromPath(mapsDir, context)
        return mapsFile
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
                        file = null,
                        documentFile = it,
                        thumbnailModel = getMapThumbnailModel("$mapsDir/${it.name}")
                    )
                }
        }
    }

    private suspend fun fetchExportedMaps() {
        withContext(Dispatchers.IO) {
            exportedMaps = (exportedMapsFile.listFiles() ?: emptyArray())
                .filter { it.isFile && it.name.lowercase().endsWith(".txt") }
                .sortedBy { it.name.lowercase() }
                .map {
                    LACMap(
                        name = it.nameWithoutExtension,
                        fileName = it.name,
                        fileSize = it.length(),
                        lastModified = it.lastModified(),
                        file = it,
                        documentFile = null
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