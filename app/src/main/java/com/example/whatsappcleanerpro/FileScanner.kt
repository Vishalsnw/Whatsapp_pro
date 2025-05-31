package com.example.whatsappcleanerpro

import android.os.Environment
import java.io.File

object FileScanner {

    // Returns all WhatsApp media and cache files
    fun getWhatsAppMediaFiles(): List<FileModel> {
        val mediaDirs = listOf(
            "/WhatsApp/Media",
            "/WhatsApp/Cache"
        )

        val root = Environment.getExternalStorageDirectory()
        val files = mutableListOf<FileModel>()

        for (dirPath in mediaDirs) {
            val dir = File(root.absolutePath + dirPath)
            if (dir.exists() && dir.isDirectory) {
                dir.walkTopDown().forEach { file ->
                    if (file.isFile) {
                        files.add(
                            FileModel(
                                file = file,
                                name = file.name,
                                size = file.length(),
                                path = file.absolutePath
                            )
                        )
                    }
                }
            }
        }

        return files
    }

    // Returns files for a specific category/type
    fun getFilesForCategory(type: String): List<FileModel> {
        // You can extend this logic for more categories as needed
        return when (type.lowercase()) {
            "media" -> getWhatsAppMediaFiles()
            // Add more categories here if needed
            else -> emptyList()
        }
    }
}
