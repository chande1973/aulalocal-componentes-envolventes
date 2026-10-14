package com.example.domain

import android.content.Context
import android.os.Environment
import com.example.data.SyncedStudent
import com.example.data.LocalSection
import com.example.data.AppLogger
import com.example.data.AttendanceRecord
import com.example.data.UnitProgress
import com.example.data.UserProfile
import com.example.util.toSafeJSONObject
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

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
                val avgFinal = student.calculateExamAverage(totalUnits)
                val attendancePct = student.calculateAttendancePercentage(section.totalClasses)
                val status = student.getAcademicStatus(totalUnits, section.totalClasses)
                
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

    /**
     * Genera un reporte / boleta personal de calificaciones y asistencia para el alumno.
     */
    fun generateStudentPersonalReport(
        context: Context,
        profile: UserProfile,
        studyProgress: List<UnitProgress>,
        attendanceHistory: List<AttendanceRecord>,
        totalClasses: Int
    ): File? {
        val sb = StringBuilder()
        sb.append("=== BOLETA ACADÉMICA PERSONAL ===\n")
        sb.append("ESTUDIANTE: ${profile.lastName}, ${profile.firstName}\n")
        sb.append("CÉDULA: ${profile.cardId}\n")
        sb.append("SECCIÓN: ${profile.activeSection}\n\n")
        
        sb.append("--- CALIFICACIONES POR UNIDAD ---\n")
        val examGrades = studyProgress.filter { it.examCompleted && it.examGrade != null }
        val avg = if (examGrades.isNotEmpty()) examGrades.sumOf { it.examGrade!!.toDouble() }.toFloat() / examGrades.size else 0f
        sb.append("Promedio Actual: ${"%.2f".format(Locale.US, avg)}\n")
        studyProgress.forEach { p ->
            if (p.examCompleted) {
                sb.append("Unidad ${p.unitIndex}: Nota ${p.examGrade ?: 0f}\n")
            }
        }
        
        sb.append("\n--- HISTORIAL DE ASISTENCIA ---\n")
        val attCount = profile.attendanceCount
        val attPct = if (totalClasses > 0) (attCount.toFloat() / totalClasses * 100).toInt() else 100
        sb.append("Clases Totales: $totalClasses | Asistencias: $attCount | Porcentaje: $attPct%\n")
        attendanceHistory.forEach { rec ->
            val dateStr = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.forLanguageTag("es-ES")).format(Date(rec.date))
            sb.append("• [$dateStr] ${rec.status}\n")
        }

        return try {
            val fileName = "Boleta_${profile.cardId}_${System.currentTimeMillis()}.txt"
            val downloadsDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
            val reportsDir = File(downloadsDir, "AulaLocal_Reportes")
            val file = try {
                if (!reportsDir.exists()) reportsDir.mkdirs()
                File(reportsDir, fileName)
            } catch (e: Exception) {
                File(context.getExternalFilesDir(null), fileName)
            }
            file.writeText(sb.toString())
            file
        } catch (e: Exception) {
            AppLogger.e("ReportManager", "Error generating student personal report", e)
            null
        }
    }

    /**
     * Genera un reporte maestro detallado con asistencia (fecha y hora), exámenes y cuadernos OCR con fecha y hora.
     */
    fun generateDetailedMasterReport(
        context: Context,
        teacherName: String,
        section: LocalSection,
        students: List<SyncedStudent>,
        allAttendanceRecords: List<AttendanceRecord>,
        totalUnits: Int
    ): File? {
        val sb = StringBuilder()
        sb.append("==================================================\n")
        sb.append(" REPORTE MAESTRO DETALLADO DE EVALUACIÓN Y ASISTENCIA\n")
        sb.append("==================================================\n")
        sb.append("PROFESOR: $teacherName\n")
        sb.append("AULA / SECCIÓN: ${section.name}\n")
        sb.append("TOTAL DE CLASES DADAS: ${section.totalClasses}\n")
        sb.append("FECHA DE EMISIÓN: ${SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault()).format(Date())}\n\n")

        students.forEach { student ->
            sb.append("--------------------------------------------------\n")
            sb.append("ESTUDIANTE: ${student.lastName}, ${student.firstName} (Cédula: ${student.cardId})\n")
            sb.append("--------------------------------------------------\n")

            // 1. Asistencia detallada con fecha y hora
            sb.append("• HISTORIAL DE ASISTENCIA:\n")
            val studentAttendance = allAttendanceRecords.filter { it.studentCardId == student.cardId }
            if (studentAttendance.isEmpty()) {
                sb.append("  (Sin registros de asistencia)\n")
            } else {
                studentAttendance.forEach { rec ->
                    val dateStr = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault()).format(Date(rec.date))
                    sb.append("  - Clase ${rec.classNumber} | Fecha y Hora: $dateStr | Estatus: ${rec.status}\n")
                }
            }
            val attendancePct = if (section.totalClasses > 0) (student.attendanceCount.toFloat() / section.totalClasses * 100).toInt() else 0
            sb.append("  -> Asistencias: ${student.attendanceCount} / ${section.totalClasses} ($attendancePct%)\n\n")

            // 2. Exámenes por Unidad
            sb.append("• CALIFICACIONES DE EXÁMENES:\n")
            val gradesObj = student.examGradesString.toSafeJSONObject()
            var examSum = 0f
            var examCount = 0
            for (i in 1..totalUnits) {
                val gradeStr = gradesObj.optString(i.toString(), "")
                if (gradeStr.isNotEmpty()) {
                    val g = gradeStr.toFloatOrNull() ?: 0f
                    examSum += g
                    examCount++
                    val status = when {
                        g >= 11.5f -> "APROBADO"
                        g >= 6.0f -> "PER"
                        else -> "REPROBADO"
                    }
                    sb.append("  - Unidad $i: ${"%.2f".format(Locale.US, g)} / 20 [$status]\n")
                } else {
                    sb.append("  - Unidad $i: Sin presentar\n")
                }
            }
            val examAvg = if (examCount > 0) examSum / examCount else 0f
            sb.append("  -> Promedio de Exámenes: ${"%.2f".format(Locale.US, examAvg)}\n\n")

            // 3. Cuadernos OCR por Unidad
            sb.append("• EVALUACIONES OCR DE CUADERNO:\n")
            val nbGradesObj = student.notebookGradesString.toSafeJSONObject()
            val nbFeedbacksObj = student.notebookFeedbacksString.toSafeJSONObject()
            var nbSum = 0f
            var nbCount = 0
            for (i in 1..totalUnits) {
                val nbGradeStr = nbGradesObj.optString(i.toString(), "")
                if (nbGradeStr.isNotEmpty()) {
                    val g = nbGradeStr.toFloatOrNull() ?: 0f
                    nbSum += g
                    nbCount++
                    val status = when {
                        g >= 11.5f -> "APROBADO"
                        g >= 6.0f -> "PER"
                        else -> "REPROBADO"
                    }
                    val feedback = nbFeedbacksObj.optString(i.toString(), "")
                    sb.append("  - Unidad $i: ${"%.2f".format(Locale.US, g)} / 20 [$status] | Feedback: $feedback\n")
                } else {
                    sb.append("  - Unidad $i: Sin escanear\n")
                }
            }
            val nbAvg = if (nbCount > 0) nbSum / nbCount else 0f
            sb.append("  -> Promedio de Cuadernos OCR: ${"%.2f".format(Locale.US, nbAvg)}\n\n")
        }

        return try {
            val fileName = "Reporte_Master_${section.name.replace(" ", "_")}_${System.currentTimeMillis()}.txt"
            val downloadsDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
            val reportsDir = File(downloadsDir, "AulaLocal_Reportes")
            val file = try {
                if (!reportsDir.exists()) reportsDir.mkdirs()
                File(reportsDir, fileName)
            } catch (e: Exception) {
                File(context.getExternalFilesDir(null), fileName)
            }
            file.writeText(sb.toString())
            AppLogger.d("ReportManager", "Reporte maestro detallado generado en: ${file.absolutePath}")
            file
        } catch (e: Exception) {
            AppLogger.e("ReportManager", "Error generating detailed master report", e)
            null
        }
    }
}
