package com.aliernfrog.lactool.ui.viewmodel

import android.content.ComponentName
import android.content.Context
import android.content.ServiceConnection
import android.content.pm.PackageManager
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
import com.aliernfrog.lactool.util.manager.PreferenceManager
import com.aliernfrog.toptoast.state.TopToastState
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import rikka.shizuku.Shizuku


class ShizukuViewModel(
    val prefs: PreferenceManager,
    val topToastState: TopToastState,
    context: Context
) : ViewModel() {
    companion object {
        const val SHIZUKU_PACKAGE = "moe.shizuku.privileged.api"
    }

    var status by mutableStateOf(ShizukuStatus.UNKNOWN)
    val installed: Boolean
        get() = status != ShizukuStatus.NOT_INSTALLED && status != ShizukuStatus.UNKNOWN

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
            if (prefs.shizukuNeverLoad.value) return;
            Log.d(TAG, "user service connected")
            fileService = IFileService.Stub.asInterface(binder)
            fileServiceRunning = true
            timeOutJob?.cancel()
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
            if (!isInstalled(context)) ShizukuStatus.NOT_INSTALLED
            else if (!Shizuku.pingBinder()) ShizukuStatus.WAITING_FOR_BINDER
            else {
                timeOutJob?.cancel()
                timedOut = false
                if (Shizuku.checkSelfPermission() == PackageManager.PERMISSION_GRANTED) ShizukuStatus.AVAILABLE
                else ShizukuStatus.UNAUTHORIZED
            }
        } catch (e: Exception) {
            Log.e(TAG, "updateStatus: ", e)
            ShizukuStatus.UNKNOWN
        }
        if (status == ShizukuStatus.AVAILABLE) {
            if (timeOutJob != null) timeOutJob = viewModelScope.launch {
                delay(15000)
                timedOut = true
            }
            Shizuku.bindUserService(userServiceArgs, userServiceConnection)
        }
        return status
    }

    private fun isInstalled(context: Context): Boolean {
        return try {
            context.packageManager.getPackageInfo(SHIZUKU_PACKAGE, 0) != null
        } catch (e: Exception) {
            Log.e(TAG, "isInstalled: ", e)
            false
        }
    }

    override fun onCleared() {
        Shizuku.removeBinderReceivedListener(binderReceivedListener)
        Shizuku.removeBinderDeadListener(binderDeadListener)
        Shizuku.removeRequestPermissionResultListener(permissionResultListener)
        super.onCleared()
    }
}