package com.aliernfrog.lactool.ui.component.maps

import android.graphics.Typeface
import android.text.Spanned
import android.text.style.ForegroundColorSpan
import android.text.style.StyleSpan
import android.text.style.UnderlineSpan
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ContentCopy
import androidx.compose.material.icons.rounded.Delete
import androidx.compose.material.icons.rounded.KeyboardArrowUp
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.contentColorFor
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.text.HtmlCompat
import com.aliernfrog.lactool.R
import com.aliernfrog.lactool.ui.component.FadeVisibility
import com.aliernfrog.lactool.ui.component.form.BaseExpandableRow
import com.aliernfrog.lactool.ui.component.form.ButtonRow
import com.aliernfrog.lactool.ui.component.form.DividerRow
import com.aliernfrog.lactool.util.extension.clickableWithColor
import com.aliernfrog.lactool.util.extension.removeHtml
import com.aliernfrog.toptoast.state.TopToastState

@Composable
fun MapRoleRow(
    role: String,
    expanded: Boolean?,
    modifier: Modifier = Modifier,
    alwaysShowRaw: Boolean = false,
    showTopDivider: Boolean = true,
    topToastState: TopToastState? = null,
    minimizedHeaderColor: Color = Color.Transparent,
    minimizedHeaderContentColor: Color =
        if (minimizedHeaderColor == Color.Transparent) MaterialTheme.colorScheme.onSurface
        else contentColorFor(minimizedHeaderColor),
    expandedHeaderColor: Color = MaterialTheme.colorScheme.secondary,
    expandedHeaderContentColor: Color = contentColorFor(expandedHeaderColor),
    onRoleDelete: (String) -> Unit,
    onClick: () -> Unit
) {
    val clipboardManager = LocalClipboardManager.current

    val headerColor by animateColorAsState(
        if (expanded == true) expandedHeaderColor else minimizedHeaderColor
    )
    val headerContentColor by animateColorAsState(
        if (expanded == true) expandedHeaderContentColor else minimizedHeaderContentColor
    )
    val arrowRotation by animateFloatAsState(
        if (expanded == true) 0f else 180f
    )

    if (showTopDivider) DividerRow()
    BaseExpandableRow(
        expanded = expanded ?: false,
        modifier = modifier,
        headerContent = {
            Row(
                modifier = Modifier
                    .heightIn(56.dp)
                    .background(headerColor)
                    .clickableWithColor(headerContentColor, onClick),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .padding(horizontal = 16.dp, vertical = 8.dp)
                ) {
                    Text(
                        text = HtmlCompat.fromHtml(
                            role.replace("<color", "<font color").replace("</color>", "</font>"),
                            HtmlCompat.FROM_HTML_MODE_LEGACY
                        ).toAnnotatedString(),
                        color = headerContentColor,
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp
                    )
                    FadeVisibility(expanded == true || alwaysShowRaw) {
                        Text(
                            text = role,
                            color = headerContentColor,
                            fontSize = 14.sp,
                            lineHeight = 18.sp,
                            fontFamily = FontFamily.Monospace
                        )
                    }
                }
                if (expanded != null) Icon(
                    imageVector = Icons.Rounded.KeyboardArrowUp,
                    contentDescription = null,
                    tint = headerContentColor,
                    modifier = Modifier
                        .padding(horizontal = 8.dp)
                        .rotate(arrowRotation)
                )
            }
        }
    ) {
        ButtonRow(
            title = stringResource(R.string.mapsRoles_copyRoleName),
            painter = rememberVectorPainter(Icons.Rounded.ContentCopy)
        ) {
            clipboardManager.setText(AnnotatedString(role.removeHtml()))
            topToastState?.showToast(R.string.info_copiedToClipboard, Icons.Rounded.ContentCopy)
        }
        ButtonRow(
            title = stringResource(R.string.mapsRoles_copyRoleRaw),
            painter = rememberVectorPainter(Icons.Rounded.ContentCopy)
        ) {
            clipboardManager.setText(AnnotatedString(role))
            topToastState?.showToast(R.string.info_copiedToClipboard, Icons.Rounded.ContentCopy)
        }
        ButtonRow(
            title = stringResource(R.string.mapsRoles_deleteRole),
            painter = rememberVectorPainter(Icons.Rounded.Delete),
            contentColor = MaterialTheme.colorScheme.error
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