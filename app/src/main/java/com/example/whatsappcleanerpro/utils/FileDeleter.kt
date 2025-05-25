package com.example.whatsappcleanerpro.utils

import java.io.File

object FileDeleter {

    fun deleteFiles(files: List<File>): Int {
        var deletedCount = 0
        files.forEach { file ->
            try {
                if (file.exists() && file.delete()) {
                    deletedCount++
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
        return deletedCount
    }

    fun deleteCategory(category: String): Int {
        val files = FileScanner.getFilesForCategory(category)
        return deleteFiles(files)
    }
}
