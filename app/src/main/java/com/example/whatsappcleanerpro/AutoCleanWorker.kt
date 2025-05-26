package com.example.whatsappcleanerpro

import android.content.Context
import android.os.Environment
import androidx.work.Worker
import androidx.work.WorkerParameters
import java.io.File

class AutoCleanWorker(appContext: Context, workerParams: WorkerParameters) :
    Worker(appContext, workerParams) {

    override fun doWork(): Result {
        return try {
            val paths = listOf(
                "/WhatsApp/Media",
                "/WhatsApp/Cache"
            )
            var deleted = false
            for (path in paths) {
                val file = File(Environment.getExternalStorageDirectory(), path)
                deleted = deleteContents(file) || deleted
            }

            // Optional: Add logs or local notification here

            Result.success()
        } catch (e: Exception) {
            e.printStackTrace()
            Result.failure()
        }
    }

    private fun deleteContents(dir: File): Boolean {
        if (!dir.exists() || !dir.isDirectory) return false
        var success = false
        dir.listFiles()?.forEach {
            success = if (it.isDirectory) deleteContents(it) || success else it.delete() || success
        }
        return success
    }
    }
