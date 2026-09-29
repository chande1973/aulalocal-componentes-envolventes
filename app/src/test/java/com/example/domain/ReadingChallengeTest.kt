package com.example.domain

import com.example.data.model.Question
import com.example.ui.components.shuffleQuestionOptions
import org.junit.Assert.assertEquals
import org.junit.Test

class ReadingChallengeTest {

    @Test
    fun testShuffleQuestionOptionsPreservesCorrectOptionText() {
        val originalQuestion = Question(
            questionText = "¿Cuál es el componente de almacenamiento principal?",
            options = listOf("RAM", "Disco Duro / SSD", "CPU", "GPU"),
            correctIndex = 1, // "Disco Duro / SSD"
            explanation = "El Disco Duro o SSD almacena la información permanentemente."
        )

        val originalCorrectText = originalQuestion.options[originalQuestion.correctIndex]

        val shuffled = shuffleQuestionOptions(originalQuestion)

        // Verificar que las opciones tengan el mismo tamaño
        assertEquals(4, shuffled.options.size)

        // Verificar que el nuevo correctIndex apunte al texto correcto original
        val newCorrectText = shuffled.options[shuffled.correctIndex]
        assertEquals(originalCorrectText, newCorrectText)
    }
}
