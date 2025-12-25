package com.aliernfrog.lactool.ui.component.maps

import android.content.ClipData
import android.graphics.Typeface
import android.text.Spanned
import android.text.style.ForegroundColorSpan
import android.text.style.StyleSpan
import android.text.style.UnderlineSpan
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ContentCopy
import androidx.compose.material.icons.rounded.CopyAll
import androidx.compose.material.icons.rounded.Delete
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.platform.ClipEntry
import androidx.compose.ui.platform.LocalClipboard
import androidx.compose.ui.platform.LocalContext
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
import com.aliernfrog.lactool.util.extension.removeHtml
import com.aliernfrog.toptoast.state.TopToastState
import io.github.aliernfrog.shared.ui.component.FadeVisibility
import io.github.aliernfrog.shared.ui.component.VerticalSegmentor
import io.github.aliernfrog.shared.ui.component.expressive.ExpressiveButtonRow
import io.github.aliernfrog.shared.ui.component.expressive.ExpressiveRowIcon
import io.github.aliernfrog.shared.ui.component.form.BaseExpandableRow
import io.github.aliernfrog.shared.ui.component.form.ToggleExpandButton
import kotlinx.coroutines.launch

@Composable
fun MapRoleRow(
    role: String,
    expanded: Boolean?,
    modifier: Modifier = Modifier,
    description: String? = null,
    alwaysShowRaw: Boolean = false,
    topToastState: TopToastState? = null,
    onRoleDelete: (String) -> Unit,
    onClick: () -> Unit
) {
    val context = LocalContext.current
    val clipboard = LocalClipboard.current
    val scope = rememberCoroutineScope()

    val rawDifferent = role.removeHtml() != role

    BaseExpandableRow(
        expanded = expanded == true,
        modifier = modifier,
        onClickHeader = onClick,
        header = { containerColor, contentColor ->
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.heightIn(56.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .padding(vertical = 8.dp, horizontal = 18.dp)
                ) {
                    Text(
                        text = HtmlCompat.fromHtml(
                            role.replace("<color", "<font color").replace("</color>", "</font>"),
                            HtmlCompat.FROM_HTML_MODE_LEGACY
                        ).toAnnotatedString(),
                        color = contentColor,
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontWeight = FontWeight.Normal,
                            fontSize = 19.sp
                        ),
                        modifier = Modifier.animateContentSize()
                    )

                    FadeVisibility(alwaysShowRaw || (expanded == true && rawDifferent) || description != null) {
                        Text(
                            text = description ?: role,
                            color = contentColor,
                            style = MaterialTheme.typography.bodyMedium.copy(
                                fontFamily = FontFamily.Monospace
                            ),
                            modifier = Modifier
                                .alpha(0.7f)
                                .animateContentSize()
                        )
                    }
                }
                expanded?.let {
                    ToggleExpandButton(
                        expanded = it,
                        onClick = onClick,
                        modifier = Modifier.padding(end = 8.dp)
                    )
                }
            }
        }
    ) {
        VerticalSegmentor(
            {
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
            },
            {
                if (rawDifferent) ExpressiveButtonRow(
                    title = stringResource(R.string.mapsRoles_copyRoleRaw),
                    icon = {
                        ExpressiveRowIcon(
                            painter = rememberVectorPainter(Icons.Rounded.CopyAll)
                        )
                    }
                ) { scope.launch {
                    clipboard.setClipEntry(ClipEntry(ClipData.newPlainText(
                        context.getString(R.string.mapsRoles_copyRoleClipLabel),
                        role
                    )))
                    topToastState?.showToast(R.string.info_copiedToClipboard, Icons.Rounded.CopyAll)
                } }
            },
            {
                ExpressiveButtonRow(
                    title = stringResource(R.string.mapsRoles_deleteRole),
                    contentColor = MaterialTheme.colorScheme.error,
                    icon = {
                        ExpressiveRowIcon(
                            painter = rememberVectorPainter(Icons.Rounded.Delete),
                            containerColor = MaterialTheme.colorScheme.error
                        )
                    }
                ) {
                    onRoleDelete(role)
                }
            },
            dynamic = true,
            modifier = Modifier
                .padding(start = 12.dp, end = 12.dp, bottom = 8.dp)
        )
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