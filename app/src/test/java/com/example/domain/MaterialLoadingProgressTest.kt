package com.example.domain

import com.example.ui.MainViewModel
import com.example.ui.viewmodels.StudentViewModel
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class MaterialLoadingProgressTest {

    @Test
    fun testUpdateStateProcessingDefaultValues() {
        val processingState = StudentViewModel.UpdateState.Processing()
        assertEquals("Procesando materiales...", processingState.message)
        assertEquals("Instalando contenido", processingState.step)
        assertEquals(0, processingState.current)
        assertEquals(0, processingState.total)
        assertEquals("", processingState.currentFile)
    }

    @Test
    fun testUpdateStateProcessingCustomValues() {
        val processingState = StudentViewModel.UpdateState.Processing(
            message = "Cargando curso...",
            step = "Unidad 2",
            current = 2,
            total = 5,
            currentFile = "unit_2.json"
        )
        assertEquals("Cargando curso...", processingState.message)
        assertEquals("Unidad 2", processingState.step)
        assertEquals(2, processingState.current)
        assertEquals(5, processingState.total)
        assertEquals("unit_2.json", processingState.currentFile)

        val percentage = (processingState.current.toFloat() / processingState.total * 100).toInt()
        assertEquals(40, percentage)
    }

    @Test
    fun testMaterialLoadingProgressModel() {
        val progress = MainViewModel.MaterialLoadingProgress(
            step = "Procesando unidad 1 de 3",
            current = 1,
            total = 3,
            currentFile = "course_index.json",
            isLoading = true
        )
        assertTrue(progress.isLoading)
        assertEquals("Procesando unidad 1 de 3", progress.step)
        assertEquals(1, progress.current)
        assertEquals(3, progress.total)
        assertEquals("course_index.json", progress.currentFile)
    }
}
