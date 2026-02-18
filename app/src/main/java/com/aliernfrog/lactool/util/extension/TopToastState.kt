package com.aliernfrog.lactool.util.extension

import com.aliernfrog.lactool.R
import com.aliernfrog.toptoast.state.TopToastState
import io.github.aliernfrog.shared.util.extension.showErrorToast
import io.github.aliernfrog.shared.util.extension.showReportableErrorToast

fun TopToastState.showErrorToast(text: Any = R.string.warning_error) {
    showErrorToast(
        text = text
    )
}

fun TopToastState.showReportableErrorToast(
    throwable: Throwable
) {
    showReportableErrorToast(
        text = R.string.warning_error_tapToReport,
        throwable = throwable
    )
}