package com.example.whatsappcleanerpro

import android.content.Context
import android.net.Uri
import android.os.Build
import android.os.Environment
import androidx.core.content.ContextCompat
import android.Manifest
import android.content.pm.PackageManager
import androidx.documentfile.provider.DocumentFile

// Data class for file representation
data class FileModel(
    val file: java.io.File?,       // Only for legacy access
    val name: String,
    val size: Long,
    val path: String               // Absolute path (legacy) or Uri string (SAF)
)

object FileScanner {

    // Check if legacy read permission is granted
    fun hasReadPermission(context: Context): Boolean {
        return ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.READ_EXTERNAL_STORAGE
        ) == PackageManager.PERMISSION_GRANTED
    }

    // For Android 10 and below: read directly from filesystem
    fun getFilesForCategory(context: Context, type: String): List<FileModel> {
        val files = mutableListOf<FileModel>()
        val root = Environment.getExternalStorageDirectory()

        val categoryFolders = mapOf(
            "images" to "WhatsApp/Media/WhatsApp Images",
            "video" to "WhatsApp/Media/WhatsApp Video",
            "audio" to "WhatsApp/Media/WhatsApp Voice Notes",
            "stickers" to "WhatsApp/Media/WhatsApp Stickers",
            "wallpaper" to "WhatsApp/Media/Wallpaper"
        )

        val folderName = categoryFolders[type.lowercase()]
        folderName?.let {
            val folder = java.io.File(root, it)
            if (folder.exists() && folder.isDirectory) {
                folder.listFiles()?.forEach { file ->
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

    // For Android 11 and above: read using SAF
    fun getFilesForCategorySAF(context: Context, pickedFolderUri: Uri, type: String): List<FileModel> {
        val files = mutableListOf<FileModel>()

        val categoryFolders = mapOf(
            "images" to "WhatsApp Images",
            "video" to "WhatsApp Video",
            "audio" to "WhatsApp Audio",
            "documents" to "WhatsApp Documents",
            "gifs" to "WhatsApp Animated Gifs",
            "voice" to "WhatsApp Voice Notes",
            "stickers" to "WhatsApp Stickers"
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

    // Recursive SAF scanner
    private fun addFilesFromDocumentDir(dir: DocumentFile, files: MutableList<FileModel>) {
        dir.listFiles()?.forEach { file ->
            if (file.isDirectory) {
                addFilesFromDocumentDir(file, files)
            } else if (file.isFile) {
                files.add(
                    FileModel(
                        file = null,  // SAF: no access to java.io.File
                        name = file.name ?: "Unnamed",
                        size = file.length(),
                        path = file.uri.toString()
                    )
                )
            }
        }
    }
}
