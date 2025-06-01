package com.example.whatsappcleanerpro

import android.content.Context
import android.net.Uri
import android.os.Build
import android.os.Environment
import androidx.core.content.ContextCompat
import android.Manifest
import android.content.pm.PackageManager
import androidx.documentfile.provider.DocumentFile
import java.io.File

data class FileModel(
    val file: File?,       // For legacy mode, else null
    val name: String,
    val size: Long,
    val path: String       // Absolute path or content Uri
)

object FileScanner {

    // Checks if legacy storage permission is granted
    fun hasStoragePermission(context: Context): Boolean {
        return ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.READ_EXTERNAL_STORAGE
        ) == PackageManager if (!hasStoragePermission(context)) return files

        val root = Environment.getExternalStorageDirectory()
        val mediaRoot = File(root, "WhatsApp/Media")

        if (mediaRoot.exists() && mediaRoot.isDirectory) {
            mediaRoot.listFiles()?.forEach { subDir ->
                if (subDir.isfile.isFile) {
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
        }
        return files
    }

    // For Android 11+ (API<FileModel> {
        val files = mutableListOf<FileModel>()
        val pickedDir = DocumentFile.fromTreeUri(context, pickedFolderUri)
        if (pickedDir != null && pickedDir.isDirectory) {
            addFilesFromDocumentDir(pickedDir, files)
        }
        return files
    }

    private                addFilesFromDocumentDir(file, files)
            } else if (file.isFile) {
                files.add(
                    FileModel(
                        file = null,
                        name = file.name ?: "",
                        size = file.length(),
                        path = file.uri.toString()
                    )
                )
            }
        }
    }

    //(context)) return files

        val root = Environment.getExternalStorageDirectory()
        val categoryFolders = mapOf(
            "images" to "WhatsApp/Media/WhatsApp Images",
            "video" to "WhatsApp/Media/WhatsApp Video",
            "audio" to "WhatsApp/Media/WhatsApp folderName = categoryFolders[type.lowercase()]
        folderName?.let {
            val folder = File(root, it)
            if (folder.exists() && folder.isDirectory) {
                folder.walkTopDown().forEach { file ->
                    if (file.isFile) {
                        files.add(
                            FileModel(
                                file = )
                    }
                }
            }
        }
        return files
    }

    // Category-based scan for SAF
    fun getFilesForCategorySAF(context: Context, pickedFolderUri: Uri, type: String): List<FileModel> {
        val files = mutableListOf<FileModel>()
        val pickedDir = DocumentFile.fromTreeUri(context, pickedFolderUri)
        if (pickedDir == null || !pickedDir.isDirectory) return files

        val categoryFolders = mapOf(
            "images" to "WhatsApp Images",
            "video" to "WhatsApp Video",
            "audio" to "WhatsApp Audio",
            "documents" to "WhatsApp Documents",
            "gifs" to "WhatsApp Animated Gifs",
            "voice" to "WhatsApp Voice Notes",
            "stickers" to "WhatsApp Stickers",
            "wallpaper" to "WallPaper"
        )

        val subFolder = categoryFolders[type.lowercase()]
        if (subFolder != null) {
            val folder = pickedDir.findFile(subFolder)
            if (folder != null && folder.isDirectory) {
                addFilesFromDocumentDir(folder, files)
            }
        }
        return files
    }
                            }
