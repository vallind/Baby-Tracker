package com.babytracker.core.util

import android.content.Context
import android.util.Log
import timber.log.Timber
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.concurrent.ConcurrentLinkedQueue

data class LogEntry(
    val timestamp: Long,
    val level: Char,
    val tag: String,
    val message: String,
)

object LogBuffer {
    private const val MAX_ENTRIES = 1000
    private val buffer = ConcurrentLinkedQueue<LogEntry>()

    fun push(entry: LogEntry) {
        buffer.add(entry)
        while (buffer.size > MAX_ENTRIES) {
            buffer.poll()
        }
    }

    fun getEntries(): List<LogEntry> = buffer.toList()

    fun clear() { buffer.clear() }
}

class AppLogTree(private val context: Context) : Timber.Tree() {
    /** 开关：关闭时只输出 logcat，不写入内存缓冲区和文件 */
    var enabled: Boolean = false

    private val logDir: File = File(context.filesDir, "logs")
    private val logFile: File = File(logDir, "app.log")
    private val fileDateFormat = SimpleDateFormat("MM-dd HH:mm:ss.SSS", Locale.getDefault())
    private val maxFileSize = 512 * 1024

    init { logDir.mkdirs() }

    override fun log(priority: Int, tag: String?, message: String, t: Throwable?) {
        val tagSafe = tag ?: "Unknown"
        val level = when (priority) {
            Log.VERBOSE -> 'V'
            Log.DEBUG -> 'D'
            Log.INFO -> 'I'
            Log.WARN -> 'W'
            Log.ERROR -> 'E'
            else -> '?'
        }
        Log.println(priority, tagSafe, message)

        if (!enabled) return

        val entry = LogEntry(
            timestamp = System.currentTimeMillis(),
            level = level,
            tag = tagSafe,
            message = message,
        )
        LogBuffer.push(entry)
        writeToFile(entry)
    }

    private fun writeToFile(entry: LogEntry) {
        try {
            if (logFile.exists() && logFile.length() > maxFileSize) {
                rotateLogs()
            }
            val line = "[${fileDateFormat.format(Date(entry.timestamp))}] ${entry.level}/${entry.tag}: ${entry.message}\n"
            logFile.appendText(line)
        } catch (_: Exception) { }
    }

    private fun rotateLogs() {
        try {
            for (i in 4 downTo 1) {
                val old = File(logDir, "app.$i.log")
                val newer = File(logDir, "app.${i + 1}.log")
                if (old.exists()) old.renameTo(newer)
            }
            if (logFile.exists()) logFile.renameTo(File(logDir, "app.1.log"))
        } catch (_: Exception) { }
    }

    fun clearLogs() {
        LogBuffer.clear()
        logDir.listFiles()?.forEach { it.delete() }
        logDir.mkdirs()
    }

    fun logFileSize(): Long = logFile.length()

    fun logFileCount(): Int = logDir.listFiles()?.size ?: 0
}
