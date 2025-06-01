package com.example.whatsappcleanerpro

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Intent
import android.os.Build
import android.os.Environment
import android.os.IBinder
import android.util.Log
import androidx.core.app.NotificationCompat
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.File

class AutoCleanService : Service() {

    private val CHANNEL_ID = "WhatsAppCleanerPro_Channel"

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
        startForeground(1, createNotification("WhatsApp Cleaner is cleaning..."))

        // Run clean task on background thread
        CoroutineScope(Dispatchers.IO).launch {
            cleanWhatsAppScopedStorage()
            stopSelf()
        }
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "WhatsApp Cleaner Background",
                NotificationManager.IMPORTANCE_LOW
            )
            val manager = getSystemService(NotificationManager::class.java)
            manager.createNotificationChannel(channel)
        }
    }

    private fun createNotification(contentText: String): Notification {
        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("WhatsApp Cleaner Pro")
            .setContentText(contentText)
            .setSmallIcon(R.mipmap.ic_launcher)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .build()
    }

    private fun cleanWhatsAppScopedStorage() {
        try {
            val root = File(Environment.getExternalStorageDirectory(), "Android/media/com.whatsapp/WhatsApp")

            val targets = listOf(
                "Media/WhatsApp Images",
                "Media/WhatsApp Video",
                "Media/WhatsApp Audio",
                "Media/WhatsApp Documents",
                "Media/WhatsApp Voice Notes",
                "Media/WhatsApp Stickers",
                "Media/WhatsApp Animated Gifs",
                "Cache"
            )

            var deletedAny = false

            for (path in targets) {
                val dir = File(root, path)
                if (dir.exists() && dir.isDirectory) {
                    val deleted = deleteRecursive(dir)
                    Log.d("AutoCleanService", "Cleaned: $path → $deleted")
                    if (deleted) deletedAny = true
                } else {
                    Log.d("AutoCleanService", "Not found: $path")
                }
            }

            if (deletedAny) {
                Log.d("AutoCleanService", "WhatsApp files cleaned successfully.")
            } else {
                Log.d("AutoCleanService", "No files deleted (nothing found or no access).")
            }
        } catch (e: Exception) {
            Log.e("AutoCleanService", "Cleanup error: ${e.message}", e)
        }
    }

    private fun deleteRecursive(fileOrDirectory: File): Boolean {
        var success = false
        if (fileOrDirectory.isDirectory) {
            fileOrDirectory.listFiles()?.forEach {
                success = deleteRecursive(it) || success
            }
        }
        return fileOrDirectory.delete() || success
    }

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }
}
