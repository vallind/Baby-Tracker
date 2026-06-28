package com.babytracker.core.backup

import android.content.Context
import android.net.Uri
import androidx.documentfile.provider.DocumentFile
import com.babytracker.core.database.AppDatabase
import com.babytracker.core.database.entity.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext
import okhttp3.Credentials
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.InputStream
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.concurrent.TimeUnit
import java.util.zip.ZipEntry
import java.util.zip.ZipInputStream
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

    suspend fun createWebDAVBackup(): Result<String> = withContext(Dispatchers.IO) {
        try {
            val config = db.backupConfigDao().get() ?: return@withContext Result.failure(Exception("未配置"))
            val url = requireNotNull(config.webdavUrl) { "服务器地址未设置" }
            val user = requireNotNull(config.webdavUser) { "用户名未设置" }
            val pass = requireNotNull(config.webdavPass) { "密码未设置" }
            val ts = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"))
            val fileName = "babytracker_backup_$ts.zip"
            val json = exportAll()
            val zipBytes = ByteArrayOutputStream().use { bos ->
                ZipOutputStream(bos).use { zos -> zos.putNextEntry(ZipEntry("data.json")); zos.write(json.toString(2).toByteArray()); zos.closeEntry() }
                bos.toByteArray()
            }
            val targetUrl = "${url.trimEnd('/')}/$fileName"
            val client = OkHttpClient.Builder().connectTimeout(15, TimeUnit.SECONDS).writeTimeout(30, TimeUnit.SECONDS).build()
            val request = Request.Builder().url(targetUrl).put(zipBytes.toRequestBody("application/zip".toMediaType())).header("Authorization", Credentials.basic(user, pass)).build()
            val response = client.newCall(request).execute()
            if (response.isSuccessful) {
                val backupAt = LocalDateTime.now().format(DateTimeFormatter.ISO_DATE_TIME)
                db.backupConfigDao().insert(config.copy(lastBackupAt = backupAt))
                Result.success(targetUrl)
            } else Result.failure(Exception("HTTP ${response.code}"))
        } catch (e: Exception) { Result.failure(e) }
    }

    suspend fun restoreFromWebDAV(): Result<Int> = withContext(Dispatchers.IO) {
        try {
            val config = db.backupConfigDao().get() ?: return@withContext Result.failure(Exception("未配置"))
            val url = requireNotNull(config.webdavUrl) { "服务器地址未设置" }
            val user = requireNotNull(config.webdavUser) { "用户名未设置" }
            val pass = requireNotNull(config.webdavPass) { "密码未设置" }
            val baseUrl = url.trimEnd('/')

            val client = OkHttpClient.Builder().connectTimeout(15, TimeUnit.SECONDS).readTimeout(60, TimeUnit.SECONDS).build()

            // Try to find backup files via PROPFIND
            val propfindXml = "<?xml version=\"1.0\" encoding=\"utf-8\"?><propfind xmlns=\"DAV:\"><prop><displayname/><getcontentlength/><getlastmodified/></prop></propfind>"
            val listRequest = Request.Builder().url(baseUrl).method("PROPFIND", propfindXml.toRequestBody("application/xml".toMediaType())).header("Authorization", Credentials.basic(user, pass)).header("Depth", "1").build()
            val listResponse = client.newCall(listRequest).execute()
            val body = listResponse.body.string()
            val backupFiles = Regex("babytracker_backup_\\d{8}_\\d{6}\\.zip").findAll(body).map { it.value }.sorted().toList()

            if (backupFiles.isEmpty()) return@withContext Result.failure(Exception("未找到备份文件"))
            val latestFile = backupFiles.last()
            val fileUrl = "$baseUrl/$latestFile"

            val getRequest = Request.Builder().url(fileUrl).get().header("Authorization", Credentials.basic(user, pass)).build()
            val getResponse = client.newCall(getRequest).execute()
            val zipBytes = getResponse.body.bytes()

            val jsonStr = java.io.ByteArrayInputStream(zipBytes).use { input ->
                val zip = ZipInputStream(input)
                var entry = zip.nextEntry
                while (entry != null) {
                    if (entry.name == "data.json") {
                        return@use zip.bufferedReader().readText()
                    }
                    zip.closeEntry()
                    entry = zip.nextEntry
                }
                null
            } ?: return@withContext Result.failure(Exception("备份文件缺少 data.json"))

            val data = JSONObject(jsonStr)
            doRestore(data)
        } catch (e: Exception) { Result.failure(e) }
    }

    suspend fun restoreFromUri(context: Context, uri: Uri): Result<Int> = withContext(Dispatchers.IO) {
        try {
            context.contentResolver.openInputStream(uri)?.use { input ->
                val zip = ZipInputStream(input)
                var entry = zip.nextEntry
                while (entry != null) {
                    if (entry.name == "data.json") {
                        val jsonStr = zip.bufferedReader().readText()
                        zip.closeEntry()
                        val data = JSONObject(jsonStr)
                        return@withContext doRestore(data)
                    }
                    zip.closeEntry()
                    entry = zip.nextEntry
                }
                return@withContext Result.failure(Exception("备份文件缺少 data.json"))
            } ?: return@withContext Result.failure(Exception("无法读取文件"))
        } catch (e: Exception) { Result.failure(e) }
    }

    private suspend fun doRestore(data: JSONObject): Result<Int> {
        val babyArr = data.getJSONArray("babies")
        if (babyArr.length() == 0) return Result.failure(Exception("备份文件无宝宝数据"))

        // 先解析原始 ID（用于匹配子记录 prefix），entity 设 id=0 让 Room 自动生成
        data class BabyRestore(val origId: Int, val entity: BabyEntity)
        val babyList = (0 until babyArr.length()).map { i ->
            val j = babyArr.getJSONObject(i)
            BabyRestore(
                origId = j.getInt("id"),
                entity = BabyEntity(
                    id = 0, name = j.getString("name"), gender = j.getString("gender"),
                    birthDate = j.getString("birthDate"),
                    birthWeight = j.optDouble("birthWeight", 0.0).takeIf { it > 0 },
                    birthHeight = j.optDouble("birthHeight", 0.0).takeIf { it > 0 },
                    createdAt = j.optString("createdAt"),
                ),
            )
        }

        var count = 0
        for (baby in babyList) {
            val newId = db.babyDao().insert(baby.entity).toInt()
            count++
            val prefix = "baby_${baby.origId}"
            parseFeedings(data.optJSONArray("${prefix}_feedings")).forEach { db.feedingDao().insert(it.copy(babyId = newId)) }
            parseSleeps(data.optJSONArray("${prefix}_sleeps")).forEach { db.sleepDao().insert(it.copy(babyId = newId)) }
            parseGrowths(data.optJSONArray("${prefix}_growths")).forEach { db.growthDao().insert(it.copy(babyId = newId)) }
            parseVaccinations(data.optJSONArray("${prefix}_vaccinations")).forEach { db.vaccinationDao().insert(it.copy(babyId = newId)) }
            parseHealths(data.optJSONArray("${prefix}_health_records")).forEach { db.healthRecordDao().insert(it.copy(babyId = newId)) }
            parseDiapers(data.optJSONArray("${prefix}_diapers")).forEach { db.diaperDao().insert(it.copy(babyId = newId)) }
            parseDevelopmentAssessments(data.optJSONArray("${prefix}_assessments")).forEach { db.developmentAssessmentDao().insert(it.copy(babyId = newId)) }
            parseReminders(data.optJSONArray("${prefix}_reminders")).forEach { db.reminderDao().insert(it.copy(babyId = newId)) }
        }

        // 消息是 App 级别的，不绑定某个宝宝
        parseMessages(data.optJSONArray("messages")).forEach { db.messageDao().insert(it) }

        return Result.success(count)
    }

    private fun parseFeedings(arr: JSONArray?) = arr?.let { (0 until it.length()).map { i ->
        val j = it.getJSONObject(i)
        FeedingEntity(babyId = j.getInt("babyId"), type = j.getString("type"), amountMl = j.optInt("amountMl", -1).takeIf { v -> v >= 0 }, durationMin = j.optInt("durationMin", -1).takeIf { v -> v >= 0 }, breastSide = j.optString("breastSide").ifBlank { null }, foodName = j.optString("foodName").ifBlank { null }, amountG = j.optInt("amountG", -1).takeIf { v -> v >= 0 }, brand = j.optString("brand").ifBlank { null }, note = j.optString("note").ifBlank { null }, timestamp = j.getString("timestamp"))
    } } ?: emptyList()

    private fun parseSleeps(arr: JSONArray?) = arr?.let { (0 until it.length()).map { i ->
        val j = it.getJSONObject(i)
        SleepEntity(babyId = j.getInt("babyId"), type = j.getString("type"), startTime = j.getString("startTime"), endTime = j.getString("endTime"), note = j.optString("note").ifBlank { null })
    } } ?: emptyList()

    private fun parseGrowths(arr: JSONArray?) = arr?.let { (0 until it.length()).map { i ->
        val j = it.getJSONObject(i)
        GrowthEntity(babyId = j.getInt("babyId"), type = j.getString("type"), value = j.getDouble("value"), measuredAt = j.getString("measuredAt"), note = j.optString("note").ifBlank { null })
    } } ?: emptyList()

    private fun parseVaccinations(arr: JSONArray?) = arr?.let { (0 until it.length()).map { i ->
        val j = it.getJSONObject(i)
        VaccinationEntity(babyId = j.getInt("babyId"), name = j.getString("name"), dose = j.optString("dose").ifBlank { null }, scheduledDate = j.optString("scheduledDate").ifBlank { null }, administeredDate = j.optString("administeredDate").ifBlank { null }, status = j.optString("status", "pending"), note = j.optString("note").ifBlank { null })
    } } ?: emptyList()

    private fun parseHealths(arr: JSONArray?) = arr?.let { (0 until it.length()).map { i ->
        val j = it.getJSONObject(i)
        HealthRecordEntity(babyId = j.getInt("babyId"), category = j.getString("category"), description = j.getString("description"), doctorName = j.optString("doctorName").ifBlank { null }, recordDate = j.getString("recordDate"), note = j.optString("note").ifBlank { null })
    } } ?: emptyList()

    private fun parseDiapers(arr: JSONArray?) = arr?.let { (0 until it.length()).map { i ->
        val j = it.getJSONObject(i)
        DiaperEntity(babyId = j.getInt("babyId"), type = j.getString("type"), timestamp = j.getString("timestamp"), note = j.optString("note").ifBlank { null })
    } } ?: emptyList()

    private fun parseMessages(arr: JSONArray?) = arr?.let { (0 until it.length()).map { i ->
        val j = it.getJSONObject(i)
        MessageEntity(
            id = 0, type = j.getString("type"), title = j.getString("title"),
            content = j.getString("content"), senderAvatar = j.optString("senderAvatar").ifBlank { null },
            createTime = j.getLong("createTime"), isRead = j.optBoolean("isRead"),
            extraData = j.optString("extraData", ""),
            uuid = j.optString("uuid").ifBlank { null },
            updatedAt = j.optLong("updatedAt", 0), deletedAt = j.optLong("deletedAt", -1).takeIf { it >= 0 },
        )
    } } ?: emptyList()

    private fun parseDevelopmentAssessments(arr: JSONArray?) = arr?.let { (0 until it.length()).map { i ->
        val j = it.getJSONObject(i)
        DevelopmentAssessmentEntity(
            id = 0, babyId = j.getInt("babyId"),
            assessDate = j.getLong("assessDate"), babyAgeMonths = j.getInt("babyAgeMonths"),
            grossMotor = j.getInt("grossMotor"), fineMotor = j.getInt("fineMotor"),
            language = j.getInt("language"), social = j.getInt("social"),
            cognitive = j.getInt("cognitive"), note = j.optString("note", ""),
            uuid = j.optString("uuid").ifBlank { null },
            updatedAt = j.optLong("updatedAt", 0), deletedAt = j.optLong("deletedAt", -1).takeIf { it >= 0 },
        )
    } } ?: emptyList()

    private fun parseReminders(arr: JSONArray?) = arr?.let { (0 until it.length()).map { i ->
        val j = it.getJSONObject(i)
        ReminderEntity(
            id = 0, babyId = j.getInt("babyId"), type = j.getString("type"),
            title = j.getString("title"), description = j.optString("description", ""),
            dueDate = j.getLong("dueDate"), isDone = j.optBoolean("isDone"),
            doneDate = j.optLong("doneDate", -1).takeIf { it >= 0 },
            isEnabled = j.optBoolean("isEnabled", true),
            repeatRule = j.optString("repeatRule", ""),
            uuid = j.optString("uuid").ifBlank { null },
            updatedAt = j.optLong("updatedAt", 0), deletedAt = j.optLong("deletedAt", -1).takeIf { it >= 0 },
        )
    } } ?: emptyList()

    suspend fun loadConfig() = db.backupConfigDao().get()
    suspend fun saveConfig(url: String, user: String, pass: String) {
        val c = db.backupConfigDao().get()?.copy(webdavUrl = url, webdavUser = user, webdavPass = pass)
            ?: BackupConfigEntity(webdavUrl = url, webdavUser = user, webdavPass = pass, autoBackup = false)
        db.backupConfigDao().insert(c)
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
            data.put("${prefix}_assessments", JSONArray().apply {
                db.developmentAssessmentDao().watchByBaby(baby.id).first().forEach { put(it.toJson()) }
            })
            data.put("${prefix}_reminders", JSONArray().apply {
                (db.reminderDao().watchPending(baby.id).first() + db.reminderDao().watchHistory(baby.id).first()).forEach { put(it.toJson()) }
            })
        }

        // 消息是 App 级别的，不属于某个宝宝
        data.put("messages", JSONArray().apply {
            db.messageDao().watchAll().first().forEach { put(it.toJson()) }
        })

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

private fun MessageEntity.toJson() = JSONObject().apply {
    put("id", id); put("type", type); put("title", title); put("content", content)
    put("senderAvatar", senderAvatar); put("createTime", createTime)
    put("isRead", isRead); put("extraData", extraData)
    put("uuid", uuid); put("updatedAt", updatedAt); put("deletedAt", deletedAt)
}

private fun DevelopmentAssessmentEntity.toJson() = JSONObject().apply {
    put("id", id); put("babyId", babyId); put("assessDate", assessDate)
    put("babyAgeMonths", babyAgeMonths); put("grossMotor", grossMotor)
    put("fineMotor", fineMotor); put("language", language)
    put("social", social); put("cognitive", cognitive); put("note", note)
    put("uuid", uuid); put("updatedAt", updatedAt); put("deletedAt", deletedAt)
}

private fun ReminderEntity.toJson() = JSONObject().apply {
    put("id", id); put("babyId", babyId); put("type", type)
    put("title", title); put("description", description)
    put("dueDate", dueDate); put("isDone", isDone); put("doneDate", doneDate)
    put("isEnabled", isEnabled); put("repeatRule", repeatRule)
    put("uuid", uuid); put("updatedAt", updatedAt); put("deletedAt", deletedAt)
}