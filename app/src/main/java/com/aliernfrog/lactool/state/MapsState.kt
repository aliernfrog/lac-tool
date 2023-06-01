package com.aliernfrog.lactool.state

import android.content.Context
import android.content.SharedPreferences
import android.os.Environment
import android.provider.DocumentsContract
import androidx.compose.foundation.ScrollState
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.TopAppBarState
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import com.aliernfrog.lactool.ConfigKey
import com.aliernfrog.lactool.data.LACMap
import com.aliernfrog.lactool.util.staticutil.FileUtil
import com.aliernfrog.toptoast.state.TopToastState
import com.lazygeniouz.dfc.file.DocumentFileCompat
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File

@OptIn(ExperimentalMaterial3Api::class)
class MapsState(
    _topToastState: TopToastState,
    config: SharedPreferences
) {
    private val topToastState = _topToastState
    val mapsMergeState = MapsMergeState(topToastState, this)
    val topAppBarState = TopAppBarState(0F, 0F, 0F)
    val scrollState = ScrollState(0)
    val mapsDir = config.getString(ConfigKey.KEY_MAPS_DIR, ConfigKey.DEFAULT_MAPS_DIR)!!
    val mapsExportDir = config.getString(ConfigKey.KEY_MAPS_EXPORT_DIR, ConfigKey.DEFAULT_MAPS_EXPORT_DIR)!!
    private lateinit var mapsFile: DocumentFileCompat
    private val exportedMapsFile = File(mapsExportDir)

    val importedMaps = mutableStateOf(emptyList<LACMap>())
    val exportedMaps = mutableStateOf(emptyList<LACMap>())
    val chosenMap: MutableState<LACMap?> = mutableStateOf(null)
    val mapNameEdit = mutableStateOf("")
    val lastMapName = mutableStateOf("")

    fun getMap(file: File? = null, documentFile: DocumentFileCompat? = null) {
        if (file != null) {
            val mapName = file.nameWithoutExtension
            if (file.exists()) setChosenMap(LACMap(name = mapName, fileName = file.name, file = file))
        } else if (documentFile != null) {
            val mapName = FileUtil.removeExtension(documentFile.name)
            if (documentFile.exists()) setChosenMap(LACMap(name = mapName, fileName = documentFile.name, documentFile = documentFile))
        } else {
            setChosenMap(null)
        }
    }

    private fun setChosenMap(map: LACMap?) {
        chosenMap.value = map
        if (map != null) {
            mapNameEdit.value = map.name
            lastMapName.value = map.name
        }
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