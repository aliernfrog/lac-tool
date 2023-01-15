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

@OptIn(ExperimentalMaterialApi::class)
class MapsState(
    _topToastState: TopToastState,
    config: SharedPreferences
) {
    private val topToastState = _topToastState
    val mapsEditState = MapsEditState(topToastState)
    val mapsMergeState = MapsMergeState(topToastState, this)
    val pickMapSheetState = ModalBottomSheetState(initialValue = ModalBottomSheetValue.Hidden)
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
        val output = mapsFile.findFile(getMapNameEdit())
        if (output != null && output.exists()) fileAlreadyExists()
        else withContext(Dispatchers.IO) {
            getChosenMapFiles().forEach { file ->
                val newName = file.name.replaceFirst(chosenMap.value!!.name, getMapNameEdit(false))
                file.renameTo(newName)
            }
            getMap(documentFile = mapsFile.findFile(getMapNameEdit()))
            topToastState.showToast(R.string.maps_rename_done, Icons.Rounded.Edit)
            getImportedMaps()
        }
    }

    suspend fun importChosenMap(context: Context) {
        var output = mapsFile.findFile(getMapNameEdit())
        if (output != null && output.exists()) fileAlreadyExists()
        else withContext(Dispatchers.IO) {
            output = mapsFile.createFile("", getMapNameEdit())
            if (output != null) FileUtil.copyFile(chosenMap.value!!.file!!.absolutePath, output!!, context)
            getMap(documentFile = output)
            topToastState.showToast(R.string.maps_import_done, Icons.Rounded.Download)
            getImportedMaps()
        }
    }

    suspend fun exportChosenMap(context: Context) {
        val output = File("${mapsExportDir}/${getMapNameEdit()}")
        if (output.exists()) fileAlreadyExists()
        else withContext(Dispatchers.IO) {
            if (!output.parentFile?.isDirectory!!) output.parentFile?.mkdirs()
            FileUtil.copyFile(mapsFile.findFile(chosenMap.value!!.fileName)!!, output.absolutePath, context)
            getMap(file = output)
            topToastState.showToast(R.string.maps_export_done, Icons.Rounded.Upload)
            getExportedMaps()
        }
    }

    suspend fun editChosenMap(context: Context, navController: NavController) {
        if (chosenMap.value!!.documentFile != null) mapsEditState.loadMap(null, mapsFile.findFile(chosenMap.value!!.fileName)!!, context)
        else mapsEditState.loadMap(chosenMap.value!!.file!!, null, context)
        navController.navigate(Destination.MAPS_EDIT.route)
    }

    suspend fun deleteChosenMap() {
        withContext(Dispatchers.IO) {
            if (chosenMap.value!!.documentFile != null) {
                getChosenMapFiles().forEach { it.delete() }
                getImportedMaps()
            } else {
                chosenMap.value?.file?.delete()
                getExportedMaps()
            }
            getMap()
            topToastState.showToast(R.string.maps_delete_done, Icons.Rounded.Delete)
        }
    }

    fun getChosenMapPath(): String? {
        return if (chosenMap.value?.file != null) chosenMap.value!!.file!!.absolutePath
        else if (chosenMap.value?.documentFile != null) "$mapsDir/${chosenMap.value!!.name}"
        else null
    }

    fun getMapNameEdit(addTxtSuffix: Boolean = true): String {
        val suffix = if (addTxtSuffix) ".txt" else ""
        return mapNameEdit.value.ifBlank { chosenMap.value?.name }+suffix
    }

    private fun getChosenMapFiles(): List<DocumentFileCompat> {
        val chosenMapName = chosenMap.value?.name.toString()
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