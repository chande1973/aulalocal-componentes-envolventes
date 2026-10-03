package com.example.data

import androidx.room.Dao
import androidx.room.Entity
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.PrimaryKey
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Entity(tableName = "user_profile")
data class UserProfile(
    @PrimaryKey val id: Int = 1,
    val role: String, // "teacher" or "student"
    val firstName: String,
    val lastName: String,
    val email: String,
    val phone: String = "",
    val gender: String = "", // "Masculino" / "Femenino"
    val cardId: String = "", // Cédula
    val avatar: String = "", // Geek, SysAdmin, Soporte, Redes, etc.
    val activeSection: String = "",
    val registeredTeacherIp: String = "",
    val registeredTeacherPort: Int = 9999,
    val isLocked: Boolean = false,
    val materialsFolderUri: String = "",
    val currentLapso: Int = 1, // Corte académico actual (1, 2, 3, 4)
    val lastSessionStartTime: Long = 0L,
    val attendanceCount: Int = 0 // Clases asistidas acumuladas (solo alumnos)
)

@Entity(tableName = "local_sections")
data class LocalSection(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val name: String,
    val totalClasses: Int = 0,
    val plannedClasses: Int = 36, // Meta de encuentros totales para el año
    val startTimestamp: Long = System.currentTimeMillis(),
    val currentWeekOffset: Int = 0,
    val enabledReadUnits: String = "1",
    val enabledExamUnits: String = "",
    val enabledNotebookUnits: String = "",
    val teacherMessage: String = ""
)

@Entity(tableName = "synced_students")
data class SyncedStudent(
    @PrimaryKey val cardId: String,
    val firstName: String,
    val lastName: String,
    val email: String,
    val avatar: String,
    val sectionName: String,
    val lastSeen: Long = System.currentTimeMillis(),
    val firstSeenInSession: Long = System.currentTimeMillis(),
    val checkpointCount: Int = 0,
    val lastCheckpointSeen: Int = -1,
    val hasDoubt: Boolean = false,
    val doubtMessage: String = "",
    val isProfileUnlockRequested: Boolean = false,
    val privateMessage: String = "",
    val privateFileUri: String = "",
    val privateFileName: String = "",
    val privateFileSize: Long = 0L,
    val privateFileId: String = "",
    val attendanceCount: Int = 0, // Clases aprobadas acumuladas
    val manualAttendanceStatus: String? = null, // "Presente", "Ausente", "Retraso" (Sobrepasa el cálculo automático)
    val readProgressString: String = "[]",
    val challengeProgressString: String = "[]",
    val examGradesString: String = "{}",
    val notebookGradesString: String = "{}",
    val notebookFeedbacksString: String = "{}"
)

@Entity(tableName = "attendance_history")
data class AttendanceRecord(
    @PrimaryKey val id: String,
    val studentCardId: String,
    val studentName: String,
    val sectionName: String,
    val classNumber: Int = 1,
    val date: Long = System.currentTimeMillis(),
    val status: String // "Presente", "Ausente", "Retraso"
) {
    constructor(
        studentCardId: String,
        studentName: String,
        sectionName: String,
        classNumber: Int = 1,
        date: Long = System.currentTimeMillis(),
        status: String
    ) : this(
        id = "${studentCardId}_${sectionName}_class_$classNumber",
        studentCardId = studentCardId,
        studentName = studentName,
        sectionName = sectionName,
        classNumber = classNumber,
        date = date,
        status = status
    )
}

@Entity(tableName = "unit_progress")
data class UnitProgress(
    @PrimaryKey val unitIndex: Int, // 1 to 11
    val isRead: Boolean = false,
    val readPercentage: Int = 0,
    val lastScrollPosition: Int = 0,
    val isChallengeDone: Boolean = false,
    val examGrade: Float? = null,
    val examCompleted: Boolean = false,
    val isRecuperativoUsed: Boolean = false,
    val recuperativoRequested: Boolean = false,
    val notebookGrade: Float? = null,
    val notebookEvaluated: Boolean = false,
    val notebookFeedback: String = ""
)

@Entity(tableName = "inbox_messages")
data class InboxMessage(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val sender: String,
    val content: String,
    val timestamp: Long = System.currentTimeMillis(),
    val type: String = "TEXT", // "TEXT", "FILE"
    val fileName: String? = null,
    val fileSize: Long? = null,
    val fileId: String? = null,
    val localUri: String? = null
)

@Dao
interface ClassroomDao {
    @Query("SELECT * FROM user_profile WHERE id = 1 LIMIT 1")
    fun getUserProfileFlow(): Flow<UserProfile?>

    @Query("SELECT * FROM user_profile WHERE id = 1 LIMIT 1")
    suspend fun getUserProfile(): UserProfile?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertProfile(profile: UserProfile)

    @Query("DELETE FROM user_profile")
    suspend fun clearProfile()

    // Sections (Teacher)
    @Query("SELECT * FROM local_sections ORDER BY id DESC")
    fun getAllSectionsFlow(): Flow<List<LocalSection>>

    @Query("SELECT * FROM local_sections ORDER BY id DESC")
    suspend fun getAllSections(): List<LocalSection>

    @Query("SELECT * FROM local_sections WHERE name = :name LIMIT 1")
    suspend fun getSectionByName(name: String): LocalSection?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSection(section: LocalSection)

    @Query("DELETE FROM local_sections")
    suspend fun clearAllSections()

    @Query("DELETE FROM local_sections WHERE id = :id")
    suspend fun deleteSection(id: Int)

    @Query("SELECT COUNT(*) FROM local_sections")
    suspend fun getSectionCount(): Int

    // Synced Students (Teacher)
    @Query("SELECT * FROM synced_students ORDER BY lastName ASC, firstName ASC")
    fun getSyncedStudentsFlow(): Flow<List<SyncedStudent>>

    @Query("SELECT * FROM synced_students ORDER BY lastName ASC, firstName ASC")
    suspend fun getAllSyncedStudentsList(): List<SyncedStudent>

    @Query("SELECT * FROM synced_students WHERE sectionName = :sectionName ORDER BY lastName ASC, firstName ASC")
    fun getSyncedStudentsBySectionFlow(sectionName: String): Flow<List<SyncedStudent>>

    @Query("SELECT * FROM synced_students WHERE sectionName = :sectionName ORDER BY lastName ASC, firstName ASC")
    suspend fun getSyncedStudentsBySectionList(sectionName: String): List<SyncedStudent>

    @Query("SELECT * FROM synced_students WHERE cardId = :cardId LIMIT 1")
    suspend fun getStudentByCardId(cardId: String): SyncedStudent?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSyncedStudent(student: SyncedStudent)

    @Query("DELETE FROM synced_students WHERE lastSeen < :timeout")
    suspend fun removeInactiveStudents(timeout: Long)

    @Query("DELETE FROM synced_students WHERE cardId = :cardId")
    suspend fun removeStudentByCardId(cardId: String)

    @Query("UPDATE synced_students SET checkpointCount = 0, lastCheckpointSeen = -1 WHERE sectionName = :sectionName")
    suspend fun resetSectionCheckpoints(sectionName: String)

    @Query("UPDATE synced_students SET lastSeen = 0")
    suspend fun resetAllStudentsLastSeen()

    @Query("DELETE FROM synced_students")
    suspend fun clearSyncedStudents()

    // Attendance History
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAttendanceRecord(record: AttendanceRecord)

    @Query("SELECT * FROM attendance_history WHERE sectionName = :sectionName ORDER BY date DESC")
    fun getAttendanceBySection(sectionName: String): Flow<List<AttendanceRecord>>

    @Query("SELECT * FROM attendance_history WHERE studentCardId = :cardId ORDER BY date DESC")
    fun getAttendanceByStudentFlow(cardId: String): Flow<List<AttendanceRecord>>

    @Query("SELECT * FROM attendance_history WHERE studentCardId = :cardId ORDER BY date DESC")
    suspend fun getAttendanceByStudentList(cardId: String): List<AttendanceRecord>

    @Query("SELECT * FROM attendance_history ORDER BY date DESC")
    fun getAllAttendanceHistory(): Flow<List<AttendanceRecord>>

    @Query("DELETE FROM attendance_history")
    suspend fun clearAttendanceHistory()

    // Unit Progress (Student's own, or tracking local study state)
    @Query("SELECT * FROM unit_progress ORDER BY unitIndex ASC")
    fun getAllProgressFlow(): Flow<List<UnitProgress>>

    @Query("SELECT * FROM unit_progress ORDER BY unitIndex ASC")
    suspend fun getAllProgress(): List<UnitProgress>

    @Query("SELECT * FROM unit_progress WHERE unitIndex = :unitIndex LIMIT 1")
    suspend fun getProgressForUnit(unitIndex: Int): UnitProgress?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertProgress(progress: UnitProgress)

    @Query("UPDATE unit_progress SET isRead = :isRead WHERE unitIndex = :unitIndex")
    suspend fun updateReadProgress(unitIndex: Int, isRead: Boolean)

    @Query("UPDATE unit_progress SET isRead = 0, isChallengeDone = 0, readPercentage = 0, lastScrollPosition = 0 WHERE unitIndex = :unitIndex")
    suspend fun resetReadingProgress(unitIndex: Int)

    @Query("UPDATE unit_progress SET readPercentage = :percentage, lastScrollPosition = :scrollPos WHERE unitIndex = :unitIndex")
    suspend fun updateReadingProgressDetail(unitIndex: Int, percentage: Int, scrollPos: Int)

    @Query("UPDATE unit_progress SET isChallengeDone = :isChallengeDone WHERE unitIndex = :unitIndex")
    suspend fun updateChallengeProgress(unitIndex: Int, isChallengeDone: Boolean)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUnitProgress(progress: UnitProgress)

    @Query("UPDATE unit_progress SET examGrade = :grade, examCompleted = 1 WHERE unitIndex = :unitIndex")
    suspend fun updateExamProgress(unitIndex: Int, grade: Float)

    @Query("UPDATE unit_progress SET examGrade = NULL, examCompleted = 0 WHERE unitIndex = :unitIndex")
    suspend fun resetExamProgress(unitIndex: Int)

    @Query("UPDATE unit_progress SET notebookGrade = NULL, notebookEvaluated = 0, notebookFeedback = '' WHERE unitIndex = :unitIndex")
    suspend fun resetNotebookProgress(unitIndex: Int)

    @Query("DELETE FROM unit_progress")
    suspend fun clearProgress()

    // Inbox (Student)
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertInboxMessage(message: InboxMessage)

    @Query("SELECT * FROM inbox_messages ORDER BY timestamp DESC")
    fun getInboxMessagesFlow(): Flow<List<InboxMessage>>

    @Query("UPDATE inbox_messages SET localUri = :uri WHERE fileId = :fileId")
    suspend fun updateMessageLocalUri(fileId: String, uri: String)

    @Query("DELETE FROM inbox_messages")
    suspend fun clearInbox()
}
