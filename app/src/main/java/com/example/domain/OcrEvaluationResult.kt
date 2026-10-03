package com.example.domain

data class OcrEvaluationResult(
    val extractedText: String,
    val contentScore: Float, // Max 10.0
    val coherenceScore: Float, // Max 5.0
    val spellingScore: Float, // Max 5.0
    val totalScore: Float, // Max 20.0
    val contentFeedback: String,
    val coherenceFeedback: String,
    val spellingCorrections: List<String>
)
