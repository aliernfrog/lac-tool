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
import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.aliernfrog.lactool.AppComposableShape
import com.aliernfrog.lactool.R
import com.aliernfrog.lactool.util.staticutil.FileUtil
import com.aliernfrog.lactool.util.staticutil.GeneralUtil

@Composable
fun PermissionsScreen(uriPath: String?, onSuccess: @Composable () -> Unit) {
    val context = LocalContext.current
    var storagePermissions by remember { mutableStateOf(GeneralUtil.checkStoragePermissions(context)) }
    var uriPermissions by remember { mutableStateOf(if (uriPath != null) FileUtil.checkUriPermission(uriPath, context) else true) }
    Crossfade(targetState = (storagePermissions && uriPermissions)) { hasPermissions ->
        if (hasPermissions) onSuccess()
        else Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState())) {
            PermissionsSetUp(
                uriPath = uriPath,
                storagePermissions = storagePermissions,
                uriPermissions = uriPermissions,
                onStorageResult =  { storagePermissions = it },
                onUriResult = { uriPermissions = it }
            )
        }
    }
}

@SuppressLint("InlinedApi")
@Composable
private fun PermissionsSetUp(
    uriPath: String?,
    storagePermissions: Boolean,
    uriPermissions: Boolean,
    onStorageResult: (Boolean) -> Unit,
    onUriResult: (Boolean) -> Unit
) {
    val context = LocalContext.current
    val allFilesAccess = Build.VERSION.SDK_INT >= 30

    val storagePermsLauncher = rememberLauncherForActivityResult(contract = ActivityResultContracts.RequestPermission(), onResult = {
        onStorageResult(GeneralUtil.checkStoragePermissions(context))
    })
    val allFilesPermsLauncher = rememberLauncherForActivityResult(contract = ActivityResultContracts.StartActivityForResult(), onResult = {
        onStorageResult(GeneralUtil.checkStoragePermissions(context))
    })
    ErrorColumn(
        visible = !storagePermissions,
        title = context.getString(R.string.warning_missingStoragePermissions),
        content = {
            Text(text = stringResource(R.string.info_storagePermission), color = MaterialTheme.colorScheme.onError)
        }
    ) {
        if (allFilesAccess) {
            val intent = Intent(Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION)
            intent.data = Uri.fromParts("package", context.packageName, null)
            allFilesPermsLauncher.launch(intent)
        } else storagePermsLauncher.launch(Manifest.permission.WRITE_EXTERNAL_STORAGE)
    }

    if (uriPath != null) {
        val uriPermsLauncher = rememberLauncherForActivityResult(contract = ActivityResultContracts.OpenDocumentTree(), onResult = {
            if (it != null) {
                val takeFlags = Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_GRANT_WRITE_URI_PERMISSION
                context.grantUriPermission(context.packageName, it, takeFlags)
                context.contentResolver.takePersistableUriPermission(it, takeFlags)
                onUriResult(FileUtil.checkUriPermission(uriPath, context))
            }
        })
        ErrorColumn(
            visible = !uriPermissions,
            title = stringResource(R.string.warning_missingUriPermissions),
            content = {
                Text(text = stringResource(R.string.info_uriPermission), color = MaterialTheme.colorScheme.onError)
                Spacer(Modifier.height(8.dp))
                Text(uriPath.replaceFirst(Environment.getExternalStorageDirectory().toString(), context.getString(R.string.internalStorage)), fontFamily = FontFamily.Monospace, fontSize = 14.sp, color = MaterialTheme.colorScheme.onError)
            }
        ) {
            val treeId = uriPath.replace("${Environment.getExternalStorageDirectory()}/", "primary:")
            val uri = DocumentsContract.buildDocumentUri("com.android.externalstorage.documents", treeId)
            uriPermsLauncher.launch(uri)
        }
    }
}

@Composable
private fun ErrorColumn(visible: Boolean = true, title: String, content: @Composable () -> Unit, onClick: () -> Unit) {
    AnimatedVisibility(
        visible = visible,
        enter = expandVertically() + fadeIn(),
        exit = shrinkVertically() + fadeOut()
    ) {
        Column(Modifier.fillMaxWidth().padding(8.dp).clip(AppComposableShape).clickable { onClick() }.background(MaterialTheme.colorScheme.error).padding(vertical = 8.dp, horizontal = 16.dp)) {
            Text(text = title, color = MaterialTheme.colorScheme.onError, fontSize = 25.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(bottom = 10.dp))
            content()
            Text(text = stringResource(R.string.info_permissionsHint), color = MaterialTheme.colorScheme.onError, fontSize = 14.sp, fontWeight = FontWeight.Medium, modifier = Modifier.padding(top = 10.dp))
        }
    }
}