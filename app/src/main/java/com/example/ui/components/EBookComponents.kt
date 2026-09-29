package com.example.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.Question
import com.example.data.model.UnitData
import com.example.ui.theme.*

fun shuffleQuestionOptions(q: Question): Question {
    if (q.options.isEmpty()) return q
    val originalCorrectText = q.options.getOrNull(q.correctIndex) ?: q.options.first()
    val shuffledOptions = q.options.shuffled()
    val newCorrectIndex = shuffledOptions.indexOf(originalCorrectText).coerceAtLeast(0)
    return q.copy(
        options = shuffledOptions,
        correctIndex = newCorrectIndex
    )
}

@Composable
fun ReadingChallengeDialog(
    unit: UnitData,
    onDismiss: () -> Unit,
    onSuccess: () -> Unit
) {
    val challengeQuestions = remember(unit) {
        val pool = mutableListOf<Question>()
        if (unit.challengeQuestions.isNotEmpty()) {
            pool.addAll(unit.challengeQuestions.map { shuffleQuestionOptions(it) })
        }
        if (pool.isEmpty() && unit.examQuestions.isNotEmpty()) {
            pool.addAll(unit.examQuestions.map { shuffleQuestionOptions(it) })
        }
        
        val result = mutableListOf<Question>()
        if (pool.isNotEmpty()) {
            while (result.size < 4) {
                result.addAll(pool.shuffled())
            }
        } else {
            result.add(
                Question(
                    questionText = "Pregunta de repaso de la Unidad ${unit.index}: ¿Comprendiste los conceptos principales explicados en la lección?",
                    options = listOf("Sí, totalmente", "Más o menos", "No lo sé"),
                    correctIndex = 0,
                    explanation = "Has completado la lectura teórica de la unidad."
                )
            )
            while (result.size < 4) {
                result.add(result.first())
            }
        }
        result.take(4)
    }

    var currentQuestionIdx by remember { mutableIntStateOf(0) }
    var selectedOption by remember { mutableIntStateOf(-1) }
    var isAnswerCorrect by remember { mutableStateOf<Boolean?>(null) }
    var correctAnswersCount by remember { mutableIntStateOf(0) }

    val question = challengeQuestions[currentQuestionIdx]

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Psychology, null, tint = AccentAmber)
                Spacer(Modifier.width(8.dp))
                Text("Mini-Reto: Unidad ${unit.index}", fontSize = 16.sp, fontWeight = FontWeight.Bold)
            }
        },
        text = {
            Column {
                Text(
                    text = "Pregunta ${currentQuestionIdx + 1} de ${challengeQuestions.size}",
                    fontSize = 11.sp,
                    color = AccentCyan,
                    fontWeight = FontWeight.Bold
                )
                Spacer(Modifier.height(8.dp))
                Text(
                    text = question.questionText,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    modifier = Modifier.padding(bottom = 12.dp)
                )

                question.options.forEachIndexed { idx, option ->
                    val isSelected = selectedOption == idx
                    val buttonBg = when {
                        isAnswerCorrect != null && isSelected && isAnswerCorrect == true -> StatusSuccess.copy(alpha = 0.3f)
                        isAnswerCorrect != null && isSelected && isAnswerCorrect == false -> StatusError.copy(alpha = 0.3f)
                        isSelected -> AccentCyan.copy(alpha = 0.2f)
                        else -> CosmicDarkSurface
                    }

                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp)
                            .clickable(enabled = isAnswerCorrect == null) {
                                selectedOption = idx
                                val correct = idx == question.correctIndex
                                isAnswerCorrect = correct
                                if (correct) correctAnswersCount++
                            },
                        shape = RoundedCornerShape(8.dp),
                        color = buttonBg,
                        border = BorderStroke(1.dp, if (isSelected) AccentCyan else Color.Gray.copy(alpha = 0.3f))
                    ) {
                        Text(
                            text = option,
                            fontSize = 12.sp,
                            color = Color.White,
                            modifier = Modifier.padding(12.dp)
                        )
                    }
                }

                if (isAnswerCorrect != null) {
                    Spacer(Modifier.height(12.dp))
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(
                            containerColor = if (isAnswerCorrect == true) StatusSuccess.copy(alpha = 0.15f) else StatusError.copy(alpha = 0.15f)
                        ),
                        border = BorderStroke(1.dp, if (isAnswerCorrect == true) StatusSuccess.copy(alpha = 0.5f) else StatusError.copy(alpha = 0.5f)),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Column(Modifier.padding(12.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = if (isAnswerCorrect == true) Icons.Default.CheckCircle else Icons.Default.Warning,
                                    contentDescription = null,
                                    tint = if (isAnswerCorrect == true) StatusSuccess else StatusError,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(Modifier.width(8.dp))
                                Text(
                                    text = if (isAnswerCorrect == true) "¡Respuesta Correcta! (+25 Pts 🔥)" else "Respuesta Incorrecta",
                                    fontWeight = FontWeight.ExtraBold,
                                    color = if (isAnswerCorrect == true) StatusSuccess else StatusError,
                                    fontSize = 12.sp
                                )
                            }
                            if (question.explanation.isNotBlank()) {
                                Spacer(Modifier.height(6.dp))
                                Text(
                                    text = "💡 " + question.explanation,
                                    color = Color.White,
                                    fontSize = 11.sp,
                                    lineHeight = 15.sp
                                )
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            if (isAnswerCorrect != null) {
                Button(
                    onClick = {
                        if (isAnswerCorrect == true) {
                            if (currentQuestionIdx + 1 < challengeQuestions.size) {
                                currentQuestionIdx++
                                selectedOption = -1
                                isAnswerCorrect = null
                            } else {
                                onSuccess()
                            }
                        } else {
                            // Reintentar la pregunta
                            selectedOption = -1
                            isAnswerCorrect = null
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = if (isAnswerCorrect == true) StatusSuccess else AccentAmber)
                ) {
                    Text(
                        text = when {
                            isAnswerCorrect == false -> "REINTENTAR PREGUNTA"
                            currentQuestionIdx + 1 < challengeQuestions.size -> "SIGUIENTE PREGUNTA"
                            else -> "🎉 DESBLOQUEAR VIDEO (+50 Pts 🔥)"
                        },
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp,
                        color = Color.Black
                    )
                }
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("CANCELAR", color = Color.Gray)
            }
        }
    )
}

@Composable
fun VideoMissingAlert(fileName: String) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp)),
        colors = CardDefaults.cardColors(containerColor = StatusError.copy(alpha = 0.1f))
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(Icons.Default.Warning, null, tint = StatusError)
            Spacer(Modifier.width(12.dp))
            Column {
                Text(
                    text = "Video no encontrado",
                    color = StatusError,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp
                )
                Text(
                    text = "El archivo \"$fileName\" no está en la carpeta de materiales.",
                    color = Color.White.copy(alpha = 0.7f),
                    fontSize = 12.sp
                )
            }
        }
    }
}
