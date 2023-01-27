package com.aliernfrog.lactool.state

import android.content.Context
import android.content.SharedPreferences
import android.os.Environment
import android.provider.DocumentsContract
import androidx.compose.foundation.ScrollState
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.ModalBottomSheetState
import androidx.compose.material.ModalBottomSheetValue
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.TopAppBarState
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.navigation.NavController
import com.aliernfrog.lactool.ConfigKey
import com.aliernfrog.lactool.R
import com.aliernfrog.lactool.data.LACMap
import com.aliernfrog.lactool.util.Destination
import com.aliernfrog.lactool.util.staticutil.FileUtil
import com.aliernfrog.toptoast.enum.TopToastColor
import com.aliernfrog.toptoast.state.TopToastState
import com.lazygeniouz.dfc.file.DocumentFileCompat
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File

@OptIn(ExperimentalMaterialApi::class, ExperimentalMaterial3Api::class)
class MapsState(
    _topToastState: TopToastState,
    config: SharedPreferences
) {
    private val topToastState = _topToastState
    val mapsEditState = MapsEditState(topToastState)
    val mapsMergeState = MapsMergeState(topToastState, this)
    val pickMapSheetState = ModalBottomSheetState(initialValue = ModalBottomSheetValue.Hidden)
    val topAppBarState = TopAppBarState(0F, 0F, 0F)
    val scrollState = ScrollState(0)
    val mapsDir = config.getString(ConfigKey.KEY_MAPS_DIR, ConfigKey.DEFAULT_MAPS_DIR)!!
    val mapsExportDir = config.getString(ConfigKey.KEY_MAPS_EXPORT_DIR, ConfigKey.DEFAULT_MAPS_EXPORT_DIR)!!
    private lateinit var mapsFile: DocumentFileCompat
    private val exportedMapsFile = File(mapsExportDir)

    val mapDeleteDialogShown = mutableStateOf(false)
    val importedMaps = mutableStateOf(emptyList<LACMap>())
    val exportedMaps = mutableStateOf(emptyList<LACMap>())
    val chosenMap: MutableState<LACMap?> = mutableStateOf(null)
    val mapNameEdit = mutableStateOf("")
    val lastMapName = mutableStateOf("")

    fun getMap(file: File? = null, documentFile: DocumentFileCompat? = null) {
        if (file != null) {
            val mapName = file.nameWithoutExtension
            if (file.exists()) setChosenMap(LACMap(name = mapName, fileName = file.name, file = file))
            else fileDoesntExist()
        } else if (documentFile != null) {
            val mapName = FileUtil.removeExtension(documentFile.name)
            if (documentFile.exists()) setChosenMap(LACMap(name = mapName, fileName = documentFile.name, documentFile = documentFile))
            else fileDoesntExist()
        } else {
            setChosenMap(null)
        }
    }

    suspend fun renameChosenMap() {
        val newName = getMapNameEdit(false)
        val currentName = chosenMap.value?.name ?: return
        val output = mapsFile.findFile(getMapNameEdit())
        if (output != null && output.exists()) fileAlreadyExists()
        else withContext(Dispatchers.IO) {
            getChosenMapFiles().forEach { file ->
                val newFileName = file.name.replaceFirst(currentName, newName)
                file.renameTo(newFileName)
            }
            getMap(documentFile = mapsFile.findFile(newName))
            topToastState.showToast(R.string.maps_rename_done, Icons.Rounded.Edit)
            getImportedMaps()
        }
    }

    suspend fun importChosenMap(context: Context) {
        val mapPath = chosenMap.value?.file?.absolutePath ?: return
        var output = mapsFile.findFile(getMapNameEdit())
        if (output != null && output.exists()) fileAlreadyExists()
        else withContext(Dispatchers.IO) {
            output = mapsFile.createFile("", getMapNameEdit()) ?: return@withContext
            FileUtil.copyFile(mapPath, output ?: return@withContext, context)
            getMap(documentFile = output)
            topToastState.showToast(R.string.maps_import_done, Icons.Rounded.Download)
            getImportedMaps()
        }
    }

    suspend fun exportChosenMap(context: Context) {
        val mapFile = chosenMap.value?.documentFile ?: return
        val output = File("${mapsExportDir}/${getMapNameEdit()}")
        if (output.exists()) fileAlreadyExists()
        else withContext(Dispatchers.IO) {
            if (output.parentFile?.isDirectory != true) output.parentFile?.mkdirs()
            FileUtil.copyFile(mapFile, output.absolutePath, context)
            getMap(file = output)
            topToastState.showToast(R.string.maps_export_done, Icons.Rounded.Upload)
            getExportedMaps()
        }
    }

    suspend fun editChosenMap(context: Context, navController: NavController) {
        val map = chosenMap.value ?: return
        if (map.documentFile != null) mapsEditState.loadMap(null, map.documentFile, context)
        else if (map.file != null) mapsEditState.loadMap(map.file, null, context)
        else return
        navController.navigate(Destination.MAPS_EDIT.route)
    }

    suspend fun deleteChosenMap() {
        val map = chosenMap.value ?: return
        withContext(Dispatchers.IO) {
            if (map.documentFile != null) {
                getChosenMapFiles().forEach { it.delete() }
                getImportedMaps()
            } else {
                map.file?.delete()
                getExportedMaps()
            }
            getMap()
            topToastState.showToast(R.string.maps_delete_done, Icons.Rounded.Delete)
        }
    }

    fun getChosenMapPath(): String? {
        val map = chosenMap.value ?: return null
        return if (map.file != null) map.file.absolutePath
        else if (map.documentFile != null) "$mapsDir/${map.documentFile.name}"
        else null
    }

    fun getMapNameEdit(addTxtSuffix: Boolean = true): String {
        val suffix = if (addTxtSuffix) ".txt" else ""
        return mapNameEdit.value.ifBlank { chosenMap.value?.name }+suffix
    }

    private fun getChosenMapFiles(): List<DocumentFileCompat> {
        val chosenMapName = chosenMap.value?.name ?: return listOf()
        val list = mutableListOf<DocumentFileCompat>()
        val mapFile = mapsFile.findFile("${chosenMapName}.txt")
        val thumbnailFile = mapsFile.findFile("${chosenMapName}.jpg")
        val dataFile = mapsFile.findFile(chosenMapName)
        if (mapFile != null && mapFile.exists() && mapFile.isFile()) list.add(mapFile)
        if (thumbnailFile != null && thumbnailFile.exists() && thumbnailFile.isFile()) list.add(thumbnailFile)
        if (dataFile != null && dataFile.exists() && dataFile.isDirectory()) list.add(dataFile)
        return list
    }

    private fun setChosenMap(map: LACMap?) {
        chosenMap.value = map
        if (map != null) {
            mapNameEdit.value = map.name
            lastMapName.value = map.name
        }
    }

    private fun fileAlreadyExists() {
        topToastState.showToast(R.string.maps_alreadyExists, Icons.Rounded.PriorityHigh, TopToastColor.ERROR)
    }

    private fun fileDoesntExist() {
        topToastState.showToast(R.string.warning_fileDoesntExist, Icons.Rounded.PriorityHigh, TopToastColor.ERROR)
    }

    fun getMapsFile(context: Context): DocumentFileCompat {
        if (::mapsFile.isInitialized) return mapsFile
        val treeId = mapsDir.replace("${Environment.getExternalStorageDirectory()}/", "primary:")
        val treeUri = DocumentsContract.buildTreeDocumentUri("com.android.externalstorage.documents", treeId)
        mapsFile = DocumentFileCompat.fromTreeUri(context, treeUri)!!
        return mapsFile
    }

    suspend fun getImportedMaps() {
        withContext(Dispatchers.IO) {
            val files = mapsFile.listFiles().filter { it.isFile() && it.name.lowercase().endsWith(".txt") }.sortedBy { it.name.lowercase() }
            val maps = files.map {
                val nameWithoutExtension = FileUtil.removeExtension(it.name)
                LACMap(nameWithoutExtension, it.name, it.length, it.lastModified, null, it, mapsFile.findFile("${nameWithoutExtension}.jpg")?.uri.toString())
            }
            importedMaps.value = maps
        }
    }

    suspend fun getExportedMaps() {
        withContext(Dispatchers.IO) {
            val files = exportedMapsFile.listFiles()?.filter { it.isFile && it.name.lowercase().endsWith(".txt") }?.sortedBy { it.name.lowercase() }
            val maps = files?.map { LACMap(it.nameWithoutExtension, it.name, it.length(), it.lastModified(), it, null) }
            if (maps != null) exportedMaps.value = maps
        }
    }
}