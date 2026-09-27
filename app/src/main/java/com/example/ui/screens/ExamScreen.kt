package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.UnitData
import com.example.ui.theme.*
import com.example.ui.viewmodels.StudentViewModel

@Composable
fun ExamScreen(
    unit: UnitData,
    studentViewModel: StudentViewModel,
    onDismiss: () -> Unit
) {
    // El examen usa todas las preguntas formales de la unidad (separadas del mini-reto)
    val questions = remember(unit) {
        unit.examQuestions
    }
    var currentQuestionIndex by remember { mutableIntStateOf(0) }
    var selectedOptionIndex by remember { mutableIntStateOf(-1) }
    var score by remember { mutableIntStateOf(0) }
    var isExamFinished by remember { mutableStateOf(false) }

    // Surface a pantalla completa para evitar solapamiento con el Scaffold padre
    Surface(
        modifier = Modifier.fillMaxSize(),
        color = CosmicDarkBg
    ) {
        if (isExamFinished) {
            val finalGrade = (score.toFloat() / questions.size) * 20f
            ExamResultView(
                grade = finalGrade,
                correctAnswers = score,
                totalQuestions = questions.size,
                onClose = {
                    studentViewModel.saveExamGrade(unit.index, finalGrade)
                    onDismiss()
                }
            )
        } else if (questions.isEmpty()) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("Esta unidad no tiene evaluación disponible.", color = Color.White)
                    Spacer(Modifier.height(16.dp))
                    Button(onClick = onDismiss) { Text("REGRESAR") }
                }
            }
        } else {
            val currentQuestion = questions[currentQuestionIndex]
            
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(24.dp)
                    .verticalScroll(rememberScrollState()),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Cabecera Minimalista
                Row(
                    Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("Unidad ${unit.index}", color = AccentCyan, fontWeight = FontWeight.Bold)
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, "Abandonar", tint = Color.Gray)
                    }
                }

                Spacer(Modifier.height(16.dp))

                // Progreso
                LinearProgressIndicator(
                    progress = { (currentQuestionIndex + 1).toFloat() / questions.size },
                    modifier = Modifier.fillMaxWidth().height(8.dp).clip(RoundedCornerShape(4.dp)),
                    color = AccentCyan,
                    trackColor = Color.DarkGray
                )
                Spacer(Modifier.height(8.dp))
                Text(
                    "Pregunta ${currentQuestionIndex + 1} de ${questions.size}",
                    color = Color.Gray,
                    fontSize = 12.sp
                )
                
                Spacer(Modifier.height(32.dp))
                
                // Pregunta
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = CosmicDarkSurface),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Text(
                        text = currentQuestion.questionText,
                        modifier = Modifier.padding(24.dp),
                        style = MaterialTheme.typography.titleLarge,
                        color = Color.White,
                        textAlign = TextAlign.Center,
                        lineHeight = 28.sp
                    )
                }
                
                Spacer(Modifier.height(32.dp))
                
                // Opciones
                currentQuestion.options.forEachIndexed { index, option ->
                    val isSelected = selectedOptionIndex == index
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp)
                            .clickable { selectedOptionIndex = index },
                        colors = CardDefaults.cardColors(
                            containerColor = if (isSelected) AccentCyan.copy(alpha = 0.15f) else CosmicDarkSurface
                        ),
                        border = if (isSelected) BorderStroke(1.dp, AccentCyan) else null,
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Row(
                            Modifier.padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(28.dp)
                                    .clip(CircleShape)
                                    .background(if (isSelected) AccentCyan else Color.DarkGray.copy(alpha = 0.5f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = (index + 65).toChar().toString(),
                                    color = if (isSelected) Color.Black else Color.White,
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.ExtraBold
                                )
                            }
                            Spacer(Modifier.width(16.dp))
                            Text(text = option, color = Color.White, fontSize = 15.sp)
                        }
                    }
                }
                
                Spacer(Modifier.height(48.dp))
                
                Button(
                    onClick = {
                        if (selectedOptionIndex == currentQuestion.correctIndex) {
                            score++
                        }
                        
                        if (currentQuestionIndex < questions.size - 1) {
                            currentQuestionIndex++
                            selectedOptionIndex = -1
                        } else {
                            isExamFinished = true
                        }
                    },
                    modifier = Modifier.fillMaxWidth().height(56.dp),
                    enabled = selectedOptionIndex != -1,
                    colors = ButtonDefaults.buttonColors(containerColor = AccentTeal),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Text(
                        if (currentQuestionIndex == questions.size - 1) "FINALIZAR EXAMEN" else "SIGUIENTE PREGUNTA",
                        fontWeight = FontWeight.ExtraBold,
                        letterSpacing = 1.sp
                    )
                }
                Spacer(Modifier.height(24.dp))
            }
        }
    }
}

@Composable
fun ExamResultView(
    grade: Float,
    correctAnswers: Int,
    totalQuestions: Int,
    onClose: () -> Unit
) {
    val isPassed = grade >= 12f
    val color = if (isPassed) Color.Green else Color.Red

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(CosmicDarkBg)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(32.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = if (isPassed) Icons.Default.CheckCircle else Icons.Default.Warning,
                contentDescription = null,
                tint = color,
                modifier = Modifier.size(100.dp)
            )
            Spacer(Modifier.height(24.dp))
            Text(
                text = if (isPassed) "¡FELICIDADES!" else "CONTINÚA ESTUDIANDO",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.ExtraBold,
                color = Color.White,
                textAlign = TextAlign.Center
            )
            Text(
                text = if (isPassed) "Has superado esta evaluación" else "No has alcanzado la nota mínima de 12",
                color = Color.Gray,
                textAlign = TextAlign.Center
            )
            
            Spacer(Modifier.height(40.dp))
            
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = CosmicDarkSurface),
                shape = RoundedCornerShape(24.dp),
                border = BorderStroke(1.dp, color.copy(alpha = 0.3f))
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text("CALIFICACIÓN FINAL", fontSize = 12.sp, color = Color.Gray, fontWeight = FontWeight.Bold)
                    Text(
                        text = "%.2f".format(grade),
                        fontSize = 56.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = color
                    )
                    Text("/ 20.00", fontSize = 16.sp, color = Color.Gray)
                    
                    Spacer(Modifier.height(24.dp)) // ESPACIO AUMENTADO
                    
                    Surface(
                        color = Color.White.copy(alpha = 0.05f),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text(
                            "Aciertos: $correctAnswers de $totalQuestions",
                            color = Color.White,
                            fontSize = 14.sp,
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                        )
                    }
                }
            }
            
            Spacer(Modifier.height(48.dp))
            
            Button(
                onClick = onClose,
                modifier = Modifier.fillMaxWidth().height(56.dp),
                colors = ButtonDefaults.buttonColors(containerColor = color),
                shape = RoundedCornerShape(16.dp)
            ) {
                Text("REGRESAR A LA UNIDAD", fontWeight = FontWeight.ExtraBold)
            }
            Spacer(Modifier.height(24.dp))
        }
    }
}
