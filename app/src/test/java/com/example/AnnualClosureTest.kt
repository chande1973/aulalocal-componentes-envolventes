package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.data.*
import com.example.domain.ReportManager
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import java.io.File

@OptIn(ExperimentalCoroutinesApi::class)
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class AnnualClosureTest {

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
    fun `test academic report passing logic`() {
        val section = LocalSection(name = "TestSection", totalClasses = 10, plannedClasses = 36)
        val students = listOf(
            SyncedStudent(
                cardId = "V-PASS",
                firstName = "John",
                lastName = "Doe",
                email = "john@example.com",
                avatar = "Geek",
                sectionName = "TestSection",
                attendanceCount = 8, // 80% attendance
                examGradesString = "{\"1\":\"15\", \"2\":\"17\"}" // Avg 16
            ),
            SyncedStudent(
                cardId = "V-FAIL-GRADE",
                firstName = "Bad",
                lastName = "Grades",
                email = "bad@example.com",
                avatar = "Geek",
                sectionName = "TestSection",
                attendanceCount = 9, // 90% attendance
                examGradesString = "{\"1\":\"10\", \"2\":\"10\"}" // Avg 10
            ),
            SyncedStudent(
                cardId = "V-FAIL-ATTENDANCE",
                firstName = "Absent",
                lastName = "Student",
                email = "",
                avatar = "",
                sectionName = "TestSection",
                attendanceCount = 5, // 50% attendance
                examGradesString = "{\"1\":\"20\"}" // Avg 20
            ),
            SyncedStudent(
                cardId = "V-PASS-ROUND",
                firstName = "Round",
                lastName = "Student",
                email = "",
                avatar = "",
                sectionName = "TestSection",
                attendanceCount = 8,
                examGradesString = "{\"1\":\"11.5\"}" // Avg 11.5 -> Should Pass
            )
        )

        val reportFile = ReportManager.generateAcademicReport(context, "Prof. X", section, students, 2)
        assertNotNull("Report file should be generated", reportFile)
        
        val content = reportFile!!.readText()
        
        // V-PASS: 80%, 16.00, APROBADO
        assertTrue("John Doe should be APROBADO", content.contains("V-PASS,Doe John,80%,16.00,APROBADO"))
        
        // V-FAIL-GRADE: 90%, 10.00, REPROBADO
        assertTrue("Bad Grades should be REPROBADO", content.contains("V-FAIL-GRADE,Grades Bad,90%,10.00,REPROBADO"))
        
        // V-FAIL-ATTENDANCE: 50%, 20.00, REPROBADO
        assertTrue("Absent Student should be REPROBADO", content.contains("V-FAIL-ATTENDANCE,Student Absent,50%,20.00,REPROBADO"))

        // V-PASS-ROUND: 80%, 11.50, APROBADO
        assertTrue("Round Student should be APROBADO", content.contains("V-PASS-ROUND,Student Round,80%,11.50,APROBADO"))
    }

    @Test
    fun `test attendance report generation`() {
        val section = LocalSection(name = "AttendanceTest", totalClasses = 20, plannedClasses = 36)
        val students = listOf(
            SyncedStudent(
                cardId = "V-1",
                firstName = "A",
                lastName = "B",
                email = "",
                avatar = "",
                sectionName = "AttendanceTest",
                attendanceCount = 15 // 75%
            )
        )

        val reportFile = ReportManager.generateAttendanceReport(context, section, students)
        assertNotNull(reportFile)
        
        val content = reportFile!!.readText()
        assertTrue("Should contain attendance percentage", content.contains("V-1,B A,15,75%"))
    }

    @Test
    fun `test app reset clears all essential tables`() = runTest {
        // Prepare data
        dao.insertProfile(UserProfile(role = "teacher", firstName = "T", lastName = "P", email = "t@p.com"))
        dao.insertSection(LocalSection(name = "S1"))
        dao.insertSyncedStudent(SyncedStudent(cardId = "C1", firstName = "S", lastName = "1", email = "", avatar = "", sectionName = "S1"))
        dao.insertProgress(UnitProgress(unitIndex = 1, isRead = true))
        dao.insertAttendanceRecord(AttendanceRecord(studentCardId = "C1", studentName = "S 1", sectionName = "S1", status = "Presente"))
        dao.insertInboxMessage(InboxMessage(sender = "T", content = "Msg"))

        // Verify data exists
        assertNotNull(dao.getUserProfile())
        assertEquals(1, dao.getAllSections().size)
        assertEquals(1, dao.getAllSyncedStudentsList().size)
        assertEquals(1, dao.getAllProgress().size)

        // Reset (Manual trigger of DAO methods like MainViewModel.resetApp does)
        dao.clearProfile()
        dao.clearSyncedStudents()
        dao.clearProgress()
        dao.clearAllSections()
        dao.clearAttendanceHistory()
        dao.clearInbox()

        // Verify data is cleared
        assertNull(dao.getUserProfile())
        assertEquals(0, dao.getAllSections().size)
        assertEquals(0, dao.getAllSyncedStudentsList().size)
        assertEquals(0, dao.getAllProgress().size)
    }
}
