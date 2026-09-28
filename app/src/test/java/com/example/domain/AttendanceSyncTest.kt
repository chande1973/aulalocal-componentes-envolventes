package com.example.domain

import com.example.data.AttendanceRecord
import com.example.data.LocalSection
import org.junit.Assert.assertEquals
import org.junit.Test

class AttendanceSyncTest {

    @Test
    fun testAttendanceRecordIdGeneration() {
        val date = 1700000000000L
        val record1 = AttendanceRecord(
            studentCardId = "V-123456",
            studentName = "Pérez, Juan",
            sectionName = "Sección A",
            classNumber = 1,
            date = date,
            status = "Presente"
        )
        
        assertEquals("V-123456_Sección A_class_1", record1.id)
    }

    @Test
    fun testSectionClassesAndPlannedWeeksTerminology() {
        val section = LocalSection(
            name = "Informatica",
            totalClasses = 5,
            plannedClasses = 36
        )
        
        assertEquals(5, section.totalClasses)
        assertEquals(36, section.plannedClasses)
    }
}
