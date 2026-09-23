package com.example.domain

import org.junit.Test
import org.junit.Assert.*

class GradesTest {

    @Test
    fun testAverageCalculation() {
        // GIVEN: Un alumno con 3 notas en un mapa (simulando JSONObject)
        val gradesMap = mapOf("1" to 10f, "2" to 15f, "3" to 20f)
        
        // WHEN: Calculamos el promedio
        var sum = 0f
        var count = 0
        for (i in 1..11) {
            val g = gradesMap[i.toString()]
            if (g != null) {
                sum += g
                count++
            }
        }
        val average = if (count > 0) sum / count else 0f
        
        // THEN: El promedio de (10+15+20)/3 es 15
        assertEquals(15f, average, 0.01f)
    }

    @Test
    fun testPassingStatus() {
        // GIVEN: Nota 12 (mínimo aprobado) y asistencia 75% (mínimo aprobado)
        val avgFinal = 12f
        val attendancePct = 75
        
        // WHEN: Aplicamos la regla de negocio
        val isPassing = avgFinal >= 12 && attendancePct >= 75
        
        // THEN: Debería estar aprobado
        assertTrue(isPassing)
    }

    @Test
    fun testFailingStatusByGrades() {
        // GIVEN: Nota 11 (reprobado) pero asistencia 100% (excelente)
        val avgFinal = 11.9f
        val attendancePct = 100
        
        // WHEN: Aplicamos la regla
        val isPassing = avgFinal >= 12 && attendancePct >= 75
        
        // THEN: Debería estar REPROBADO (la nota manda)
        assertFalse(isPassing)
    }

    @Test
    fun testFailingStatusByAttendance() {
        // GIVEN: Nota 20 (excelente) pero asistencia 74% (insuficiente)
        val avgFinal = 20f
        val attendancePct = 74
        
        // WHEN: Aplicamos la regla
        val isPassing = avgFinal >= 12 && attendancePct >= 75
        
        // THEN: Debería estar REPROBADO (la asistencia manda)
        assertFalse(isPassing)
    }
}
