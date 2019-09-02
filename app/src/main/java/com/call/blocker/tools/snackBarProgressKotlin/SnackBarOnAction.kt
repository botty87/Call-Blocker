package com.call.blocker.tools.snackBarProgressKotlin

import com.tingyik90.snackprogressbar.SnackProgressBar

class SnackBarOnAction(private val onAction: (() -> Unit)): SnackProgressBar.OnActionClickListener {
    override fun onActionClick() {
        onAction.invoke()
    }
}