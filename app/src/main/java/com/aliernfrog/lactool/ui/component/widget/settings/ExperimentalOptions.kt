package com.aliernfrog.lactool.ui.component.widget.settings

import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import io.github.aliernfrog.shared.ui.component.VerticalSegmentor
import io.github.aliernfrog.shared.ui.component.expressive.ExpressiveButtonRow
import io.github.aliernfrog.shared.ui.component.expressive.ExpressiveSection

@Composable
fun ExperimentalOptions(
    onSetIndeterminateProgressRequest: () -> Unit
) {
    ExpressiveSection(title = "Progress") {
        VerticalSegmentor(
            {
                ExpressiveButtonRow(
                    title = "Set indeterminate progress",
                    onClick = onSetIndeterminateProgressRequest
                )
            },
            modifier = Modifier.padding(horizontal = 12.dp)
        )
    }
}