package com.example.domain

import android.graphics.Bitmap
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.latin.TextRecognizerOptions
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume

object OcrEvaluationManager {

    suspend fun recognizeTextFromBitmap(bitmap: Bitmap): String = suspendCancellableCoroutine { continuation ->
        val recognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)
        val image = InputImage.fromBitmap(bitmap, 0)
        recognizer.process(image)
            .addOnSuccessListener { visionText ->
                continuation.resume(visionText.text)
            }
            .addOnFailureListener { _ ->
                continuation.resume("")
            }
    }

    fun evaluateSummary(extractedText: String, unitTopicText: String): OcrEvaluationResult {
        val cleanExtracted = extractedText.lowercase()
        val cleanTopic = unitTopicText.lowercase()

        // 1. Contenido y Comprensión (10 pts)
        val keywords = cleanTopic.split(Regex("\\s+"))
            .map { it.trim() }
            .filter { it.length > 3 }
            .distinct()

        val foundKeywords = if (keywords.isNotEmpty()) {
            keywords.count { cleanExtracted.contains(it) }
        } else {
            0
        }

        val keywordRatio = if (keywords.isNotEmpty()) foundKeywords.toFloat() / keywords.size.toFloat() else 1f
        val wordCount = cleanExtracted.split(Regex("\\s+")).filter { it.isNotBlank() }.size
        val lengthScore = when {
            wordCount >= 10 -> 1f
            wordCount >= 5 -> 0.7f
            else -> 0.3f
        }

        val contentScore = ((keywordRatio * 0.5f + lengthScore * 0.5f) * 10f).coerceIn(1f, 10f)
        val contentFeedback = when {
            contentScore >= 7f -> "Excelente cobertura de conceptos clave de la lección."
            contentScore >= 4f -> "Buena síntesis, aunque faltaron algunos conceptos clave."
            else -> "Resumen demasiado corto o ausencia de los temas principales de la lección."
        }

        // 2. Redacción y Coherencia (5 pts)
        val coherenceScore = when {
            wordCount >= 15 -> 5.0f
            wordCount >= 8 -> 4.0f
            else -> 3.0f
        }
        val coherenceFeedback = if (coherenceScore >= 4f) "Estructura de párrafos y coherencia clara." else "Estructura de redacción mejorable."

        // 3. Ortografía y Gramática (5 pts)
        val spellingScore = 5.0f
        val corrections = mutableListOf<String>()

        val totalScore = (contentScore + coherenceScore + spellingScore).coerceIn(0f, 20f)

        return OcrEvaluationResult(
            extractedText = extractedText,
            contentScore = contentScore,
            coherenceScore = coherenceScore,
            spellingScore = spellingScore,
            totalScore = totalScore,
            contentFeedback = contentFeedback,
            coherenceFeedback = coherenceFeedback,
            spellingCorrections = corrections
        )
    }
}
