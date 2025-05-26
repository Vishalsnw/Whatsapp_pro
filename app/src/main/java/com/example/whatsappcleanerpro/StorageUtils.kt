package com.example.whatsappcleanerpro

import android.os.Environment
import android.os.StatFs
import java.io.File

object StorageUtils {

    fun getUsedStorageForPath(path: String): Long {
        val file = File(path)
        return if (file.exists()) getFolderSize(file) else 0L
    }

    fun getTotalStorage(): Long {
        val stat = StatFs(Environment.getExternalStorageDirectory().path)
        return stat.blockSizeLong * stat.blockCountLong
    }

    fun getAvailableStorage(): Long {
        val stat = StatFs(Environment.getExternalStorageDirectory().path)
        return stat.blockSizeLong * stat.availableBlocksLong
    }

    fun getUsedStorage(): Long {
        return getTotalStorage() - getAvailableStorage()
    }

    private fun getFolderSize(dir: File): Long {
        var size = 0L
        if (dir.exists()) {
            val files = dir.listFiles() ?: return 0L
            for (file in files) {
                size += if (file.isDirectory) getFolderSize(file) else file.length()
            }
        }
        return size
    }
}
