package com.example.domain

import android.content.Context
import android.net.Uri
import com.example.data.ClassroomDao
import com.example.util.ZipUtils
import java.io.File

object CourseInstallerManager {

    /**
     * Descomprime e instala una actualización de curso en ZIP.
     */
    suspend fun installCourseZip(
        context: Context,
        dao: ClassroomDao,
        zipFile: File,
        onProgress: (currentFile: String, count: Int) -> Unit
    ): ZipUtils.UnzipResult {
        val internalCourseDir = File(context.filesDir, "course_data")
        val result = ZipUtils.unzip(zipFile, internalCourseDir, onProgress)
        
        if (result is ZipUtils.UnzipResult.Success) {
            val profile = dao.getUserProfile()
            if (profile != null) {
                val uri = Uri.fromFile(internalCourseDir)
                dao.insertProfile(profile.copy(materialsFolderUri = uri.toString()))
            }
        }
        
        if (zipFile.exists()) {
            zipFile.delete()
        }
        
        return result
    }
}
