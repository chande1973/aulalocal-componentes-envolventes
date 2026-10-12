package com.example.domain

import com.example.data.SyncedStudent
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class SyncedStudentAcademicLogicTest {

    @Test
    fun testCalculateExamAverage_withValidGrades() {
        val gradesJson = """{"1": 15.0, "2": 18.0, "3": 12.0}"""
        val student = SyncedStudent(
            cardId = "12345",
            firstName = "Carlos",
            lastName = "Pérez",
            email = "carlos@example.com",
            avatar = "🤓",
            sectionName = "Aula 101",
            examGradesString = gradesJson
        )

        val avg = student.calculateExamAverage(totalUnits = 3)
        assertEquals(15.0f, avg, 0.01f)
    }

    @Test
    fun testCalculateExamAverage_emptyGrades_returnsZero() {
        val student = SyncedStudent(
            cardId = "12345",
            firstName = "Carlos",
            lastName = "Pérez",
            email = "carlos@example.com",
            avatar = "🤓",
            sectionName = "Aula 101",
            examGradesString = "{}"
        )

        val avg = student.calculateExamAverage(totalUnits = 11)
        assertEquals(0.0f, avg, 0.01f)
    }

    @Test
    fun testCalculateAttendancePercentage() {
        val student = SyncedStudent(
            cardId = "12345",
            firstName = "Ana",
            lastName = "García",
            email = "ana@example.com",
            avatar = "🖥️",
            sectionName = "Aula 101",
            attendanceCount = 18
        )

        val pct = student.calculateAttendancePercentage(totalClasses = 20)
        assertEquals(90, pct)
    }

    @Test
    fun testGetAcademicStatus_Aprobado() {
        val gradesJson = """{"1": 16.0, "2": 17.0}"""
        val student = SyncedStudent(
            cardId = "12345",
            firstName = "Maria",
            lastName = "Lopez",
            email = "maria@example.com",
            avatar = "🔧",
            sectionName = "Aula 101",
            attendanceCount = 10,
            examGradesString = gradesJson
        )

        val status = student.getAcademicStatus(totalUnits = 2, totalClasses = 10)
        assertEquals("APROBADO", status)
    }

    @Test
    fun testGetAcademicStatus_LowAttendance_Reprobado() {
        val gradesJson = """{"1": 20.0, "2": 20.0}"""
        val student = SyncedStudent(
            cardId = "12345",
            firstName = "Juan",
            lastName = "Ríos",
            email = "juan@example.com",
            avatar = "📡",
            sectionName = "Aula 101",
            attendanceCount = 5, // 5/10 = 50% (< 75%)
            examGradesString = gradesJson
        )

        val status = student.getAcademicStatus(totalUnits = 2, totalClasses = 10)
        assertEquals("REPROBADO", status)
    }
}
