package com.example.whatsappcleanerpro.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.example.whatsappcleanerpro.service.CleanerService
import android.util.Log

class BootCompletedReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context?, intent: Intent?) {
        if (intent?.action == Intent.ACTION_BOOT_COMPLETED) {
            Log.d("BootCompletedReceiver", "Device rebooted - starting CleanerService")
            context?.let {
                val serviceIntent = Intent(it, CleanerService::class.java)
                it.startForegroundService(serviceIntent)
            }
        }
    }
}
