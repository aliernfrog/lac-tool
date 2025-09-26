package com.aliernfrog.lactool.ui.component.expressive

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

val ROW_DEFAULT_ICON_SIZE = 40.dp

val Color.toRowFriendlyColor
    get() = copy(alpha = 0.25f)