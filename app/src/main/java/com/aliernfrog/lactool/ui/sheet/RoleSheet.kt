package com.aliernfrog.lactool.ui.sheet

import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.ModalBottomSheetState
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.AnnotatedString
import com.aliernfrog.lactool.R
import com.aliernfrog.lactool.ui.composable.LACToolButtonShapeless
import com.aliernfrog.lactool.ui.composable.LACToolColumnDivider
import com.aliernfrog.lactool.ui.composable.LACToolMapRole
import com.aliernfrog.lactool.ui.composable.LACToolModalBottomSheet
import com.aliernfrog.lactool.util.extensions.removeHtml
import com.aliernfrog.toptoast.TopToastColorType
import com.aliernfrog.toptoast.TopToastManager
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun RoleSheet(role: String, state: ModalBottomSheetState, topToastManager: TopToastManager? = null, onDeleteRole: (String) -> Unit) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val clipboardManager = LocalClipboardManager.current
    LACToolModalBottomSheet(sheetState = state) {
        LACToolColumnDivider(null) {
            LACToolMapRole(role, true) {}
        }
        LACToolColumnDivider(null, bottomDivider = false) {
            LACToolButtonShapeless(
                title = stringResource(R.string.mapsRoles_copyRoleName),
                painter = painterResource(R.drawable.copy)
            ) {
                clipboardManager.setText(AnnotatedString(role.removeHtml()))
                topToastManager?.showToast(context.getString(R.string.info_copiedToClipboard), iconDrawableId = R.drawable.copy, iconTintColorType = TopToastColorType.PRIMARY)
                scope.launch { state.hide() }
            }
            LACToolButtonShapeless(
                title = stringResource(R.string.mapsRoles_copyRoleRaw),
                painter = painterResource(R.drawable.copy)
            ) {
                clipboardManager.setText(AnnotatedString(role))
                topToastManager?.showToast(context.getString(R.string.info_copiedToClipboard), iconDrawableId = R.drawable.copy, iconTintColorType = TopToastColorType.PRIMARY)
                scope.launch { state.hide() }
            }
            LACToolButtonShapeless(
                title = stringResource(R.string.mapsRoles_deleteRole),
                painter = painterResource(R.drawable.trash),
                contentColor = MaterialTheme.colorScheme.error
            ) {
                onDeleteRole(role)
                scope.launch { state.hide() }
            }
        }
    }
}