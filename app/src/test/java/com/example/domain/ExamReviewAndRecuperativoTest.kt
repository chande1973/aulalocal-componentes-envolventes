package com.example.domain

import com.example.data.UnitProgress
import com.example.data.model.Question
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class ExamReviewAndRecuperativoTest {

    @Test
    fun testUnitProgressRecuperativoLimits() {
        val initialProgress = UnitProgress(
            unitIndex = 1,
            examCompleted = true,
            examGrade = 14f,
            isRecuperativoUsed = false,
            recuperativoRequested = false
        )

        assertFalse(initialProgress.isRecuperativoUsed)

        val updatedProgress = initialProgress.copy(
            isRecuperativoUsed = true,
            recuperativoRequested = false
        )

        assertTrue(updatedProgress.isRecuperativoUsed)
    }

    @Test
    fun testQuestionExplanationMapping() {
        val question = Question(
            questionText = "¿Cuál es el protocolo de red local?",
            options = listOf("TCP/UDP", "HTTP público", "FTP"),
            correctIndex = 0,
            explanation = "Se usa TCP/UDP sobre red local sin necesidad de internet."
        )

        assertEquals("Se usa TCP/UDP sobre red local sin necesidad de internet.", question.explanation)
    }
}
