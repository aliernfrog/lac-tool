package com.aliernfrog.lactool.util.extension

import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke

fun Stroke.copy(
    width: Float = this.width,
    miter: Float = this.miter,
    cap: StrokeCap = this.cap,
    join: StrokeJoin = this.join,
    pathEffect: PathEffect? = this.pathEffect
) = Stroke(
    width = width,
    miter = miter,
    cap = cap,
    join = join,
    pathEffect = pathEffect
)