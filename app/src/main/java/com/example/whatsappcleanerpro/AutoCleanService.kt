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
        startForeground(1, createNotification("WhatsApp Cleaner Pro is running"))

        // Start the cleaning task in background
        CoroutineScope(Dispatchers.IO).launch {
            cleanWhatsAppFiles()
        }
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "WhatsApp Cleaner Pro Service",
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

    private fun cleanWhatsAppFiles() {
        try {
            // WhatsApp media folder path
            val whatsappMedia = File(Environment.getExternalStorageDirectory(), "WhatsApp/Media")

            if (whatsappMedia.exists() && whatsappMedia.isDirectory) {
                deleteRecursive(whatsappMedia)
                Log.d("AutoCleanService", "WhatsApp Media cleaned successfully")
            } else {
                Log.d("AutoCleanService", "WhatsApp Media folder not found")
            }
        } catch (e: Exception) {
            Log.e("AutoCleanService", "Error cleaning WhatsApp files: ${e.message}")
        }

        // Stop the service after cleaning
        stopSelf()
    }

    private fun deleteRecursive(fileOrDirectory: File) {
        if (fileOrDirectory.isDirectory) {
            fileOrDirectory.listFiles()?.forEach {
                deleteRecursive(it)
            }
        }
        fileOrDirectory.delete()
    }

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }
}
