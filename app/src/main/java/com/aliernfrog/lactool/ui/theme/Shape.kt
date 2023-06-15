package com.aliernfrog.lactool.ui.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Shapes
import androidx.compose.ui.unit.dp

val AppRoundnessSize = 28.dp
val AppComponentShape = RoundedCornerShape(AppRoundnessSize)
val AppInnerComponentShape = RoundedCornerShape(6.dp)
val AppBottomSheetShape = RoundedCornerShape(topStart = AppRoundnessSize, topEnd = AppRoundnessSize)

val Shapes = Shapes(
    small = RoundedCornerShape(4.dp),
    medium = RoundedCornerShape(4.dp),
    large = RoundedCornerShape(0.dp)
)