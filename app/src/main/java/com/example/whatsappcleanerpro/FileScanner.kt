package com.example.whatsappcleanerpro

import android.os.Environment
import java.io.File

object FileScanner {

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
}
