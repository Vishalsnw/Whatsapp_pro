package com.example.whatsappcleanerpro

import android.os.Environment
import android.os.StatFs
import java.io.File

object StorageUtils {

    /**
     * Returns total size of a folder and its contents
     */
    fun getUsedStorageForPath(path: String): Long {
        val file = File(path)
        return if (file.exists() && file.canRead()) {
            getFolderSize(file)
        } else {
            0L
        }
    }

    /**
     * Returns total external storage (usually internal shared storage)
     */
    fun getTotalStorage(): Long {
        val stat = StatFs(Environment.getExternalStorageDirectory().absolutePath)
        return stat.blockSizeLong * stat.blockCountLong
    }

    /**
     * Returns currently available (free) external storage
     */
    fun getAvailableStorage(): Long {
        val stat = StatFs(Environment.getExternalStorageDirectory().absolutePath)
        return stat.blockSizeLong * stat.availableBlocksLong
    }

    /**
     * Returns used external storage
     */
    fun getUsedStorage(): Long {
        return getTotalStorage() - getAvailableStorage()
    }

    /**
     * Recursively calculates total size of a directory
     */
    private fun getFolderSize(dir: File): Long {
        if (!dir.exists() || !dir.canRead()) return 0L
        var size = 0L
        dir.listFiles()?.forEach { file ->
            try {
                size += if (file.isDirectory) getFolderSize(file)
                else if (!file.name.startsWith(".") && file.name != ".nomedia") file.length()
                else 0L
            } catch (e: Exception) {
                // Skip inaccessible files
            }
        }
        return size
    }
}
