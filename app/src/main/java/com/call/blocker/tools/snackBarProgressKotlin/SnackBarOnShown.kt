package com.call.blocker.tools.snackBarProgressKotlin

import com.tingyik90.snackprogressbar.SnackProgressBar
import com.tingyik90.snackprogressbar.SnackProgressBarManager

class SnackBarOnShown(private val onShown: (() -> Unit)) : SnackProgressBarManager.OnDisplayListener {

    override fun onShown(snackProgressBar: SnackProgressBar, onDisplayId: Int) {
        super.onShown(snackProgressBar, onDisplayId)
        onShown.invoke()
    }
}