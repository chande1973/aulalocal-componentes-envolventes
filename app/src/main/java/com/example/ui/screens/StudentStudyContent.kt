package com.example.ui.screens

import android.net.Uri
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.OndemandVideo
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.data.UnitProgress
import com.example.data.model.UnitData
import com.example.ui.components.VideoPlayer
import com.example.ui.theme.AccentAmber
import com.example.ui.theme.AccentCyan
import com.example.ui.theme.StatusError
import com.example.ui.theme.StatusSuccess
import java.util.Locale

@Composable
fun StudentStudyContent(
    unit: UnitData,
    videoUri: Uri?,
    isLocked: Boolean,
    enabledExams: String,
    studyProgress: List<UnitProgress> = emptyList(),
    onTakeExam: () -> Unit
) {
    val examActive = isLocked && enabledExams.split(",").filter { it.isNotEmpty() }.contains(unit.index.toString())
    val unitProgress = studyProgress.find { it.unitIndex == unit.index }
    val hasCompletedExam = unitProgress?.examCompleted == true || unitProgress?.examGrade != null

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 8.dp)
            .verticalScroll(rememberScrollState())
    ) {
        // 1. PRIMERO: Lectura Teórica del Tema
        Text(
            text = unit.topicText,
            color = Color.White,
            lineHeight = 22.sp,
            fontSize = 16.sp,
            modifier = Modifier.padding(bottom = 24.dp)
        )

        // 2. SEGUNDO: Video Explicativo de la Clase (después de la lectura)
        if (videoUri != null || !unit.videoFile.isNullOrEmpty()) {
            Row(
                modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    Icons.Default.OndemandVideo,
                    contentDescription = null,
                    tint = AccentCyan,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(Modifier.width(8.dp))
                Text(
                    text = "Video Explicativo de la Clase",
                    color = AccentCyan,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp
                )
            }

            if (videoUri != null) {
                VideoPlayer(videoUri = videoUri)
                Spacer(Modifier.height(24.dp))
            } else {
                VideoMissingAlert(fileName = unit.videoFile ?: "")
                Spacer(Modifier.height(24.dp))
            }
        }

        // 3. TERCERO: Evaluación (Examen / Estado Completado)
        if (hasCompletedExam) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 24.dp),
                colors = CardDefaults.cardColors(containerColor = StatusSuccess.copy(alpha = 0.1f)),
                border = BorderStroke(1.dp, StatusSuccess.copy(alpha = 0.5f)),
                shape = RoundedCornerShape(12.dp)
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        Icons.Default.CheckCircle,
                        contentDescription = null, 
                        tint = StatusSuccess,
                        modifier = Modifier.size(28.dp)
                    )
                    Spacer(Modifier.width(12.dp))
                    Column {
                        Text(
                            text = "Examen Completado",
                            fontWeight = FontWeight.Bold,
                            color = StatusSuccess,
                            fontSize = 14.sp
                        )
                        val grade = unitProgress?.examGrade ?: 0f
                        Text(
                            text = "Tu Calificación: ${"%.2f".format(Locale.US, grade)} / 20",
                            color = Color.White,
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 13.sp
                        )
                        Text(
                            text = "Si tu profesor te habilita un recuperativo, podrás volver a presentar.",
                            color = Color.Gray,
                            fontSize = 10.sp
                        )
                    }
                }
            }
        } else if (isLocked && examActive) {
            Button(
                onClick = onTakeExam,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                colors = ButtonDefaults.buttonColors(containerColor = AccentAmber),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text(
                    text = stringResource(R.string.btn_take_exam),
                    color = Color.Black,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp
                )
            }
            Spacer(Modifier.height(24.dp))
        }
    }
}

@Composable
private fun VideoMissingAlert(fileName: String) {
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
