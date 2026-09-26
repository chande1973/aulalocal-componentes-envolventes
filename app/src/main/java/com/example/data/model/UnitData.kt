package com.example.data.model

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

/**
 * Modelo interno de UI para representar una unidad en la vista (MainViewModel, ClassroomUi).
 * Se obtiene mapeando desde UnitPack (modelo externo JSON) vía UnitPackMapper.
 */
@JsonClass(generateAdapter = true)
data class UnitData(
    val index: Int,
    val title: String,
    val description: String,
    val summary: String,
    val audioNarratorText: String,
    val topicText: String,
    val challengeTitle: String,
    val challengeInstructions: String,
    val challengeOptions: List<String>,
    val challengeAnswer: String,
    val videoFile: String? = null,
    val challengeQuestions: List<Question> = emptyList(),
    val examQuestions: List<Question> = emptyList(),
    val extendedContent: List<ContentSection> = emptyList(),
    val labs: List<LabActivity> = emptyList()
)

@JsonClass(generateAdapter = true)
data class Question(
    val questionText: String,
    val options: List<String>,
    val correctIndex: Int,
    val explanation: String = ""
)

@JsonClass(generateAdapter = true)
data class ContentSection(
    val type: ContentType,
    val title: String? = null,
    val content: String,
    val mediaResId: String? = null
)

enum class ContentType { TEXT, VIDEO, IMAGE, TIP, PDF, LINK }

@JsonClass(generateAdapter = true)
data class LabActivity(
    val title: String,
    val instructions: String,
    val question: String,
    val options: List<String>,
    val correctAnswer: String
)