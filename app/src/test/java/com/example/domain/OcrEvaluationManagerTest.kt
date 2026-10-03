package com.example.domain

import org.junit.Assert.assertTrue
import org.junit.Test

class OcrEvaluationManagerTest {

    @Test
    fun `test evaluateSummary with good content and keywords`() {
        val topicText = "La arquitectura de Von Neumann consta de unidad aritmetico logica, unidad de control y memoria principal."
        val extractedText = "En la arquitectura de Von Neumann tenemos la unidad aritmetico logica, la unidad de control y la memoria principal del computador."

        val result = OcrEvaluationManager.evaluateSummary(extractedText, topicText)

        assertTrue(result.contentScore > 5f)
        assertTrue(result.coherenceScore >= 3f)
        assertTrue(result.totalScore > 10f)
    }

    @Test
    fun `test evaluateSummary with poor content`() {
        val topicText = "Arquitectura avanzada de microprocesadores y buses de datos."
        val extractedText = "Hola."

        val result = OcrEvaluationManager.evaluateSummary(extractedText, topicText)

        assertTrue(result.contentScore < 5f)
        assertTrue(result.totalScore < 15f)
    }
}
