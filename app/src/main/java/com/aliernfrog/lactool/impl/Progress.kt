package com.aliernfrog.lactool.impl

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue

class ProgressState {
    var currentProgress by mutableStateOf<Progress?>(null)
}

class Progress(
    val description: String,
    totalProgress: Long,
    passedProgress: Long = 0,
    steps: List<Progress> = emptyList()
) {
    constructor(
        description: String,
        steps: List<Progress> = emptyList()
    ) : this(
        description = description,
        totalProgress = 0,
        passedProgress = 0,
        steps = steps
    )

    private val indeterminate: Boolean = totalProgress <= 1

    val finished: Boolean = if (indeterminate) passedProgress > 0
    else if (steps.isEmpty()) totalProgress <= passedProgress
    else !steps.any { !it.finished }

    val percentage: Int? = if (indeterminate) null
    else if (finished) 100
    else if (steps.isEmpty()) ((100*passedProgress)/totalProgress).toInt()
    else {
        val percentagePerStep = 100/steps.size
        var total = 0
        steps.forEach {
            total += ((percentagePerStep*(it.percentage ?: 0))/100)
        }
        total
    }
}