package com.veritas.reader

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.concurrent.TimeUnit

/**
 * Weekly safety net: writes a data-only backup (same JSON as manual export, no
 * originals) into app storage and keeps the last [KEEP_COUNT]. Costs a few KB–MB,
 * so users who never export manually still have something recent to restore or
 * copy out of Android/data. Controlled by ReaderSettings.autoBackupWeekly.
 */
class AutoBackupWorker(
    appContext: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(appContext, workerParams) {

    override suspend fun doWork(): Result {
        val repository = DocumentRepository(applicationContext)
        if (!repository.loadReaderSettings().autoBackupWeekly) return Result.success()
        return runCatching {
            val dir = File(applicationContext.filesDir, "auto_backups").apply { mkdirs() }
            val name = "veritas_auto_backup_${SimpleDateFormat("yyyyMMdd", Locale.US).format(Date())}.json"
            File(dir, name).writeText(repository.buildBackupJson(), Charsets.UTF_8)
            dir.listFiles()
                ?.filter { it.name.startsWith("veritas_auto_backup_") }
                ?.sortedByDescending { it.name }
                ?.drop(KEEP_COUNT)
                ?.forEach { runCatching { it.delete() } }
        }.fold({ Result.success() }, { Result.retry() })
    }

    companion object {
        private const val KEEP_COUNT = 4
        private const val WORK_NAME = "veritas_auto_backup"

        fun schedule(context: Context) {
            val request = PeriodicWorkRequestBuilder<AutoBackupWorker>(7, TimeUnit.DAYS).build()
            WorkManager.getInstance(context)
                .enqueueUniquePeriodicWork(WORK_NAME, ExistingPeriodicWorkPolicy.KEEP, request)
        }
    }
}
