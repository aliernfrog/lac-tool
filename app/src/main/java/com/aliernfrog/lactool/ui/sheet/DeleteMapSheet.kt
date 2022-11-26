package com.aliernfrog.lactool.ui.sheet

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.ModalBottomSheetState
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.aliernfrog.lactool.R
import com.aliernfrog.lactool.ui.composable.LACToolButtonCentered
import com.aliernfrog.lactool.ui.composable.LACToolModalBottomSheet
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterialApi::class, ExperimentalComposeUiApi::class)
@Composable
fun DeleteMapSheet(mapName: String, sheetState: ModalBottomSheetState, onCancel: (() -> Unit)? = null, onDeleteConfirm: () -> Unit) {
    val context = LocalContext.current
    val keyboardController = LocalSoftwareKeyboardController.current
    val scope = rememberCoroutineScope()
    LACToolModalBottomSheet(sheetState = sheetState) {
        Text(text = context.getString(R.string.info_deleteQuestion).replace("%NAME%", mapName), color = MaterialTheme.colorScheme.onSurfaceVariant, fontWeight = FontWeight.Bold, fontSize = 20.sp, textAlign = TextAlign.Center, modifier = Modifier.fillMaxWidth().padding(8.dp))
        Row {
            Column(Modifier.weight(1f)) {
                LACToolButtonCentered(title = context.getString(R.string.action_cancel), containerColor = MaterialTheme.colorScheme.surfaceVariant, contentColor = MaterialTheme.colorScheme.onSurfaceVariant) {
                    scope.launch { sheetState.hide() }
                    if (onCancel != null) onCancel()
                }
            }
            Column(Modifier.weight(1f)) {
                LACToolButtonCentered(title = context.getString(R.string.action_delete), containerColor = MaterialTheme.colorScheme.error, contentColor = MaterialTheme.colorScheme.onError) {
                    scope.launch { sheetState.hide() }
                    onDeleteConfirm()
                }
            }
        }
    }
    LaunchedEffect(sheetState.isVisible) {
        if (sheetState.isVisible) keyboardController?.hide()
    }
}