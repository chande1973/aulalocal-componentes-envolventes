package com.example.domain

import android.content.Context
import android.os.Environment
import com.example.data.ClassroomDao
import com.example.data.AppLogger
import org.json.JSONArray
import org.json.JSONObject
import java.io.File

object BackupManager {

    suspend fun exportBackup(context: Context, dao: ClassroomDao): File? {
        return try {
            val profile = dao.getUserProfile()
            val progress = dao.getAllProgress()
            
            val backupObj = JSONObject().apply {
                put("version", 1)
                put("timestamp", System.currentTimeMillis())
                profile?.let {
                    put("profile", JSONObject().apply {
                        put("role", it.role)
                        put("firstName", it.firstName)
                        put("lastName", it.lastName)
                        put("email", it.email)
                        put("phone", it.phone)
                        put("gender", it.gender)
                        put("cardId", it.cardId)
                        put("avatar", it.avatar)
                        put("activeSection", it.activeSection)
                        put("materialsFolderUri", it.materialsFolderUri)
                        put("attendanceCount", it.attendanceCount)
                    })
                }
                val progressArray = JSONArray()
                progress.forEach { p ->
                    progressArray.put(JSONObject().apply {
                        put("unitIndex", p.unitIndex)
                        put("isRead", p.isRead)
                        put("readPercentage", p.readPercentage)
                        put("lastScrollPosition", p.lastScrollPosition)
                        put("isChallengeDone", p.isChallengeDone)
                        put("examGrade", p.examGrade?.toDouble() ?: JSONObject.NULL)
                        put("examCompleted", p.examCompleted)
                        put("isRecuperativoUsed", p.isRecuperativoUsed)
                        put("recuperativoRequested", p.recuperativoRequested)
                    })
                }
                put("progress", progressArray)
            }

            val fileName = "AulaLocal_Respaldo_${profile?.cardId ?: "Student"}_${System.currentTimeMillis()}.json"
            val downloadsDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
            val reportsDir = File(downloadsDir, "AulaLocal_Backups")
            val file = try {
                if (!reportsDir.exists()) reportsDir.mkdirs()
                File(reportsDir, fileName)
            } catch (e: Exception) {
                File(context.getExternalFilesDir(null), fileName)
            }

            file.writeText(backupObj.toString(2))
            AppLogger.d("BackupManager", "Respaldo exportado en: ${file.absolutePath}")
            file
        } catch (e: Exception) {
            AppLogger.e("BackupManager", "Error exporting backup", e)
            null
        }
    }
}
