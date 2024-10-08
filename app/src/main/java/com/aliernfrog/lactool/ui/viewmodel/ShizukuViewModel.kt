package com.aliernfrog.lactool.ui.viewmodel

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.content.pm.PackageManager
import android.net.Uri
import android.os.IBinder
import android.util.Log
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.aliernfrog.lactool.BuildConfig
import com.aliernfrog.lactool.IFileService
import com.aliernfrog.lactool.R
import com.aliernfrog.lactool.TAG
import com.aliernfrog.lactool.enum.ShizukuStatus
import com.aliernfrog.lactool.service.FileService
import com.aliernfrog.lactool.util.extension.showErrorToast
import com.aliernfrog.lactool.util.manager.PreferenceManager
import com.aliernfrog.toptoast.state.TopToastState
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import rikka.shizuku.Shizuku
import java.io.File


class ShizukuViewModel(
    val prefs: PreferenceManager,
    val topToastState: TopToastState,
    context: Context
) : ViewModel() {
    companion object {
        const val SHIZUKU_PACKAGE = "moe.shizuku.privileged.api"
        const val SHIZUKU_PLAY_STORE = "https://play.google.com/store/apps/details?id=moe.shizuku.privileged.api"
        const val SUI_GITHUB = "https://github.com/RikkaApps/Sui"
    }

    var status by mutableStateOf(ShizukuStatus.UNKNOWN)
    val managerInstalled: Boolean
        get() = status != ShizukuStatus.NOT_INSTALLED && status != ShizukuStatus.UNKNOWN
    val deviceRooted = System.getenv("PATH")?.split(":")?.any { path ->
        File(path, "su").canExecute()
    } ?: false

    var fileService: IFileService? = null
    var fileServiceRunning by mutableStateOf(false)
    var timedOut by mutableStateOf(false)

    private var timeOutJob: Job? = null
    private val binderReceivedListener = Shizuku.OnBinderReceivedListener {
        checkAvailability(context)
    }
    private val binderDeadListener = Shizuku.OnBinderDeadListener {
        fileServiceRunning = false
        checkAvailability(context)
    }
    private val permissionResultListener = Shizuku.OnRequestPermissionResultListener { _ /* requestCode */, _ /*grantResult*/ ->
        checkAvailability(context)
    }

    private val userServiceConnection = object : ServiceConnection {
        override fun onServiceConnected(componentName: ComponentName, binder: IBinder) {
            val shizukuNeverLoad = prefs.shizukuNeverLoad.value
            Log.d(TAG, "user service connected, shizukuNeverLoad: $shizukuNeverLoad")
            if (shizukuNeverLoad) return
            fileService = IFileService.Stub.asInterface(binder)
            fileServiceRunning = true
            timeOutJob?.cancel()
            timeOutJob = null
            timedOut = false
        }

        override fun onServiceDisconnected(componentName: ComponentName) {
            Log.d(TAG, "user service disconnected")
            fileServiceRunning = false
            topToastState.showToast(
                text = R.string.info_shizuku_disconnected,
                icon = Icons.Default.Info
            )
        }
    }

    private val userServiceArgs = Shizuku.UserServiceArgs(ComponentName(BuildConfig.APPLICATION_ID, FileService::class.java.name))
        .daemon(false)
        .processNameSuffix("service")
        .debuggable(BuildConfig.DEBUG)
        .version(BuildConfig.VERSION_CODE)


    init {
        Shizuku.addBinderReceivedListener(binderReceivedListener)
        Shizuku.addBinderDeadListener(binderDeadListener)
        Shizuku.addRequestPermissionResultListener(permissionResultListener)
    }

    fun checkAvailability(context: Context): ShizukuStatus {
        status = try {
            if (Shizuku.pingBinder()) {
                if (Shizuku.checkSelfPermission() == PackageManager.PERMISSION_GRANTED) ShizukuStatus.AVAILABLE
                else ShizukuStatus.UNAUTHORIZED
            } else if (!isManagerInstalled(context)) ShizukuStatus.NOT_INSTALLED
            else ShizukuStatus.WAITING_FOR_BINDER
        } catch (e: Exception) {
            Log.e(TAG, "ShizukuViewModel/checkAvailability: failed to determine status", e)
            ShizukuStatus.UNKNOWN
        }
        if (status == ShizukuStatus.AVAILABLE && !fileServiceRunning) {
            if (timeOutJob == null) timeOutJob = viewModelScope.launch {
                delay(15000)
                timedOut = true
            }
            Shizuku.bindUserService(userServiceArgs, userServiceConnection)
        }
        return status
    }

    fun launchManager(context: Context) {
        try {
            if (managerInstalled) context.startActivity(
                context.packageManager.getLaunchIntentForPackage(SHIZUKU_PACKAGE)
            ) else {
                val intent = Intent(Intent.ACTION_VIEW, Uri.parse(SHIZUKU_PLAY_STORE))
                context.startActivity(intent)
            }
        } catch (e: Exception) {
            Log.e(TAG, "ShizukuViewModel/launchManager: failed to start activity ", e)
            topToastState.showErrorToast()
        }
    }

    private fun isManagerInstalled(context: Context) = try {
        context.packageManager.getPackageInfo(SHIZUKU_PACKAGE, 0) != null
    } catch (e: Exception) {
        Log.e(TAG, "isInstalled: ", e)
        false
    }

    override fun onCleared() {
        Shizuku.removeBinderReceivedListener(binderReceivedListener)
        Shizuku.removeBinderDeadListener(binderDeadListener)
        Shizuku.removeRequestPermissionResultListener(permissionResultListener)
        super.onCleared()
    }
}