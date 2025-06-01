package com.example.whatsappcleanerpro

import android.content.Context
import android.net.Uri
import android.os.Environment
import android.Manifest
import android.content.pm.PackageManager
import androidx.core.content.ContextCompat
import androidx.documentfile.provider.DocumentFile
import java.io.File

object FileScanner {

    // Check permission for Android 10 and below
    fun hasReadPermission(context: Context): Boolean {
        return ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.READ_EXTERNAL_STORAGE
        ) == PackageManager.PERMISSION_GRANTED
    }

    // For Android 10 and below (direct file access)
    fun getFilesForCategory(context: Context, type: String): List<FileModel> {
        val files = mutableListOf<FileModel>()

        // ✅ Correct root path for legacy access (common issue)
        val root = File(
            Environment.getExternalStorageDirectory(),
            "Android/media/com.whatsapp/WhatsApp"
        )

        val categoryFolders = mapOf(
            "images" to "Media/WhatsApp Images",
            "video" to "Media/WhatsApp Video",
            "audio" to "Media/WhatsApp Audio",
            "documents" to "Media/WhatsApp Documents",
            "voice" to "Media/WhatsApp Voice Notes",
            "stickers" to "Media/WhatsApp Stickers",
            "gifs" to "Media/WhatsApp Animated Gifs"
        )

        val folderPath = categoryFolders[type.lowercase()]
        folderPath?.let {
            val targetDir = File(root, it)
            if (targetDir.exists() && targetDir.isDirectory) {
                targetDir.listFiles()?.forEach { file ->
                    if (file.isFile &&
                        !file.name.startsWith(".") &&
                        file.name != ".nomedia"
                    ) {
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

    // Android 11+ SAF-based access
    fun getFilesForCategorySAF(context: Context, pickedFolderUri: Uri, type: String): List<FileModel> {
        val files = mutableListOf<FileModel>()

        val categoryFolders = mapOf(
            "images" to "WhatsApp Images",
            "video" to "WhatsApp Video",
            "audio" to "WhatsApp Audio",
            "documents" to "WhatsApp Documents",
            "voice" to "WhatsApp Voice Notes",
            "stickers" to "WhatsApp Stickers",
            "gifs" to "WhatsApp Animated Gifs"
        )

        val folderName = categoryFolders[type.lowercase()]
        folderName?.let {
            val pickedDir = DocumentFile.fromTreeUri(context, pickedFolderUri)
            val subFolder = pickedDir?.findFile(it)
            if (subFolder != null && subFolder.isDirectory) {
                addFilesFromDocumentDir(subFolder, files)
            }
        }

        return files
    }

    // SAF recursive scanner
    private fun addFilesFromDocumentDir(dir: DocumentFile, files: MutableList<FileModel>) {
        dir.listFiles()?.forEach { file ->
            if (file.isDirectory) {
                addFilesFromDocumentDir(file, files)
            } else if (
                file.isFile &&
                !file.name.orEmpty().startsWith(".") &&
                file.name != ".nomedia"
            ) {
                files.add(
                    FileModel(
                        file = null,
                        name = file.name ?: "Unnamed",
                        size = file.length(),
                        path = file.uri.toString()
                    )
                )
            }
        }
    }
}
