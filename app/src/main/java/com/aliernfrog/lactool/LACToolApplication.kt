package com.aliernfrog.lactool

import android.app.Application
import com.aliernfrog.lactool.di.appModules
import com.aliernfrog.lactool.ui.activity.CrashHandlerActivity
import io.github.aliernfrog.shared.di.getKoinInstance
import io.github.aliernfrog.shared.impl.VersionManager
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.startKoin
import kotlin.system.exitProcess

class LACToolApplication : Application() {
    override fun onCreate() {
        super.onCreate()

        Thread.setDefaultUncaughtExceptionHandler { _, throwable ->
            CrashHandlerActivity.start(
                context = this,
                throwable = throwable,
                debugInfo = getKoinInstance<VersionManager>().getDebugInfo()
            )
            exitProcess(1)
        }

        startKoin {
            androidContext(this@LACToolApplication)
            modules(appModules)
        }
    }
}