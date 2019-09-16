package com.botty.callblocker.data

import java.util.*

data class SyncReport(
    val deviceID: String,
    val startTime: Date,
    val endTime: Date,
    val exception: String?,
    val deviceModel: String? = android.os.Build.MODEL,
    val deviceManufacturer: String? = android.os.Build.MANUFACTURER,
    val deviceProduct: String? = android.os.Build.PRODUCT
)