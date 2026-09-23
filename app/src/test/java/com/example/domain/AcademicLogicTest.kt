package com.example.domain

import org.junit.Assert.assertEquals
import org.junit.Test

class AcademicLogicTest {

    @Test
    fun `test attendance percentage calculation`() {
        val attendanceCount = 3
        val totalClasses = 4
        
        val attendancePct = if (totalClasses > 0) (attendanceCount.toFloat() / totalClasses) else 0f
        
        assertEquals(0.75f, attendancePct, 0.01f)
    }

    @Test
    fun `test average grade calculation`() {
        val grades = listOf(15f, 10f, 14f)
        val average = if (grades.isNotEmpty()) grades.sum() / grades.size else 0f
        
        assertEquals(13f, average, 0.01f)
    }

    @Test
    fun `test pass status logic`() {
        // Caso: Aprobado justo en el límite
        val grade1 = 12f
        val attendance1 = 0.75f
        val isPassed1 = grade1 >= 12f && attendance1 >= 0.75f
        assertEquals(true, isPassed1)

        // Caso: Buena nota pero mala asistencia
        val grade2 = 18f
        val attendance2 = 0.50f
        val isPassed2 = grade2 >= 12f && attendance2 >= 0.75f
        assertEquals(false, isPassed2)

        // Caso: Mala nota pero buena asistencia
        val grade3 = 08f
        val attendance3 = 1.0f
        val isPassed3 = grade3 >= 12f && attendance3 >= 0.75f
        assertEquals(false, isPassed3)
    }

    @Test
    fun `test semaphore logic alignment`() {
        fun getStatus(grade: Float, attendance: Float): String {
            return when {
                grade >= 12f && attendance >= 0.75f -> "APROBADO"
                grade < 10f || attendance < 0.75f -> "CRÍTICO"
                else -> "EN RIESGO"
            }
        }

        assertEquals("APROBADO", getStatus(12f, 0.75f))
        assertEquals("CRÍTICO", getStatus(15f, 0.70f)) // Falla por asistencia
        assertEquals("CRÍTICO", getStatus(09f, 1.00f)) // Falla por nota muy baja
        assertEquals("EN RIESGO", getStatus(11f, 0.80f)) // Nota baja pero no crítica
    }
}
