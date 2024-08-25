package com.aliernfrog.lactool.ui.component

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ProgressIndicatorDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.aliernfrog.lactool.R
import com.aliernfrog.lactool.impl.Progress

@Composable
fun HorizontalProgressIndicator(
    progress: Progress?,
    modifier: Modifier = Modifier,
    textColor: Color = Color.Unspecified,
    indicatorColor: Color? = null
) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(16.dp),
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier
    ) {
        CircularProgress(
            progress = progress,
            color = indicatorColor
        )
        Text(
            text = progress?.description ?: stringResource(R.string.info_pleaseWait),
            color = textColor
        )
    }
}

@Composable
fun VerticalProgressIndicator(
    progress: Progress?,
    modifier: Modifier = Modifier,
    textColor: Color = Color.Unspecified,
    indicatorColor: Color? = null
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
        modifier = modifier
    ) {
        CircularProgress(
            progress = progress,
            color = indicatorColor
        )
        Text(
            text = progress?.description ?: stringResource(R.string.info_pleaseWait),
            color = textColor,
            modifier = Modifier.padding(top = 8.dp)
        )
    }
}

@Composable
private fun CircularProgress(
    progress: Progress?,
    modifier: Modifier = Modifier,
    color: Color? = null
) {
    progress?.float.let {
        if (it == null || progress?.finished == true) return@let CircularProgressIndicator(
            trackColor = color ?: ProgressIndicatorDefaults.circularIndeterminateTrackColor,
            modifier = modifier
        )
        val animated by animateFloatAsState(it)
        CircularProgressIndicator(
            progress = { animated },
            color = color ?: ProgressIndicatorDefaults.circularColor,
            modifier = modifier
        )
    }
}