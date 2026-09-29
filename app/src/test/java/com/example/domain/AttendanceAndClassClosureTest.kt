package com.example.domain

import com.example.data.AttendanceRecord
import com.example.network.NetworkConstants
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Test

class AttendanceAndClassClosureTest {

    @Test
    fun testClassFinalizeCommandConstant() {
        assertEquals("class_finalize_official", NetworkConstants.CMD_CLASS_FINALIZE_OFFICIAL)
    }

    @Test
    fun testAttendanceRecordTimestampFormatting() {
        val now = System.currentTimeMillis()
        val record = AttendanceRecord(
            studentCardId = "V-999",
            studentName = "García, Ana",
            sectionName = "Sección C",
            classNumber = 3,
            date = now,
            status = "Presente"
        )

        assertNotNull(record)
        assertEquals(now, record.date)
        assertEquals("V-999_Sección C_class_3", record.id)
    }
}
