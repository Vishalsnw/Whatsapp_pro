package com.example.whatsappcleanerpro.utils

import java.io.File
import java.text.DecimalFormat

object FileUtils {

    fun deleteFiles(files: List<File>): Int {
        var deletedCount = 0
        for (file in files) {
            if (file.exists() && file.delete()) {
                deletedCount++
            }
        }
        return deletedCount
    }

    fun formatSize(size: Long): String {
        if (size <= 0) return "0 B"
        val units = arrayOf("B", "KB", "MB", "GB", "TB")
        val digitGroups = (Math.log10(size.toDouble()) / Math.log10(1024.0)).toInt()
        return DecimalFormat("#,##0.#").format(size / Math.pow(1024.0, digitGroups.toDouble())) + " " + units[digitGroups]
    }

    fun getTotalSize(files: List<File>): Long {
        return files.sumOf { it.length() }
    }
}
