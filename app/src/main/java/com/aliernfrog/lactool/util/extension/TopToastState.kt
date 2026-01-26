package com.aliernfrog.lactool.util.extension

import com.aliernfrog.lactool.R
import com.aliernfrog.toptoast.state.TopToastState
import io.github.aliernfrog.shared.util.extension.showErrorToast

fun TopToastState.showErrorToast(text: Any = R.string.warning_error) {
    showErrorToast(
        text = text
    )
}