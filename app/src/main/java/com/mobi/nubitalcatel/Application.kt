package com.mobi.nubitalcatel

import android.app.Application
import com.mobi.nubitalcatel.core.network.NetworkModule

class MyApp : Application() {
    override fun onCreate() {
        super.onCreate()
        NetworkModule.init(this)
    }
}
