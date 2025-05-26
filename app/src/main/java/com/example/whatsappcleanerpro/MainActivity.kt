package com.example.whatsappcleanerpro

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.work.*
import java.io.File
import java.util.concurrent.TimeUnit

class MainActivity : AppCompatActivity() {

    private lateinit var tvStorageInfo: TextView
    private lateinit var tvStatus: TextView
    private lateinit var btnCleanCache: Button
    private lateinit var btnAutoClean: Button
    private lateinit var storageProgress: ProgressBar

    private val workTag = "whatsapp_auto_clean"
    private val PERMISSION_REQUEST_CODE = 1001

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        tvStorageInfo = findViewById(R.id.tvStorageInfo)
        tvStatus = findViewById(R.id.tvStatus)
        btnCleanCache = findViewById(R.id.btnCleanCache)
        btnAutoClean = findViewById(R.id.btnAutoClean)
        storageProgress = findViewById(R.id.storageProgress)

        btnCleanCache.setOnClickListener {
            if (checkAndRequestPermissions()) {
                cleanWhatsAppCache()
            }
        }

        btnAutoClean.setOnClickListener {
            toggleAutoClean()
        }

        updateStorageInfo()
        updateAutoCleanButton()
    }

    private fun checkAndRequestPermissions(): Boolean {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            val permissions = arrayOf(
                Manifest.permission.READ_EXTERNAL_STORAGE,
                Manifest.permission.WRITE_EXTERNAL_STORAGE
            )
            val needed = permissions.filter {
                ContextCompat.checkSelfPermission(this, it) != PackageManager.PERMISSION_GRANTED
            }
            return if (needed.isNotEmpty()) {
                requestPermissions(needed.toTypedArray(), PERMISSION_REQUEST_CODE)
                false
            } else {
                true
            }
        }
        return true
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == PERMISSION_REQUEST_CODE) {
            if (grantResults.all { it == PackageManager.PERMISSION_GRANTED }) {
                cleanWhatsAppCache()
            } else {
                tvStatus.text = "Permissions denied. Cannot clean cache."
            }
        }
    }

    private fun updateStorageInfo() {
        val sizeBytes = getWhatsAppCacheSize()
        val sizeMb = sizeBytes / (1024.0 * 1024.0)
        val usedPercent = ((sizeMb / 1024.0) * 100).toInt().coerceAtMost(100)
        tvStorageInfo.text = "Used by WhatsApp: %.2f MB".format(sizeMb)
        storageProgress.progress = usedPercent
    }

    private fun getWhatsAppCacheSize(): Long {
        val paths = listOf(
            Environment.getExternalStorageDirectory().absolutePath + "/WhatsApp/Media",
            Environment.getExternalStorageDirectory().absolutePath + "/WhatsApp/Cache"
        )
        return paths.sumOf { getFolderSize(File(it)) }
    }

    private fun getFolderSize(dir: File): Long {
        if (!dir.exists() || !dir.isDirectory) return 0L
        return dir.listFiles()?.sumOf {
            if (it.isDirectory) getFolderSize(it) else it.length()
        } ?: 0L
    }

    private fun cleanWhatsAppCache() {
        tvStatus.text = "Cleaning cache..."
        val deleted = listOf(
            "/WhatsApp/Media",
            "/WhatsApp/Cache"
        ).map {
            deleteContents(File(Environment.getExternalStorageDirectory(), it))
        }.any { it }
        tvStatus.text = if (deleted) "Cache cleaned." else "No files found."
        updateStorageInfo()
    }

    private fun deleteContents(dir: File): Boolean {
        if (!dir.exists() || !dir.isDirectory) return false
        var success = false
        dir.listFiles()?.forEach {
            success = if (it.isDirectory) deleteContents(it) || success else it.delete() || success
        }
        return success
    }

    private fun toggleAutoClean() {
        val wm = WorkManager.getInstance(applicationContext)
        if (isAutoCleanEnabled()) {
            wm.cancelAllWorkByTag(workTag)
            tvStatus.text = "Auto clean disabled."
        } else {
            val constraints = Constraints.Builder()
                .setRequiresBatteryNotLow(true)
                .build()

            val request = PeriodicWorkRequestBuilder<AutoCleanWorker>(1, TimeUnit.DAYS)
                .setConstraints(constraints)
                .addTag(workTag)
                .build()

            wm.enqueueUniquePeriodicWork(workTag, ExistingPeriodicWorkPolicy.REPLACE, request)
            tvStatus.text = "Auto clean enabled."
        }
        updateAutoCleanButton()
    }

    private fun updateAutoCleanButton() {
        btnAutoClean.text = if (isAutoCleanEnabled()) "Disable Auto Clean" else "Enable Auto Clean"
    }

    private fun isAutoCleanEnabled(): Boolean {
        val statuses = WorkManager.getInstance(applicationContext)
            .getWorkInfosByTag(workTag).get()
        return statuses.any { it.state == WorkInfo.State.ENQUEUED || it.state == WorkInfo.State.RUNNING }
    }
}
