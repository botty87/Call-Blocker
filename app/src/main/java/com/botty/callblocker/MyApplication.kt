package com.botty.callblocker

import android.app.Application
import com.google.android.gms.ads.MobileAds
import net.danlew.android.joda.JodaTimeAndroid
import timber.log.Timber




class MyApplication: Application() {
    override fun onCreate() {
        super.onCreate()
        if (BuildConfig.DEBUG) {
            Timber.plant(Timber.DebugTree())
        }
        JodaTimeAndroid.init(this)
        MobileAds.initialize(this, "ca-app-pub-9694877750002081~7217166383")
    }
}