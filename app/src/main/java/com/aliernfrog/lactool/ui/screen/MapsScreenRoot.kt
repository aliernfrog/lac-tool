package com.aliernfrog.lactool.ui.screen

import android.Manifest
import android.annotation.SuppressLint
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.DocumentsContract
import android.provider.Settings
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.Crossfade
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.aliernfrog.lactool.LACToolComposableShape
import com.aliernfrog.lactool.R
import com.aliernfrog.lactool.state.MapsState
import com.aliernfrog.lactool.util.FileUtil
import com.aliernfrog.lactool.util.GeneralUtil

private var hasStoragePerms = mutableStateOf(true)
private var hasUriPerms = mutableStateOf(true)

@Composable
fun MapsScreenRoot(mapsState: MapsState, navController: NavController) {
    val context = LocalContext.current
    hasStoragePerms.value = GeneralUtil.checkStoragePermissions(context)
    hasUriPerms.value = FileUtil.checkUriPermission(mapsState.mapsDir, context)
    Crossfade(targetState = (hasUriPerms.value && hasStoragePerms.value)) {
        if (it) MapsScreen(mapsState, navController)
        else Column { PermissionsSetUp(mapsState.mapsDir) }
    }
}

@SuppressLint("InlinedApi")
@Composable
private fun PermissionsSetUp(mapsDir: String) {
    val context = LocalContext.current
    val allFilesAccess = Build.VERSION.SDK_INT >= 30

    val storagePermsLauncher = rememberLauncherForActivityResult(contract = ActivityResultContracts.RequestPermission(), onResult = {})
    ErrorColumn(
        visible = !hasStoragePerms.value,
        title = context.getString(R.string.warning_missingStoragePermissions),
        content = {
            Text(text = if (allFilesAccess) context.getString(R.string.info_allFilesPermission) else context.getString(R.string.info_storagePermission), color = MaterialTheme.colorScheme.onError)
        }
    ) {
        hasStoragePerms.value = GeneralUtil.checkStoragePermissions(context)
        if (!hasStoragePerms.value) {
            if (allFilesAccess) {
                val intent = Intent(Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION)
                val uri = Uri.fromParts("package", context.packageName, null)
                intent.data = uri
                context.startActivity(intent)
            } else storagePermsLauncher.launch(Manifest.permission.WRITE_EXTERNAL_STORAGE)
        }
    }

    val uriPermsLauncher = rememberLauncherForActivityResult(contract = ActivityResultContracts.OpenDocumentTree(), onResult = {
        if (it != null) {
            val takeFlags = Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_GRANT_WRITE_URI_PERMISSION
            context.grantUriPermission(context.packageName, it, takeFlags)
            context.contentResolver.takePersistableUriPermission(it, takeFlags)
        }
    })
    ErrorColumn(
        visible = !hasUriPerms.value,
        title = context.getString(R.string.warning_missingUriPermissions),
        content = {
            Text(text = context.getString(R.string.info_mapsFolderPermission), color = MaterialTheme.colorScheme.onError)
            Spacer(Modifier.height(8.dp))
            Text(mapsDir.replaceFirst(Environment.getExternalStorageDirectory().toString(), context.getString(R.string.internalStorage)), fontFamily = FontFamily.Monospace, fontSize = 14.sp, color = MaterialTheme.colorScheme.onError)
        }
    ) {
        hasUriPerms.value = FileUtil.checkUriPermission(mapsDir, context)
        if (!hasUriPerms.value) {
            val treeId = mapsDir.replace("${Environment.getExternalStorageDirectory()}/", "primary:")
            val uri = DocumentsContract.buildDocumentUri("com.android.externalstorage.documents", treeId)
            uriPermsLauncher.launch(uri)
        }
    }
}

@Composable
private fun ErrorColumn(visible: Boolean = true, title: String, content: @Composable () -> Unit, onClick: () -> Unit) {
    val context = LocalContext.current
    AnimatedVisibility(visible) {
        Column(Modifier.fillMaxWidth().padding(8.dp).clip(LACToolComposableShape).clickable { onClick() }.background(MaterialTheme.colorScheme.error).padding(vertical = 8.dp, horizontal = 16.dp)) {
            Text(text = title, color = MaterialTheme.colorScheme.onError, fontSize = 25.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(bottom = 10.dp))
            content()
            Text(text = context.getString(R.string.info_permissionsHint), color = MaterialTheme.colorScheme.onError, fontSize = 14.sp, fontWeight = FontWeight.Medium, modifier = Modifier.padding(top = 10.dp))
        }
    }
}