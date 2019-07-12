package com.urbanize.urbanizeplayer

import android.os.Environment
import android.os.StatFs

class ResourceManager {
    fun getMemoryUsage(): Resource {
        val runtime = Runtime.getRuntime()
        return Resource(
            totalSize = runtime.maxMemory() / (1024L * 1024L), // MB
            availableSize = runtime.freeMemory() / (1024L * 1024L), // MB
            usedSize = (runtime.totalMemory() - runtime.freeMemory()) / (1024L * 1024L) // MB
        )
    }

    fun getDiskUsage(): Resource {
        val stats = StatFs(Environment.getDataDirectory().absolutePath)
        return Resource(
            totalSize = stats.totalBytes / (1024L * 1024L), // MB
            availableSize = stats.availableBytes / (1024L * 1024L), // MB
            usedSize = (stats.totalBytes - stats.availableBytes) / (1024L * 1024L) // MB
        )
    }
}

data class Resource(
    val totalSize: Long = 0,
    val availableSize: Long = 0,
    val usedSize: Long = 0
)