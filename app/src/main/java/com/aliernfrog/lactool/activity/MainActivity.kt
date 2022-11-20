package com.aliernfrog.lactool.activity

import android.Manifest
import android.annotation.SuppressLint
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.net.Uri
import android.os.*
import android.os.StrictMode.ThreadPolicy
import android.provider.DocumentsContract
import android.provider.Settings
import android.text.Html
import android.view.View
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.aliernfrog.LacMapTool.R
import com.aliernfrog.lactool.*
import com.aliernfrog.lactool.fragment.OkCancelSheet
import com.aliernfrog.lactool.fragment.OkCancelSheet.OkCancelListener
import com.aliernfrog.lactool.utils.AppUtil
import java.io.File

@Suppress("DEPRECATION")
@SuppressLint("CommitPrefEdits", "ClickableViewAccessibility")
class MainActivity : AppCompatActivity(), OkCancelListener {
    private lateinit var missingPerms: LinearLayout
    private lateinit var lacLinear: LinearLayout
    private lateinit var redirectMaps: LinearLayout
    private lateinit var redirectWallpapers: LinearLayout
    private lateinit var redirectScreenshots: LinearLayout
    private lateinit var appLinear: LinearLayout
    private lateinit var checkUpdates: LinearLayout
    private lateinit var redirectOptions: LinearLayout
    private lateinit var updateLinear: LinearLayout
    private lateinit var updateLinearTitle: TextView
    private lateinit var updateLog: TextView
    private lateinit var update: SharedPreferences
    private lateinit var config: SharedPreferences
    private lateinit var mapsPath: String
    private lateinit var wallpapersPath: String
    private lateinit var screenshotsPath: String

    private val requestUri = 1
    private var uriSdkVersion = 30
    private var hasPerms = true
    private var version = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        val policy = ThreadPolicy.Builder().permitAll().build()
        StrictMode.setThreadPolicy(policy)
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        update = getSharedPreferences("APP_UPDATE", MODE_PRIVATE)
        config = getSharedPreferences("APP_CONFIG", MODE_PRIVATE)
        uriSdkVersion = config.getInt("uriSdkVersion", 30)
        version = update.getInt("versionCode", 0)
        mapsPath = update.getString("path-maps", "")!!
        wallpapersPath = update.getString("path-wallpapers", "")!!
        screenshotsPath = update.getString("path-screenshots", "")!!
        missingPerms = findViewById(R.id.main_missingPerms)
        lacLinear = findViewById(R.id.main_optionsLac)
        redirectMaps = findViewById(R.id.main_maps)
        redirectWallpapers = findViewById(R.id.main_wallpapers)
        redirectScreenshots = findViewById(R.id.main_screenshots)
        appLinear = findViewById(R.id.main_optionsApp)
        checkUpdates = findViewById(R.id.main_checkUpdates)
        redirectOptions = findViewById(R.id.main_options)
        updateLinear = findViewById(R.id.main_update)
        updateLinearTitle = findViewById(R.id.main_update_title)
        updateLog = findViewById(R.id.main_update_description)
        checkUpdates(false)
        checkPerms()
        setListeners()
    }

    private fun fetchUpdates() {
        try {
            if (AppUtil.getUpdates(applicationContext)) checkUpdates(true)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun checkUpdates(toastResult: Boolean) {
        val latest = update.getInt("updateLatest", 0)
        val download = update.getString("updateDownload", null)
        val changelog = update.getString("updateChangelog", null)
        val changelogVersion = update.getString("updateChangelogVersion", null)
        val notes = update.getString("notes", null)
        val hasUpdate = latest > version
        var linearVisible = false
        var full: String? = ""
        if (hasUpdate) {
            linearVisible = true
            full = changelog + "<br /><br /><b>" + getString(R.string.optionsChangelogChangelog) + ":</b> " + changelogVersion
            updateLinearTitle.visibility = View.VISIBLE
            updateLinear.onClick { redirectURL(download) }
            if (toastResult) Toast.makeText(applicationContext, R.string.update_toastAvailable, Toast.LENGTH_SHORT).show()
        } else {
            if (notes != null && notes != "") {
                linearVisible = true
                full = notes
            }
            if (toastResult) Toast.makeText(applicationContext, R.string.update_toastNoUpdates, Toast.LENGTH_SHORT).show()
        }
        updateLog.text = Html.fromHtml(full)
        if (linearVisible) updateLinear.visibility = View.VISIBLE
    }

    @SuppressLint("NewApi")
    fun checkPerms() {
        if (Build.VERSION.SDK_INT in 23..29) {
            if (checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                afterPermsDenied()
                requestPermissions(arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE), 3)
            } else afterPermsGranted()
        } else if (Build.VERSION.SDK_INT >= 30) {
            if (!Environment.isExternalStorageManager()) {
                afterPermsDenied()
                showAllFilesAccessDialog()
            } else afterPermsGranted()
        } else afterPermsGranted()
    }

    private fun showAllFilesAccessDialog() {
        val bundle = Bundle()
        bundle.putString("text", getString(R.string.info_storagePermSdk30))
        val okCancelSheet = OkCancelSheet()
        okCancelSheet.arguments = bundle
        okCancelSheet.show(supportFragmentManager, "allfiles")
    }

    private fun afterPermsGranted() {
        hasPerms = true
        missingPerms.visibility = View.GONE
        createFiles()
    }

    private fun afterPermsDenied() {
        hasPerms = false
        missingPerms.visibility = View.VISIBLE
    }

    private fun createFiles() {
        try {
            val mapsFolder = File(update.getString("path-maps", "")!!)
            val wallpapersFolder = File(update.getString("path-wallpapers", "")!!)
            val screenshotsFolder = File(update.getString("path-screenshots", "")!!)
            val appFolder = File(update.getString("path-app", "")!!)
            val backupFolder = File(appFolder.path + "/backups/")
            val aBackupFolder = File(appFolder.path + "/auto-backups/")
            val tempMapsFolder = File(update.getString("path-temp-maps", "")!!)
            val tempWallpapersFolder = File(update.getString("path-temp-wallpapers", "")!!)
            val tempScreenshotsFolder = File(update.getString("path-temp-screenshots", "")!!)
            val nomedia = File(appFolder.path + "/.nomedia")
            if (!mapsFolder.exists()) mkdirs(mapsFolder)
            if (!wallpapersFolder.exists()) mkdirs(wallpapersFolder)
            if (!screenshotsFolder.exists()) mkdirs(screenshotsFolder)
            if (!appFolder.exists()) mkdirs(appFolder)
            if (!backupFolder.exists()) mkdirs(backupFolder)
            if (!aBackupFolder.exists()) mkdirs(aBackupFolder)
            if (!tempMapsFolder.exists()) mkdirs(tempMapsFolder)
            if (!tempWallpapersFolder.exists()) mkdirs(tempWallpapersFolder)
            if (!tempScreenshotsFolder.exists()) mkdirs(tempScreenshotsFolder)
            if (!nomedia.exists()) nomedia.createNewFile()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun mkdirs(mk: File) {
        mk.mkdirs()
    }

    @SuppressLint("NewApi")
    fun checkUriPerms(path: String?): Boolean {
        if (Build.VERSION.SDK_INT < uriSdkVersion) return true
        if (path == null) return true
        val treeId = path.replace(Environment.getExternalStorageDirectory().toString() + "/", "primary:")
        val uri = DocumentsContract.buildDocumentUri("com.android.externalstorage.documents", treeId)
        val treeUri = DocumentsContract.buildTreeDocumentUri("com.android.externalstorage.documents", treeId)
        val takeFlags = Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_GRANT_WRITE_URI_PERMISSION or Intent.FLAG_GRANT_PERSISTABLE_URI_PERMISSION
        return if (applicationContext.checkUriPermission(treeUri, Process.myPid(), Process.myUid(), Intent.FLAG_GRANT_READ_URI_PERMISSION) != PackageManager.PERMISSION_GRANTED) {
            Toast.makeText(applicationContext, R.string.info_treePerm, Toast.LENGTH_LONG).show()
            val intent = Intent(Intent.ACTION_OPEN_DOCUMENT_TREE)
                .putExtra(DocumentsContract.EXTRA_INITIAL_URI, uri)
                .putExtra(Intent.EXTRA_LOCAL_ONLY, true)
                .addFlags(takeFlags)
            startActivityForResult(intent, requestUri)
            false
        }
        else true
    }

    private fun switchActivity(i: Class<*>, allowWithoutPerms: Boolean? = false, path: String? = null) {
        if (!allowWithoutPerms!! && !hasPerms) checkPerms()
        else {
            val intent = Intent(this.applicationContext, i)
            if (checkUriPerms(path)) startActivity(intent)
        }
    }

    private fun redirectURL(url: String?) {
        val viewIntent = Intent("android.intent.action.VIEW", Uri.parse(url))
        startActivity(viewIntent)
    }

    @Deprecated("Deprecated in Java")
    @SuppressLint("NewApi")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == requestUri && data != null) {
            val takeFlags = Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_GRANT_WRITE_URI_PERMISSION
            grantUriPermission(applicationContext.packageName, data.data, takeFlags)
            applicationContext.contentResolver.takePersistableUriPermission(data.data!!, takeFlags)
        }
    }

    fun setListeners() {
        missingPerms.onClick { checkPerms() }
        redirectMaps.onClick { switchActivity(MapsActivity::class.java, false, mapsPath) }
        redirectWallpapers.onClick { switchActivity(WallpaperActivity::class.java, false, wallpapersPath) }
        redirectScreenshots.onClick { switchActivity(ScreenshotsActivity::class.java, false, screenshotsPath) }
        checkUpdates.onClick { fetchUpdates() }
        redirectOptions.onClick { switchActivity(OptionsActivity::class.java, true) }
    }

    @SuppressLint("InlinedApi")
    override fun onOkClick() {
        val intent = Intent(Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION)
        val uri = Uri.fromParts("package", packageName, null)
        intent.data = uri
        startActivity(intent)
    }
}