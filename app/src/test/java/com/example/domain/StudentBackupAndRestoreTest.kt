package com.example.domain

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.example.data.AppDatabase
import com.example.data.UnitProgress
import com.example.data.UserProfile
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertNotNull
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class StudentBackupAndRestoreTest {

    @Test
    fun testExportBackupCreatesJsonFile() = runTest {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val db = Room.inMemoryDatabaseBuilder(context, AppDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        val dao = db.classroomDao()

        dao.insertProfile(
            UserProfile(
                role = "student",
                firstName = "Carlos",
                lastName = "Mendoza",
                email = "carlos@test.com",
                cardId = "V-88888",
                activeSection = "Sección A"
            )
        )
        dao.insertProgress(
            UnitProgress(
                unitIndex = 1,
                isRead = true,
                examGrade = 19.5f,
                examCompleted = true
            )
        )

        val backupFile = BackupManager.exportBackup(context, dao)
        assertNotNull(backupFile)
        
        db.close()
    }
}
