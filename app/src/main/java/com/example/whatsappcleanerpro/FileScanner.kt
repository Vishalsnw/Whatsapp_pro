package com.example.whatsappcleanerpro

import android.content.Context
import android.net.Uri
import android.os.Build
import android.os.Environment
import androidx.core.content.ContextCompat
import android.Manifest
import android.content.pm.PackageManager
import androidx.documentfile.provider.DocumentFile

data class FileModel(
    val file: java.io.File?,       // For legacy mode, else null
    val name: String,
    val size: Long,
    val path: String              // Absolute path or content Uri
)

object FileScanner {

    // Checks if legacy storage permission is granted
    fun hasReadPermission(context: Context): Boolean {
        return ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.READ_EXTERNAL_STORAGE
        ) == PackageManager.PERMISSION_GRANTED
    }

    // For Android 10 and below: Scan WhatsApp/Media/[type] using legacy permissions
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
            val folder = java.io.File(root, it)  // Corrected the file path access
            if (folder.exists() && folder.isDirectory) {
                folder.listFiles()?.forEach { file ->
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
        return files
    }

    // For Android 11+ (SAF): Scan WhatsApp/Media/[type] using DocumentFile
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

    // Recursively add files from a DocumentFile directory
    private fun addFilesFromDocumentDir(dir: DocumentFile, files: MutableList<FileModel>) {
        dir.listFiles()?.forEach { file ->
            if (file.isDirectory) {
                addFilesFromDocumentDir(file, files)
            } else if (file.isFile) {
                files.add(
                    FileModel(
                        file = null,  // can't get File for SAF
                        name = file.name ?: "",
                        size = file.length(),
                        path = file.uri.toString()
                    )
                )
            }
        }
    }
}
