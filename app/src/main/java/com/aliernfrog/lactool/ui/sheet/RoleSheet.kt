package com.aliernfrog.lactool.ui.sheet

import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.ModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.AnnotatedString
import androidx.core.text.HtmlCompat.FROM_HTML_MODE_LEGACY
import androidx.core.text.HtmlCompat.fromHtml
import com.aliernfrog.lactool.R
import com.aliernfrog.lactool.ui.composable.LACToolButtonShapeless
import com.aliernfrog.lactool.ui.composable.LACToolColumnDivider
import com.aliernfrog.lactool.ui.composable.LACToolMapRole
import com.aliernfrog.lactool.ui.composable.LACToolModalBottomSheet
import com.aliernfrog.toptoast.TopToastColorType
import com.aliernfrog.toptoast.TopToastManager

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun RoleSheet(role: String, state: ModalBottomSheetState, topToastManager: TopToastManager? = null) {
    val context = LocalContext.current
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
                clipboardManager.setText(AnnotatedString(fromHtml(role, FROM_HTML_MODE_LEGACY).toString()))
                topToastManager?.showToast(context.getString(R.string.info_copiedToClipboard), iconDrawableId = R.drawable.copy, iconTintColorType = TopToastColorType.PRIMARY)
            }
            LACToolButtonShapeless(
                title = stringResource(R.string.mapsRoles_copyRoleRaw),
                painter = painterResource(R.drawable.copy)
            ) {
                clipboardManager.setText(AnnotatedString(role))
                topToastManager?.showToast(context.getString(R.string.info_copiedToClipboard), iconDrawableId = R.drawable.copy, iconTintColorType = TopToastColorType.PRIMARY)
            }
        }
    }
}