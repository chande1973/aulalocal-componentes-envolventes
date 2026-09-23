package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.data.LocalSection
import com.example.data.SyncedStudent
import com.example.domain.ReportManager
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import java.io.File

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class DomainLogicTest {

    @Test
    fun `test academic report generation`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val section = LocalSection(name = "102", totalClasses = 10)
        val students = listOf(
            SyncedStudent(
                cardId = "12345678",
                firstName = "Alumno",
                lastName = "Prueba",
                email = "test@example.com",
                avatar = "🤓",
                sectionName = "102",
                attendanceCount = 8,
                examGradesString = "{\"1\":\"20\", \"2\":\"15\"}"
            )
        )

        val reportFile = ReportManager.generateAcademicReport(context, "Profesor Test", section, students, totalUnits = 11)
        
        assertNotNull(reportFile)
        assertTrue(reportFile!!.exists())
        
        val content = reportFile.readText()
        assertTrue(content.contains("Profesor Test"))
        assertTrue(content.contains("102"))
        assertTrue(content.contains("Prueba Alumno"))
        assertTrue(content.contains("80%")) // 8/10 = 80%
        assertTrue(content.contains("17.50")) // (20+15)/2 = 17.5
        assertTrue(content.contains("APROBADO"))
    }

    @Test
    fun `test attendance report generation`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val section = LocalSection(name = "102", totalClasses = 10)
        val students = listOf(
            SyncedStudent(
                cardId = "12345678",
                firstName = "Alumno",
                lastName = "Prueba",
                email = "test@example.com",
                avatar = "🤓",
                sectionName = "102",
                attendanceCount = 5,
                examGradesString = "{}"
            )
        )

        val reportFile = ReportManager.generateAttendanceReport(context, section, students)
        
        assertNotNull(reportFile)
        assertTrue(reportFile!!.exists())
        
        val content = reportFile.readText()
        assertTrue(content.contains("102"))
        assertTrue(content.contains("50%")) // 5/10 = 50%
    }
}
