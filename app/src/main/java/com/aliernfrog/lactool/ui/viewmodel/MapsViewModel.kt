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
    val exportedMapsDir = prefs.exportedMapsDir
    private lateinit var mapsFile: DocumentFileCompat
    private val exportedMapsFile = File(exportedMapsDir)

    var mapDeleteDialogShown by mutableStateOf(false)
    var importedMaps by mutableStateOf(emptyList<LACMap>())
    var exportedMaps by mutableStateOf(emptyList<LACMap>())
    var chosenMap by mutableStateOf<LACMap?>(null)
    var mapNameEdit by mutableStateOf("")
    var lastMapName by mutableStateOf("")

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
            is LACMap -> mapToChoose = map
            else -> mapToChoose = null
        }

        chosenMap = mapToChoose
        if (mapToChoose != null) {
            mapNameEdit = mapToChoose.name
            lastMapName = mapToChoose.name
        }
    }

    suspend fun renameChosenMap() {
        val newName = getMapNameEdit(false)
        val currentName = chosenMap?.name ?: return
        val output = mapsFile.findFile(getMapNameEdit())
        if (output != null && output.exists()) fileAlreadyExists()
        else withContext(Dispatchers.IO) {
            getChosenMapFiles().forEach { file ->
                val newFileName = file.name.replaceFirst(currentName, newName)
                file.renameTo(newFileName)
            }
            chooseMap(mapsFile.findFile(newName))
            topToastState.showToast(R.string.maps_rename_done, Icons.Rounded.Edit)
            fetchImportedMaps()
        }
    }

    suspend fun importChosenMap(context: Context) {
        val mapPath = chosenMap?.file?.absolutePath ?: return
        var output = mapsFile.findFile(getMapNameEdit())
        if (output != null && output.exists()) fileAlreadyExists()
        else withContext(Dispatchers.IO) {
            output = mapsFile.createFile("", getMapNameEdit()) ?: return@withContext
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

    fun getChosenMapPath(): String? {
        val map = chosenMap ?: return null
        return if (map.file != null) map.file.absolutePath
        else if (map.documentFile != null) "$mapsDir/${map.documentFile.name}"
        else null
    }

    fun getMapNameEdit(addTxtSuffix: Boolean = true): String {
        val suffix = if (addTxtSuffix) ".txt" else ""
        return mapNameEdit.ifBlank { chosenMap?.name }+suffix
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
            val files = mapsFile.listFiles().filter { it.isFile() && it.name.lowercase().endsWith(".txt") }.sortedBy { it.name.lowercase() }
            val maps = files.map {
                val nameWithoutExtension = FileUtil.removeExtension(it.name)
                LACMap(nameWithoutExtension, it.name, it.length, it.lastModified, null, it, mapsFile.findFile("${nameWithoutExtension}.jpg")?.uri.toString())
            }
            importedMaps = maps
        }
    }

    private suspend fun fetchExportedMaps() {
        withContext(Dispatchers.IO) {
            val files = exportedMapsFile.listFiles()?.filter { it.isFile && it.name.lowercase().endsWith(".txt") }?.sortedBy { it.name.lowercase() }
            val maps = files?.map { LACMap(it.nameWithoutExtension, it.name, it.length(), it.lastModified(), it, null) }
            if (maps != null) exportedMaps = maps
        }
    }

    private fun fileAlreadyExists() {
        topToastState.showToast(R.string.maps_alreadyExists, Icons.Rounded.PriorityHigh, TopToastColor.ERROR)
    }

    private fun fileDoesntExist() {
        topToastState.showToast(R.string.warning_fileDoesntExist, Icons.Rounded.PriorityHigh, TopToastColor.ERROR)
    }
}