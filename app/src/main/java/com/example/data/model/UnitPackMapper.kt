package com.example.data.model

import com.example.data.model.ContentType
import com.example.data.model.LabActivity
import com.example.data.model.Question
import com.example.data.model.UnitData
object UnitPackMapper {

    fun mapToUnitData(pack: UnitPack): UnitData {
        return UnitData(
            index = pack.index,
            title = pack.title,
            description = pack.description,
            summary = pack.description, // Usamos description como summary si no hay campo específico
            audioNarratorText = pack.narrator.text,
            topicText = pack.theory.fallbackText, // Fallback por defecto
            challengeTitle = pack.challenge.title,
            challengeInstructions = pack.challenge.instructions,
            challengeOptions = pack.challenge.options.map { it.text },
            challengeAnswer = pack.challenge.options.firstOrNull { it.isCorrect }?.text ?: "",
            videoFile = pack.extendedContent.find { it.type == "VIDEO" }?.mediaFile,
            examQuestions = pack.exam.questions.map { q ->
                Question(
                    questionText = q.text,
                    options = q.options,
                    correctIndex = q.correctIndex,
                )
            },
            extendedContent = pack.extendedContent.map { ext ->
                ContentSection(
                    type = ContentType.valueOf(ext.type),
                    title = ext.title,
                    content = ext.content,
                    mediaResId = ext.mediaFile
                )
            },
            labs = pack.labs.map { lab ->
                LabActivity(
                    title = lab.title,
                    instructions = lab.instructions,
                    question = lab.question,
                    options = lab.options,
                    correctAnswer = lab.options.getOrNull(lab.correctIndex) ?: ""
                )
            }
        )
    }
}