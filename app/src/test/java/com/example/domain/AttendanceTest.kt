package com.example.domain

import org.junit.Test
import org.junit.Assert.*

class AttendanceTest {

    @Test
    fun testAttendancePercentageCalculation() {
        val totalClasses = 10
        val studentAttendance = 7
        
        val percentage = (studentAttendance.toFloat() / totalClasses) * 100
        
        // El alumno tiene 70%, debería estar "En Riesgo" (meta 75%)
        assertTrue(percentage < 75)
        assertEquals(70f, percentage, 0.1f)
    }

    @Test
    fun testPassingGrade() {
        val totalClasses = 20
        val studentAttendance = 15 // 75% justo
        
        val percentage = (studentAttendance.toFloat() / totalClasses) * 100
        
        // Con 75% debería estar aprobado según la regla de la app
        assertTrue(percentage >= 75)
    }
}
