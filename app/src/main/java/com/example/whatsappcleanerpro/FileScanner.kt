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

object FileScanner {

    // Checks if legacy storage permission is granted
    fun hasStoragePermission(context: Context): Boolean {
        return ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.READ_EXTERNAL_STORAGE
        ) == PackageManager.PERMISSION_GRANTED
    }

    // For Android 10 and below: Scan WhatsApp media using legacy storage
    fun getWhatsAppMediaFilesLegacy(context: Context): List<FileModel> {
        val files = mutableListOf<FileModel>()
        if (!hasStoragePermission(context)) return files

        val root = Environment.getExternalStorageDirectory()
        val mediaRoot = File(root, "WhatsApp/Media")

        if (mediaRoot.exists() && mediaRoot.isDirectory) {
            mediaRoot.listFiles()?.forEach { subDir ->
                if (subDir.isDirectory) {
                    subDir.walkTopDown().forEach { file ->
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
        }
        return files
    }

    // For Android 11+ (API 30+): Scan WhatsApp media using SAF
    fun getWhatsAppMediaFilesFromSAF(context: Context, pickedFolderUri: Uri): List<FileModel> {
        val files = mutableList.documents/tree/primary%3AWhatsApp%2FMedia"))
startActivityForResult(intent, REQUEST_CODE_PICK_WHATSAPP_MEDIA)
