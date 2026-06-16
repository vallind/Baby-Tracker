package com.babytracker.core.backup

import android.content.Context
import android.net.Uri
import androidx.documentfile.provider.DocumentFile
import com.babytracker.core.database.AppDatabase
import com.babytracker.core.database.entity.*
import kotlinx.coroutines.flow.first
import org.json.JSONArray
import org.json.JSONObject
import java.io.File
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.zip.ZipEntry
import java.util.zip.ZipOutputStream

class BackupManager(private val db: AppDatabase) {
    suspend fun createLocalBackup(context: Context): String {
        val backupDir = File(context.filesDir, "backups").also { it.mkdirs() }
        val ts = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"))
        val file = File(backupDir, "babytracker_backup_$ts.zip")
        val json = exportAll()
        ZipOutputStream(file.outputStream()).use { zos ->
            zos.putNextEntry(ZipEntry("data.json"))
            zos.write(json.toString(2).toByteArray())
            zos.closeEntry()
        }
        return file.absolutePath
    }

    suspend fun createLocalBackupToUri(context: Context, targetUri: Uri): String? {
        val ts = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"))
        val fileName = "babytracker_backup_$ts.zip"
        val documentFile = DocumentFile.fromTreeUri(context, targetUri)
        val file = documentFile?.createFile("application/zip", fileName) ?: return null
        val json = exportAll()
        context.contentResolver.openOutputStream(file.uri)?.use { os ->
            ZipOutputStream(os).use { zos ->
                zos.putNextEntry(ZipEntry("data.json"))
                zos.write(json.toString(2).toByteArray())
                zos.closeEntry()
            }
        }
        return file.uri.toString()
    }

    private suspend fun exportAll(): JSONObject {
        val data = JSONObject()
        data.put("version", 1)
        data.put("exported_at", LocalDateTime.now().format(DateTimeFormatter.ISO_DATE_TIME))

        val babies = db.babyDao().watchAll().first()
        data.put("babies", JSONArray().apply {
            babies.forEach { put(it.toJson()) }
        })

        babies.forEach { baby ->
            val prefix = "baby_${baby.id}"
            data.put("${prefix}_feedings", JSONArray().apply {
                db.feedingDao().watchByBaby(baby.id).first().forEach { put(it.toJson()) }
            })
            data.put("${prefix}_sleeps", JSONArray().apply {
                db.sleepDao().watchByBaby(baby.id).first().forEach { put(it.toJson()) }
            })
            data.put("${prefix}_growths", JSONArray().apply {
                db.growthDao().watchByBaby(baby.id).first().forEach { put(it.toJson()) }
            })
            data.put("${prefix}_vaccinations", JSONArray().apply {
                db.vaccinationDao().watchByBaby(baby.id).first().forEach { put(it.toJson()) }
            })
            data.put("${prefix}_health_records", JSONArray().apply {
                db.healthRecordDao().watchByBaby(baby.id).first().forEach { put(it.toJson()) }
            })
            data.put("${prefix}_diapers", JSONArray().apply {
                db.diaperDao().watchByBaby(baby.id).first().forEach { put(it.toJson()) }
            })
        }

        data.put("backup_config", JSONArray().apply {
            db.backupConfigDao().get()?.let { put(it.toJson()) }
        })

        return data
    }
}

private fun BabyEntity.toJson() = JSONObject().apply {
    put("id", id); put("name", name); put("gender", gender)
    put("birthDate", birthDate); put("birthWeight", birthWeight); put("birthHeight", birthHeight)
    put("avatarPath", avatarPath); put("createdAt", createdAt)
}

private fun FeedingEntity.toJson() = JSONObject().apply {
    put("id", id); put("babyId", babyId); put("type", type)
    put("amountMl", amountMl); put("durationMin", durationMin)
    put("breastSide", breastSide); put("foodName", foodName)
    put("amountG", amountG); put("brand", brand); put("note", note)
    put("timestamp", timestamp)
}

private fun SleepEntity.toJson() = JSONObject().apply {
    put("id", id); put("babyId", babyId); put("type", type)
    put("startTime", startTime); put("endTime", endTime); put("note", note)
}

private fun GrowthEntity.toJson() = JSONObject().apply {
    put("id", id); put("babyId", babyId); put("type", type)
    put("value", value); put("measuredAt", measuredAt); put("note", note)
}

private fun VaccinationEntity.toJson() = JSONObject().apply {
    put("id", id); put("babyId", babyId); put("name", name)
    put("dose", dose); put("scheduledDate", scheduledDate)
    put("administeredDate", administeredDate); put("status", status); put("note", note)
}

private fun HealthRecordEntity.toJson() = JSONObject().apply {
    put("id", id); put("babyId", babyId); put("category", category)
    put("description", description); put("doctorName", doctorName)
    put("recordDate", recordDate); put("attachments", attachments); put("note", note)
}

private fun DiaperEntity.toJson() = JSONObject().apply {
    put("id", id); put("babyId", babyId); put("type", type)
    put("timestamp", timestamp); put("note", note)
}

private fun BackupConfigEntity.toJson() = JSONObject().apply {
    put("id", id); put("webdavUrl", webdavUrl); put("webdavUser", webdavUser)
    put("webdavPass", webdavPass); put("autoBackup", autoBackup)
    put("lastBackupAt", lastBackupAt)
}