package com.aliernfrog.lactool.state

import android.content.Context
import android.content.SharedPreferences
import android.os.Environment
import android.provider.DocumentsContract
import androidx.compose.foundation.ScrollState
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.ModalBottomSheetState
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.navigation.NavController
import com.aliernfrog.lactool.ConfigKey
import com.aliernfrog.lactool.NavRoutes
import com.aliernfrog.lactool.R
import com.aliernfrog.lactool.data.LacMap
import com.aliernfrog.lactool.data.MapsListItem
import com.aliernfrog.lactool.util.FileUtil
import com.aliernfrog.toptoast.TopToastColorType
import com.aliernfrog.toptoast.TopToastManager
import com.lazygeniouz.filecompat.file.DocumentFileCompat
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File

@OptIn(ExperimentalMaterialApi::class)
class MapsState(
    _topToastManager: TopToastManager,
    config: SharedPreferences,
    _pickMapSheetState: ModalBottomSheetState,
    _deleteMapSheetState: ModalBottomSheetState
) {
    private val topToastManager = _topToastManager
    val mapsEditState = MapsEditState()
    val pickMapSheetState = _pickMapSheetState
    val deleteMapSheetState = _deleteMapSheetState
    val scrollState = ScrollState(0)
    val mapsDir = config.getString(ConfigKey.KEY_MAPS_DIR, ConfigKey.DEFAULT_MAPS_DIR)!!
    val mapsExportDir = config.getString(ConfigKey.KEY_MAPS_EXPORT_DIR, ConfigKey.DEFAULT_MAPS_EXPORT_DIR)!!
    private lateinit var mapsFile: DocumentFileCompat
    private val exportedMapsFile = File(mapsExportDir)

    val importedMaps = mutableStateOf(emptyList<MapsListItem>())
    val exportedMaps = mutableStateOf(emptyList<MapsListItem>())
    val chosenMap: MutableState<LacMap?> = mutableStateOf(null)
    val mapNameEdit = mutableStateOf("")
    val lastMapName = mutableStateOf("")

    fun getMap(file: File? = null, documentFile: DocumentFileCompat? = null, context: Context) {
        if (file != null) {
            val mapName = file.nameWithoutExtension
            if (file.exists()) setChosenMap(LacMap(mapName = mapName, fileName = file.name, filePath = file.absolutePath, isFromUri = false))
            else fileDoesntExist(context)
        } else if (documentFile != null) {
            val mapName = FileUtil.removeExtension(documentFile.name)
            if (documentFile.exists()) setChosenMap(LacMap(mapName = mapName, fileName = documentFile.name, filePath = "$mapsDir/${documentFile.name}", isFromUri = true))
            else fileDoesntExist(context)
        } else {
            setChosenMap(null)
        }
    }

    suspend fun renameChosenMap(context: Context) {
        val output = mapsFile.findFile(getMapNameEdit())
        if (output != null && output.exists()) fileAlreadyExists(context)
        else withContext(Dispatchers.IO) {
            getChosenMapFiles().forEach { file ->
                val newName = file.name.replaceFirst(chosenMap.value!!.mapName, getMapNameEdit(false))
                file.renameTo(newName)
            }
            getMap(documentFile = mapsFile.findFile(getMapNameEdit()), context = context)
            topToastManager.showToast(context.getString(R.string.info_renamedMap), iconDrawableId = R.drawable.edit, iconTintColorType = TopToastColorType.PRIMARY)
            getImportedMaps()
        }
    }

    suspend fun importChosenMap(context: Context) {
        var output = mapsFile.findFile(getMapNameEdit())
        if (output != null && output.exists()) fileAlreadyExists(context)
        else withContext(Dispatchers.IO) {
            output = mapsFile.createFile("", getMapNameEdit())
            if (output != null) FileUtil.copyFile(chosenMap.value!!.filePath, output!!, context)
            getMap(documentFile = output, context = context)
            topToastManager.showToast(context.getString(R.string.info_importedMap), iconDrawableId = R.drawable.download, iconTintColorType = TopToastColorType.PRIMARY)
            getImportedMaps()
        }
    }

    suspend fun exportChosenMap(context: Context) {
        val output = File("${mapsExportDir}/${getMapNameEdit()}")
        if (output.exists()) fileAlreadyExists(context)
        else withContext(Dispatchers.IO) {
            if (!output.parentFile?.isDirectory!!) output.parentFile?.mkdirs()
            FileUtil.copyFile(mapsFile.findFile(chosenMap.value!!.fileName)!!, output.absolutePath, context)
            getMap(file = output, context = context)
            topToastManager.showToast(context.getString(R.string.info_exportedMap), iconDrawableId = R.drawable.share, iconTintColorType = TopToastColorType.PRIMARY)
            getExportedMaps()
        }
    }

    suspend fun editChosenMap(context: Context, navController: NavController) {
        if (chosenMap.value!!.isFromUri) mapsEditState.loadMap(null, mapsFile.findFile(chosenMap.value!!.fileName)!!, context)
        else mapsEditState.loadMap(File(chosenMap.value!!.filePath), null, context)
        navController.navigate(NavRoutes.MAPS_EDIT)
    }

    suspend fun deleteChosenMap(context: Context) {
        withContext(Dispatchers.IO) {
            if (chosenMap.value!!.isFromUri) {
                getChosenMapFiles().forEach { it.delete() }
                getImportedMaps()
            } else {
                File(chosenMap.value!!.filePath).delete()
                getExportedMaps()
            }
            getMap(context = context)
            topToastManager.showToast(context.getString(R.string.info_deletedMap), iconDrawableId = R.drawable.trash, iconTintColorType = TopToastColorType.PRIMARY)
        }
    }

    fun getMapNameEdit(addTxtSuffix: Boolean = true): String {
        val suffix = if (addTxtSuffix) ".txt" else ""
        return mapNameEdit.value.ifBlank { chosenMap.value!!.mapName }+suffix
    }

    private fun getChosenMapFiles(): List<DocumentFileCompat> {
        val chosenMapName = chosenMap.value!!.mapName
        val list = mutableListOf<DocumentFileCompat>()
        val mapFile = mapsFile.findFile("${chosenMapName}.txt")
        val thumbnailFile = mapsFile.findFile("${chosenMapName}.jpg")
        val dataFile = mapsFile.findFile(chosenMapName)
        if (mapFile != null && mapFile.exists() && mapFile.isFile()) list.add(mapFile)
        if (thumbnailFile != null && thumbnailFile.exists() && thumbnailFile.isFile()) list.add(thumbnailFile)
        if (dataFile != null && dataFile.exists() && dataFile.isDirectory()) list.add(dataFile)
        return list
    }

    private fun setChosenMap(map: LacMap?) {
        chosenMap.value = map
        if (map != null) {
            mapNameEdit.value = map.mapName
            lastMapName.value = map.mapName
        }
    }

    private fun fileAlreadyExists(context: Context) {
        topToastManager.showToast(context.getString(R.string.warning_mapAlreadyExists), iconDrawableId = R.drawable.exclamation, iconTintColorType = TopToastColorType.ERROR)
    }

    private fun fileDoesntExist(context: Context) {
        topToastManager.showToast(context.getString(R.string.warning_fileDoesntExist), iconDrawableId = R.drawable.exclamation, iconTintColorType = TopToastColorType.ERROR)
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
                MapsListItem(nameWithoutExtension, it.name, it.lastModified, null, it, mapsFile.findFile("${nameWithoutExtension}.jpg")?.uri.toString())
            }
            importedMaps.value = maps
        }
    }

    suspend fun getExportedMaps() {
        withContext(Dispatchers.IO) {
            val files = exportedMapsFile.listFiles()?.filter { it.isFile && it.name.lowercase().endsWith(".txt") }?.sortedBy { it.name.lowercase() }
            val maps = files?.map { MapsListItem(it.nameWithoutExtension, it.name, it.lastModified(), it, null) }
            if (maps != null) exportedMaps.value = maps
        }
    }
}