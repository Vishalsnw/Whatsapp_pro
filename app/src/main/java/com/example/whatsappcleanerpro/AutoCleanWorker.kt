package com.example.whatsappcleanerpro

import android.content.Context
import androidx.work.Worker
import androidx.work.WorkerParameters

class AutoCleanWorker(context: Context, workerParams: WorkerParameters) : Worker(context, workerParams) {

    override fun doWork(): Result {
        val categories = listOf("Images", "Videos", "Audio", "Documents", "Statuses", "Voice Notes")

        for (type in categories) {
            val files = FileScanner.getFilesForCategory(type)
            for (file in files) {
                if (shouldAutoDelete(file)) {
                    file.delete()
                }
            }
        }

        return Result.success()
    }

    private fun shouldAutoDelete(file: java.io.File): Boolean {
        val ageLimit = 7 * 24 * 60 * 60 * 1000L // 7 days in ms
        val age = System.currentTimeMillis() - file.lastModified()
        return age > ageLimit
    }
}
