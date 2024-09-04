package com.aliernfrog.lactool.data

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Error
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.res.stringResource
import com.aliernfrog.lactool.R
import com.aliernfrog.lactool.ui.component.ErrorWithIcon

data class MediaViewData(
    val model: Any?,
    val title: String? = null,
    val zoomEnabled: Boolean = true,
    val errorContent: @Composable () -> Unit = {
        ErrorWithIcon(
            error = stringResource(R.string.warning_error),
            painter = rememberVectorPainter(Icons.Rounded.Error),
            contentColor = Color.Red
        )
    },
    val options: (@Composable () -> Unit)? = null
)