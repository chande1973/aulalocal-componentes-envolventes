package com.example.domain

import android.content.Context
import com.example.data.SyncedStudent
import com.example.data.LocalSection
import com.example.data.AppLogger
import org.json.JSONObject
import java.io.File

object ReportManager {

    /**
     * Genera el acta final en formato CSV.
     */
    fun generateAcademicReport(
        context: Context,
        teacherName: String,
        section: LocalSection,
        students: List<SyncedStudent>,
        totalUnits: Int
    ): File? {
        val csvBuilder = StringBuilder()
        csvBuilder.append("PROFESOR: $teacherName\n")
        csvBuilder.append("SECCIÓN: ${section.name}\n")
        csvBuilder.append("Cedula,Alumno,Asistencia %,Nota Final,Estado\n")

        return try {
            students.forEach { student ->
                val gradesObj = try { JSONObject(student.examGradesString) } catch (e: Exception) { JSONObject() }
                
                var sum = 0f
                var count = 0
                for (i in 1..totalUnits) {
                    val g = gradesObj.optString(i.toString(), "")
                    if (g.isNotEmpty()) {
                        sum += g.toFloatOrNull() ?: 0f
                        count++
                    }
                }
                
                val avgFinal = if (count > 0) sum / count else 0f
                val attendancePct = if (section.totalClasses > 0) (student.attendanceCount.toFloat() / section.totalClasses * 100).toInt() else 0
                
                val isPassing = avgFinal >= 11.5f && attendancePct >= 75
                val status = if (isPassing) "APROBADO" else "REPROBADO"
                
                val safeName = "${student.lastName} ${student.firstName}".replace(",", "")
                csvBuilder.append("${student.cardId},$safeName,$attendancePct%,${"%.2f".format(java.util.Locale.US, avgFinal)},$status\n")
            }
            
            val fileName = "Acta_Final_${section.name.replace(" ", "_")}_${System.currentTimeMillis()}.csv"
            
            // 1. Intentar en Descargas (Público)
            val downloadsDir = android.os.Environment.getExternalStoragePublicDirectory(android.os.Environment.DIRECTORY_DOWNLOADS)
            val reportsDir = File(downloadsDir, "AulaLocal_Reportes")
            
            var file = try {
                if (!reportsDir.exists()) reportsDir.mkdirs()
                File(reportsDir, fileName)
            } catch (e: Exception) {
                // 2. Fallback a carpeta interna de la app (Siempre accesible vía ADB)
                File(context.getExternalFilesDir(null), fileName)
            }
            
            file.writeText(csvBuilder.toString())
            AppLogger.d("ReportManager", "Reporte generado en: ${file.absolutePath}")
            file
        } catch (e: Exception) {
            AppLogger.e("ReportManager", "Error generating academic report", e)
            null
        }
    }

    /**
     * Genera un reporte detallado de asistencia.
     */
    fun generateAttendanceReport(
        context: Context,
        section: LocalSection,
        students: List<SyncedStudent>
    ): File? {
        val csvBuilder = StringBuilder()
        csvBuilder.append("REPORTE DE ASISTENCIA DETALLADO\n")
        csvBuilder.append("AULA: ${section.name}\n")
        csvBuilder.append("Clases dadas: ${section.totalClasses}\n\n")
        csvBuilder.append("Cedula,Alumno,Asistencias,Asistencia %\n")

        return try {
            students.forEach { student ->
                val pct = if (section.totalClasses > 0) (student.attendanceCount.toFloat() / section.totalClasses * 100).toInt() else 0
                val safeName = "${student.lastName} ${student.firstName}".replace(",", "")
                csvBuilder.append("${student.cardId},$safeName,${student.attendanceCount},$pct%\n")
            }

            val fileName = "Asistencia_${section.name.replace(" ", "_")}_${System.currentTimeMillis()}.csv"
            
            // 1. Intentar en Descargas (Público)
            val downloadsDir = android.os.Environment.getExternalStoragePublicDirectory(android.os.Environment.DIRECTORY_DOWNLOADS)
            val reportsDir = File(downloadsDir, "AulaLocal_Reportes")
            
            val file = try {
                if (!reportsDir.exists()) reportsDir.mkdirs()
                File(reportsDir, fileName)
            } catch (e: Exception) {
                // 2. Fallback a carpeta interna de la app (Siempre accesible vía ADB)
                File(context.getExternalFilesDir(null), fileName)
            }

            file.writeText(csvBuilder.toString())
            AppLogger.d("ReportManager", "Reporte de asistencia en: ${file.absolutePath}")
            file
        } catch (e: Exception) {
            AppLogger.e("ReportManager", "Error generating attendance report", e)
            null
        }
    }
}
