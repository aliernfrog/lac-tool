package com.aliernfrog.lactool.ui.screen

import android.graphics.Typeface
import android.text.Spanned
import android.text.style.ForegroundColorSpan
import android.text.style.StyleSpan
import android.text.style.UnderlineSpan
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.text.HtmlCompat.FROM_HTML_MODE_LEGACY
import androidx.core.text.HtmlCompat.fromHtml
import com.aliernfrog.lactool.LACToolComposableShape
import com.aliernfrog.lactool.R
import com.aliernfrog.lactool.state.MapsEditState

@Composable
fun MapsRolesScreen(mapsEditState: MapsEditState) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        state = mapsEditState.rolesLazyListState
    ) {
        item {
            Text(
                text = stringResource(R.string.mapsRoles_showingCount).replace("%COUNT%", mapsEditState.mapRoles?.size.toString()),
                modifier = Modifier.padding(horizontal = 8.dp)
            )
        }
        items(mapsEditState.mapRoles ?: mutableListOf()) {
            Role(it)
        }
    }
}

@Composable
private fun Role(role: String) {
    Row(Modifier.fillMaxWidth().padding(8.dp).clip(LACToolComposableShape).background(MaterialTheme.colorScheme.surfaceVariant)) {
        Text(
            text = fromHtml(
                role.replace("<color", "<font color").replace("</color>", "</font>"),
                FROM_HTML_MODE_LEGACY
            ).toAnnotatedString(),
            modifier = Modifier.padding(8.dp),
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold
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