package com.example.domain

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class ExamNotificationTest {

    @Test
    fun testEnabledExamUnitsParsing() {
        val enabledExamUnits = "1,3,5"
        val activeUnits = enabledExamUnits.split(",").filter { it.isNotEmpty() }.map { it.toInt() }

        assertEquals(3, activeUnits.size)
        assertTrue(activeUnits.contains(1))
        assertTrue(activeUnits.contains(3))
        assertTrue(activeUnits.contains(5))
    }
}
