package com.example.domain

import com.example.data.UnitProgress
import com.example.network.NetworkConstants
import org.junit.Assert.*
import org.junit.Test

class NewFeaturesUnitTest {

    @Test
    fun `test UnitProgress scroll memory and reading percentage updates`() {
        val initialProgress = UnitProgress(
            unitIndex = 1,
            isRead = false,
            readPercentage = 45,
            lastScrollPosition = 350
        )

        assertEquals(1, initialProgress.unitIndex)
        assertFalse(initialProgress.isRead)
        assertEquals(45, initialProgress.readPercentage)
        assertEquals(350, initialProgress.lastScrollPosition)

        // Simular actualización cuando el alumno completa la lectura
        val updatedProgress = initialProgress.copy(
            readPercentage = 100,
            isRead = true,
            lastScrollPosition = 1200
        )

        assertTrue(updatedProgress.isRead)
        assertEquals(100, updatedProgress.readPercentage)
        assertEquals(1200, updatedProgress.lastScrollPosition)
    }

    @Test
    fun `test official class finalize network command string`() {
        assertEquals("class_finalize_official", NetworkConstants.CMD_CLASS_FINALIZE_OFFICIAL)
        assertEquals("FORCE_DISCONNECT", NetworkConstants.CMD_FORCE_DISCONNECT)
        assertEquals("attendance_check", NetworkConstants.CMD_ATTENDANCE_CHECK)
    }

    @Test
    fun `test dynamic final grade calculation for variable number of units`() {
        fun calculateFinalCourseGrade(unitGrades: List<Float>): Float {
            if (unitGrades.isEmpty()) return 0f
            return unitGrades.sum() / unitGrades.size
        }

        // Caso 1: Curso de 11 unidades (180 / 11 = 16.3636)
        val grades11Units = listOf(16f, 18f, 14f, 15f, 17f, 19f, 12f, 15f, 16f, 18f, 20f)
        val finalGrade11 = calculateFinalCourseGrade(grades11Units)
        assertEquals(16.36f, finalGrade11, 0.01f)

        // Caso 2: Curso de 8 unidades (101 / 8 = 12.625)
        val grades8Units = listOf(10f, 12f, 14f, 15f, 11f, 13f, 12f, 14f)
        val finalGrade8 = calculateFinalCourseGrade(grades8Units)
        assertEquals(12.625f, finalGrade8, 0.01f)

        // Caso 3: Curso de 15 unidades
        val grades15Units = List(15) { 20f }
        val finalGrade15 = calculateFinalCourseGrade(grades15Units)
        assertEquals(20.0f, finalGrade15, 0.01f)
    }

    @Test
    fun `test group reading average percentage calculation`() {
        val studentReadPercentages = listOf(100, 65, 30, 100, 85)
        val totalStudents = studentReadPercentages.size
        val avgPercentage = if (totalStudents > 0) studentReadPercentages.sum().toFloat() / totalStudents else 0f

        assertEquals(76.0f, avgPercentage, 0.01f)
    }
}
