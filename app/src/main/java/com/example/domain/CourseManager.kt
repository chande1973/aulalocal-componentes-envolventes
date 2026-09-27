package com.example.domain

import android.net.Uri
import com.example.data.CoursePack
import com.example.data.CourseRepository
import com.example.data.model.LoadResult
import com.example.data.model.UnitData
import com.example.data.model.UnitPackMapper
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

object CourseManager {

    /**
     * Valida y prepara un Course Pack para su uso en la app.
     */
    suspend fun processCoursePack(
        repository: CourseRepository,
        folderUri: Uri,
        onProgress: ((step: String, current: Int, total: Int, currentFile: String) -> Unit)? = null
    ): LoadResult<CoursePack> {
        return withContext(Dispatchers.IO) {
            repository.loadCoursePack(folderUri, onProgress)
        }
    }

    /**
     * Mapea las unidades crudas del pack a objetos UnitData para la UI.
     */
    fun mapPackToUnits(pack: CoursePack): List<UnitData> {
        return pack.units.values
            .map { UnitPackMapper.mapToUnitData(it) }
            .sortedBy { it.index }
            .toList()
    }
}
