package com.example.domain

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.data.AttendanceRecord
import com.example.data.UnitProgress
import com.example.data.UserProfile
import org.junit.Assert.*
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class StudentReportTest {

    @Test
    fun `test student personal report generation`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val profile = UserProfile(
            role = "student",
            email = "maria@test.com",
            cardId = "V-12345",
            firstName = "María",
            lastName = "González",
            activeSection = "Sección B",
            attendanceCount = 8
        )
        val studyProgress = listOf(
            UnitProgress(unitIndex = 1, examCompleted = true, examGrade = 18f, isRead = true),
            UnitProgress(unitIndex = 2, examCompleted = true, examGrade = 16f, isRead = true)
        )
        val attendanceHistory = listOf(
            AttendanceRecord(studentCardId = "V-12345", studentName = "González, María", sectionName = "Sección B", classNumber = 1, status = "Presente"),
            AttendanceRecord(studentCardId = "V-12345", studentName = "González, María", sectionName = "Sección B", classNumber = 2, status = "Presente")
        )

        val reportFile = ReportManager.generateStudentPersonalReport(context, profile, studyProgress, attendanceHistory, totalClasses = 10)
        assertNotNull(reportFile)
        val content = reportFile!!.readText()

        assertTrue("Debe contener la cédula del alumno", content.contains("V-12345"))
        assertTrue("Debe contener el promedio de notas (17.00)", content.contains("17.00"))
        assertTrue("Debe contener el porcentaje de asistencia (80%)", content.contains("80%"))
        assertTrue("Debe contener el registro de asistencia Presente", content.contains("Presente"))
    }
}
