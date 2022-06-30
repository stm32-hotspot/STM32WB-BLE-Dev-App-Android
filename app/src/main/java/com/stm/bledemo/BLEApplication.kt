package com.stm.bledemo

import android.app.Application
import timber.log.Timber

class BLEApplication: Application() {
    override fun onCreate() {
        super.onCreate()
        app = this

        // Install Timber for Logging
        if (BuildConfig.DEBUG) {
            Timber.plant(Timber.DebugTree())
        }
    }

    companion object {
        lateinit var app: Application
    }
}