package com.example.whatsappcleanerpro

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.provider.Settings
import android.widget.*
import androidx.activity.result.contract.ActivityResultContracts
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

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        tvStorageInfo = findViewById(R.id.tvStorageInfo)
        tvStatus = findViewById(R.id.tvStatus)
        btnCleanCache = findViewById(R.id.btnCleanCache)
        btnAutoClean = findViewById(R.id.btnAutoClean)
        storageProgress = findViewById(R.id.storageProgress)

        btnCleanCache.setOnClickListener {
            if (hasAllPermissions()) {
                cleanWhatsAppCache()
            } else {
                requestNecessaryPermissions()
            }
        }

        btnAutoClean.setOnClickListener {
            toggleAutoClean()
        }

        updateStorageInfo()
        updateAutoCleanButton()
    }

    private fun hasAllPermissions(): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            Environment.isExternalStorageManager()
        } else {
            ContextCompat.checkSelfPermission(
                this, Manifest.permission.READ_EXTERNAL_STORAGE
            ) == PackageManager.PERMISSION_GRANTED
        }
    }

    private fun requestNecessaryPermissions() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            try {
                val intent = Intent(Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION)
                intent.data = Uri.parse("package:$packageName")
                startActivity(intent)
            } catch (e: Exception) {
                val intent = Intent(Settings.ACTION_MANAGE_ALL_FILES_ACCESS_PERMISSION)
                startActivity(intent)
            }
        } else {
            requestPermissionsLauncher.launch(arrayOf(
                Manifest.permission.READ_EXTERNAL_STORAGE,
                Manifest.permission.WRITE_EXTERNAL_STORAGE
            ))
        }
    }

    private val requestPermissionsLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        if (permissions.values.all { it }) {
            cleanWhatsAppCache()
        } else {
            tvStatus.text = "Permissions denied. Cannot clean cache."
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
        val scopedPath = File(
            Environment.getExternalStorageDirectory(),
            "Android/media/com.whatsapp/WhatsApp"
        )
        return getFolderSize(scopedPath)
    }

    private fun getFolderSize(dir: File): Long {
        if (!dir.exists() || !dir.isDirectory) return 0L
        return dir.listFiles()?.sumOf {
            if (it.isDirectory) getFolderSize(it) else it.length()
        } ?: 0L
    }

    private fun cleanWhatsAppCache() {
        tvStatus.text = "Cleaning cache..."

        val root = File(
            Environment.getExternalStorageDirectory(),
            "Android/media/com.whatsapp/WhatsApp"
        )

        val deleted = listOf(
            "Media/WhatsApp Images",
            "Media/WhatsApp Video",
            "Media/WhatsApp Audio",
            "Media/WhatsApp Documents",
            "Media/WhatsApp Voice Notes",
            "Media/WhatsApp Stickers",
            "Media/WhatsApp Animated Gifs",
            "Cache"
        ).map {
            deleteContents(File(root, it))
        }.any { it }

        tvStatus.text = if (deleted) "Cache cleaned." else "No files found or no permission."
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
        return try {
            val statuses = WorkManager.getInstance(applicationContext)
                .getWorkInfosByTag(workTag)
                .get()
            statuses.any { it.state == WorkInfo.State.ENQUEUED || it.state == WorkInfo.State.RUNNING }
        } catch (e: Exception) {
            false
        }
    }
}
