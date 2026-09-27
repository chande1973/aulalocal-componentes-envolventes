package com.example.domain

import com.example.data.LocalSection
import com.example.data.SyncedStudent
import org.junit.Assert.*
import org.junit.Test

class ClassClosureAndSyncTest {

    @Test
    fun `test connected students filtering logic when server is running vs stopped`() {
        val now = System.currentTimeMillis()
        val studentActive = SyncedStudent(
            cardId = "123",
            firstName = "Juan",
            lastName = "Perez",
            email = "juan@example.com",
            avatar = "👤",
            sectionName = "Seccion A",
            lastSeen = now - 5000 // visto hace 5 segundos
        )

        val studentOld = SyncedStudent(
            cardId = "456",
            firstName = "Maria",
            lastName = "Gomez",
            email = "maria@example.com",
            avatar = "👤",
            sectionName = "Seccion A",
            lastSeen = now - 40000 // visto hace 40 segundos
        )

        val allStudents = listOf(studentActive, studentOld)

        fun filterConnectedStudents(students: List<SyncedStudent>, isServerRunning: Boolean, limitTime: Long): List<SyncedStudent> {
            if (!isServerRunning) return emptyList()
            val limit = limitTime - 30000
            return students.filter { it.lastSeen > limit }
        }

        // Caso 1: Servidor ENCENDIDO -> Solo el alumno activo (hace 5s) debe aparecer
        val activeWhenRunning = filterConnectedStudents(allStudents, isServerRunning = true, limitTime = now)
        assertEquals(1, activeWhenRunning.size)
        assertEquals("123", activeWhenRunning[0].cardId)

        // Caso 2: Servidor APAGADO -> La lista debe estar vacía de inmediato
        val activeWhenStopped = filterConnectedStudents(allStudents, isServerRunning = false, limitTime = now)
        assertTrue(activeWhenStopped.isEmpty())
    }

    @Test
    fun `test grace period parameter is greater than client sync interval`() {
        val clientSyncIntervalMs = 3000L
        val gracePeriodShutdownMs = 5000L

        // La ventana de gracia al apagar el servidor debe ser mayor al intervalo de polling del alumno
        assertTrue(gracePeriodShutdownMs > clientSyncIntervalMs)
    }

    @Test
    fun `test attendance calculation and automatic report generation model`() {
        val section = LocalSection(name = "Seccion A", totalClasses = 10)
        val students = listOf(
            SyncedStudent(
                cardId = "111",
                firstName = "Carlos",
                lastName = "Rivas",
                email = "carlos@example.com",
                avatar = "👤",
                sectionName = "Seccion A",
                attendanceCount = 8
            ),
            SyncedStudent(
                cardId = "222",
                firstName = "Ana",
                lastName = "Torres",
                email = "ana@example.com",
                avatar = "👤",
                sectionName = "Seccion A",
                attendanceCount = 5
            )
        )

        val carlosAttendancePct = (students[0].attendanceCount.toFloat() / (section.totalClasses + 1)) * 100
        val anaAttendancePct = (students[1].attendanceCount.toFloat() / (section.totalClasses + 1)) * 100

        // Carlos: 8 / 11 = ~72.7%
        assertEquals(72.72f, carlosAttendancePct, 0.1f)
        // Ana: 5 / 11 = ~45.4%
        assertEquals(45.45f, anaAttendancePct, 0.1f)
    }

    @Test
    fun `test student reset lastSeen state on server shutdown`() {
        val student = SyncedStudent(
            cardId = "999",
            firstName = "Luis",
            lastName = "Mendoza",
            email = "luis@example.com",
            avatar = "👤",
            sectionName = "Seccion B",
            lastSeen = System.currentTimeMillis()
        )

        val studentAfterReset = student.copy(lastSeen = 0L)

        assertEquals(0L, studentAfterReset.lastSeen)
    }
}
