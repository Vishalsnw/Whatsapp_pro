package com.example.whatsappcleanerpro

import android.content.Context
import android.os.Environment
import androidx.work.Worker
import androidx.work.WorkerParameters
import java.io.File
import android.util.Log

class AutoCleanWorker(appContext: Context, workerParams: WorkerParameters) :
    Worker(appContext, workerParams) {

    override fun doWork(): Result {
        return try {
            val root = File(Environment.getExternalStorageDirectory(), "Android/media/com.whatsapp/WhatsApp")

            val paths = listOf(
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
            for (subPath in paths) {
                val dir = File(root, subPath)
                if (dir.exists() && dir.isDirectory) {
                    val deleted = deleteContents(dir)
                    Log.d("AutoCleanWorker", "Deleted contents in $subPath = $deleted")
                    if (deleted) deletedAny = true
                } else {
                    Log.d("AutoCleanWorker", "Path not found: $subPath")
                }
            }

            Log.d("AutoCleanWorker", if (deletedAny) "Auto clean success" else "Nothing deleted")
            Result.success()
        } catch (e: Exception) {
            Log.e("AutoCleanWorker", "Error during auto clean: ${e.message}", e)
            Result.failure()
        }
    }

    private fun deleteContents(dir: File): Boolean {
        var success = false
        dir.listFiles()?.forEach {
            success = if (it.isDirectory) deleteContents(it) || success else it.delete() || success
        }
        return success
    }
    }
