package com.aliernfrog.lactool.ui.component.maps

import android.content.ClipData
import android.graphics.Typeface
import android.text.Spanned
import android.text.style.ForegroundColorSpan
import android.text.style.StyleSpan
import android.text.style.UnderlineSpan
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ContentCopy
import androidx.compose.material.icons.rounded.Delete
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.platform.ClipEntry
import androidx.compose.ui.platform.LocalClipboard
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.core.text.HtmlCompat
import com.aliernfrog.lactool.R
import com.aliernfrog.lactool.ui.component.expressive.ExpressiveButtonRow
import com.aliernfrog.lactool.ui.component.expressive.ExpressiveRowIcon
import com.aliernfrog.lactool.ui.component.form.DividerRow
import com.aliernfrog.lactool.ui.component.form.ExpandableRow
import com.aliernfrog.lactool.util.extension.removeHtml
import com.aliernfrog.toptoast.state.TopToastState
import kotlinx.coroutines.launch

@Composable
fun MapRoleRow(
    role: String,
    expanded: Boolean?,
    modifier: Modifier = Modifier,
    alwaysShowRaw: Boolean = false,
    showTopDivider: Boolean = true,
    topToastState: TopToastState? = null,
    onRoleDelete: (String) -> Unit,
    onClick: () -> Unit
) {
    val context = LocalContext.current
    val clipboard = LocalClipboard.current
    val scope = rememberCoroutineScope()

    val rawDifferent = role.removeHtml() != role

    if (showTopDivider) DividerRow()
    ExpandableRow(
        title = HtmlCompat.fromHtml(
            role.replace("<color", "<font color").replace("</color>", "</font>"),
            HtmlCompat.FROM_HTML_MODE_LEGACY
        ).toAnnotatedString().text, // TODO handle colors
        description = role, // TODO better way to handle raw name
        expanded = expanded ?: false,
        modifier = modifier,
        onClickHeader = onClick
    ) {
        ExpressiveButtonRow(
            title = stringResource(R.string.mapsRoles_copyRoleName),
            icon = {
                ExpressiveRowIcon(
                    painter = rememberVectorPainter(Icons.Rounded.ContentCopy)
                )
            }
        ) { scope.launch {
            clipboard.setClipEntry(ClipEntry(ClipData.newPlainText(
                context.getString(R.string.mapsRoles_copyRoleClipLabel),
                role.removeHtml()
            )))
            topToastState?.showToast(R.string.info_copiedToClipboard, Icons.Rounded.ContentCopy)
        } }
        if (rawDifferent) ExpressiveButtonRow(
            title = stringResource(R.string.mapsRoles_copyRoleRaw),
            icon = {
                ExpressiveRowIcon(
                    painter = rememberVectorPainter(Icons.Rounded.ContentCopy)
                )
            }
        ) { scope.launch {
            clipboard.setClipEntry(ClipEntry(ClipData.newPlainText(
                context.getString(R.string.mapsRoles_copyRoleClipLabel),
                role
            )))
            topToastState?.showToast(R.string.info_copiedToClipboard, Icons.Rounded.ContentCopy)
        } }
        ExpressiveButtonRow(
            title = stringResource(R.string.mapsRoles_deleteRole),
            contentColor = MaterialTheme.colorScheme.error,
            icon = {
                ExpressiveRowIcon(
                    painter = rememberVectorPainter(Icons.Rounded.Delete)
                )
            }
        ) {
            onRoleDelete(role)
        }
    }
}

private fun Spanned.toAnnotatedString(): AnnotatedString = buildAnnotatedString {
    val spanned = this@toAnnotatedString
    append(spanned.toString())
    getSpans(0, spanned.length, Any::class.java).forEach { span ->
        val start = getSpanStart(span)
        val end = getSpanEnd(span)
        when (span) {
            is StyleSpan -> when (span.style) {
                Typeface.BOLD -> addStyle(SpanStyle(fontWeight = FontWeight.Bold), start, end)
                Typeface.ITALIC -> addStyle(SpanStyle(fontStyle = FontStyle.Italic), start, end)
                Typeface.BOLD_ITALIC -> addStyle(SpanStyle(fontWeight = FontWeight.Bold, fontStyle = FontStyle.Italic), start, end)
            }
            is UnderlineSpan -> addStyle(SpanStyle(textDecoration = TextDecoration.Underline), start, end)
            is ForegroundColorSpan -> addStyle(SpanStyle(color = Color(span.foregroundColor)), start, end)
        }
    }
}