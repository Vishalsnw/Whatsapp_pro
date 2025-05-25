package com.example.whatsappcleanerpro.service

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Intent
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationCompat
import android.util.Log
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.io.File

class CleanerService : Service() {

    private val CHANNEL_ID = "CleanerServiceChannel"

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
        val notification: Notification = NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("WhatsApp Cleaner Pro")
            .setContentText("Cleaning in progress...")
            .setSmallIcon(android.R.drawable.ic_menu_delete)
            .build()
        startForeground(1, notification)

        // Start cleaning periodically
        GlobalScope.launch {
            while (true) {
                cleanWhatsAppFiles()
                delay(1000 * 60 * 60) // Run every 1 hour
            }
        }
    }

    private fun cleanWhatsAppFiles() {
        try {
            val waDir = File("/storage/emulated/0/WhatsApp/Media")
            if (waDir.exists() && waDir.isDirectory) {
                val files = waDir.listFiles()
                files?.forEach { file ->
                    if (shouldDelete(file)) {
                        val deleted = file.deleteRecursively()
                        Log.d("CleanerService", "Deleted ${file.name}: $deleted")
                    }
                }
            }
        } catch (e: Exception) {
            Log.e("CleanerService", "Error cleaning WhatsApp files", e)
        }
    }

    private fun shouldDelete(file: File): Boolean {
        // Customize your deletion logic here, e.g., based on file size, type or age
        val maxAgeMillis = 7 * 24 * 60 * 60 * 1000L // 7 days
        return (System.currentTimeMillis() - file.lastModified()) > maxAgeMillis
    }

    override fun onBind(intent: Intent?): IBinder? = null

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val serviceChannel = NotificationChannel(
                CHANNEL_ID,
                "WhatsApp Cleaner Service Channel",
                NotificationManager.IMPORTANCE_LOW
            )
            val manager = getSystemService(NotificationManager::class.java)
            manager.createNotificationChannel(serviceChannel)
        }
    }
}
