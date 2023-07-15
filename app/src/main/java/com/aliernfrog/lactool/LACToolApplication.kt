package com.aliernfrog.lactool

import android.app.Application
import com.aliernfrog.lactool.di.appModules
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.startKoin

class LACToolApplication : Application() {
    override fun onCreate() {
        super.onCreate()

        startKoin {
            androidContext(this@LACToolApplication)
            modules(appModules)
        }
    }
}