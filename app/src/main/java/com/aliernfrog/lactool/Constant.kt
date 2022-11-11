package com.aliernfrog.lactool

import android.os.Environment
import com.aliernfrog.lactool.utils.AppUtil

object ConfigKey {
    val DEFAULT_APP_PATH = "${AppUtil.getAppPath()}/LacMapTool"
    val DEFAULT_LAC_PATH = "${Environment.getExternalStorageDirectory()}/Android/data/com.MA.LAC/files"
}