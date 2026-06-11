package com.elmandadito.app

import android.app.Application
import com.elmandadito.app.network.NetworkMonitor
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class MandaditoApp : Application() {
    override fun onCreate() {
        super.onCreate()
        NetworkMonitor.init(this)
    }
}
