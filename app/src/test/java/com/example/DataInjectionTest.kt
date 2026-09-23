package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.data.*
import com.example.domain.ReportManager
import com.example.util.TestDataInjector
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@OptIn(ExperimentalCoroutinesApi::class)
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class DataInjectionTest {

    private lateinit var context: Context
    private lateinit var database: AppDatabase
    private lateinit var dao: ClassroomDao

    @Before
    fun setup() {
        context = ApplicationProvider.getApplicationContext()
        database = androidx.room.Room.inMemoryDatabaseBuilder(context, AppDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        dao = database.classroomDao()
    }

    @Test
    fun `test injection for a full school year 36 weeks`() = runTest {
        val sectionName = "36-Weeks-Year"
        val weeks = 36
        val studentsCount = 10
        val totalUnits = 12

        TestDataInjector.injectSimulationData(dao, sectionName, weeks, studentsCount, totalUnits)

        // Verificar sección
        val section = dao.getSectionByName(sectionName)
        assertNotNull(section)
        assertEquals(weeks, section!!.totalClasses)

        // Verificar alumnos
        val students = dao.getSyncedStudentsBySectionList(sectionName)
        assertEquals(studentsCount, students.size)

        // Verificar que tengan historial de asistencia
        students.forEach { student ->
            val history = dao.getAttendanceByStudentFlow(student.cardId).first()
            assertEquals("Debe tener 36 registros de asistencia", weeks, history.size)
        }
    }

    @Test
    fun `test report generation with injected 36 weeks data`() = runTest {
        val sectionName = "Report-Test-Year"
        val weeks = 36
        TestDataInjector.injectSimulationData(dao, sectionName, weeks, studentsCount = 5, totalUnits = 12)

        val section = dao.getSectionByName(sectionName)!!
        val students = dao.getSyncedStudentsBySectionList(sectionName)
        
        val reportFile = ReportManager.generateAcademicReport(context, "Test Teacher", section, students, totalUnits = 12)
        assertNotNull(reportFile)
        
        val content = reportFile!!.readText()
        assertTrue("El reporte debe contener la sección", content.contains(sectionName))
        
        // Verificar que aparezcan los 5 alumnos
        students.forEach { student ->
            assertTrue("Debe contener la cédula ${student.cardId}", content.contains(student.cardId))
        }
    }
}
