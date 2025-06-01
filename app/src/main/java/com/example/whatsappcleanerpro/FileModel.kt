package com.example.whatsappcleanerpro

import java.io.File

data class FileModel(
    val file: File?,       // Nullable to support SAF mode (no java.io.File available)
    val name: String,
    val size: Long,
    val path: String       // Absolute path (legacy) or content Uri (SAF)
)
