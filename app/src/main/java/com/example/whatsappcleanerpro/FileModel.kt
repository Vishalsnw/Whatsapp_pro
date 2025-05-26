package com.example.whatsappcleanerpro

data class FileModel(
    val file: java.io.File,
    val name: String,
    val size: Long,
    val path: String
)
