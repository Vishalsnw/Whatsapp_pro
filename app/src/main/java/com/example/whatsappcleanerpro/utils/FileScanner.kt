package com.example.whatsappcleanerpro.utils

import android.os.Environment
import java.io.File

object FileScanner {

    fun getFilesForCategory(category: String): List<File> {
        val baseDir = File(Environment.getExternalStorageDirectory(), "WhatsApp/Media")

        val targetDir = when (category.lowercase()) {
            "images"       -> File(baseDir, "WhatsApp Images")
            "videos"       -> File(baseDir, "WhatsApp Video")
            "audio"        -> File(baseDir, "WhatsApp Audio")
            "documents"    -> File(baseDir, "WhatsApp Documents")
            "statuses"     -> File(baseDir, ".Statuses")
            "voice notes"  -> File(baseDir, "WhatsApp Voice Notes")
            else           -> baseDir
        }

        if (!targetDir.exists() || !targetDir.isDirectory) return emptyList()

        return targetDir.walkTopDown()
            .filter { it.isFile && !it.name.startsWith(".") && it.name != ".nomedia" }
            .toList()
    }
}
