package com.example.domain

import com.example.data.ClassroomDao
import com.example.data.SyncedStudent
import com.example.data.AttendanceRecord
import com.example.data.LocalSection
import com.example.data.LocalNetworkManager

object AttendanceManager {

    /**
     * Procesa el fin de una clase, actualizando contadores y guardando registros históricos.
     */
    suspend fun finalizeAttendance(
        dao: ClassroomDao,
        section: LocalSection,
        students: List<SyncedStudent>
    ) {
        val totalCheckpoints = LocalNetworkManager.getSessionCheckpoints()
        
        students.forEach { student ->
            // 1. Determinar estado (Auto vs Manual)
            val automaticStatus = if (student.checkpointCount >= totalCheckpoints * 0.6) "Presente" else "Ausente"
            val finalStatus = student.manualAttendanceStatus ?: automaticStatus

            // 2. Incrementar contador acumulado
            val isPresent = finalStatus == "Presente" || finalStatus == "Retraso"
            val newAttendanceCount = if (isPresent) student.attendanceCount + 1 else student.attendanceCount
            
            dao.insertSyncedStudent(student.copy(
                attendanceCount = newAttendanceCount,
                manualAttendanceStatus = null // Reset para la próxima clase
            ))

            // 3. Guardar registro histórico
            val nextClassNumber = section.totalClasses + 1
            dao.insertAttendanceRecord(AttendanceRecord(
                studentCardId = student.cardId,
                studentName = "${student.lastName}, ${student.firstName}",
                sectionName = section.name,
                classNumber = nextClassNumber,
                status = finalStatus
            ))
        }
    }
}
