package com.example.ui.screens

import android.net.Uri
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.OndemandVideo
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.*
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
import com.example.ui.theme.CosmicDarkSurface
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
    onTakeExam: () -> Unit,
    onMarkRead: (Int) -> Unit = {},
    onUpdateReadingProgress: (unitIndex: Int, percentage: Int, scrollPos: Int) -> Unit = { _, _, _ -> }
) {
    val scrollState = rememberScrollState()
    val maxScroll = scrollState.maxValue
    val currentScroll = scrollState.value
    val scrollProgress = if (maxScroll > 0) (currentScroll.toFloat() / maxScroll.toFloat()).coerceIn(0f, 1f) else 1f
    val isScrollAtBottom = scrollProgress >= 0.88f || maxScroll == 0

    val examActive = isLocked && enabledExams.split(",").filter { it.isNotEmpty() }.contains(unit.index.toString())
    val unitProgress = studyProgress.find { it.unitIndex == unit.index }
    val hasCompletedExam = unitProgress?.examCompleted == true || unitProgress?.examGrade != null
    var isReadLocal by remember(unit.index, unitProgress?.isRead) { mutableStateOf(unitProgress?.isRead == true) }

    // Control dinámico del tamaño de fuente (Lector Inteligente)
    var fontSizeSp by remember { mutableIntStateOf(16) }

    // RESTAURAR POSICIÓN DE DESPLAZAMIENTO (Scroll Memory)
    LaunchedEffect(unit.index) {
        val initialScroll = unitProgress?.lastScrollPosition ?: 0
        if (initialScroll > 0) {
            scrollState.scrollTo(initialScroll)
        }
    }

    // ACTUALIZAR PROGRESO CONTINUO DE LECTURA
    val currentPct = if (isReadLocal) 100 else (scrollProgress * 100).toInt()
    LaunchedEffect(currentScroll, currentPct) {
        if (currentScroll > 0 || currentPct > 0) {
            onUpdateReadingProgress(unit.index, currentPct, currentScroll)
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 8.dp)
            .verticalScroll(scrollState)
    ) {
        // BARRA SUPERIOR DE PROGRESO Y CONTROLES DE LECTURA
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp),
            colors = CardDefaults.cardColors(containerColor = CosmicDarkSurface),
            shape = RoundedCornerShape(16.dp),
            border = BorderStroke(1.dp, AccentCyan.copy(alpha = 0.2f))
        ) {
            Column(Modifier.padding(14.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = if (isReadLocal) Icons.Default.CheckCircle else Icons.Default.MenuBook,
                            contentDescription = null,
                            tint = if (isReadLocal) StatusSuccess else AccentCyan,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(Modifier.width(8.dp))
                        Text(
                            text = if (isReadLocal) "Lectura Completada" else "Avance de Lectura",
                            fontWeight = FontWeight.Bold,
                            color = Color.White,
                            fontSize = 12.sp
                        )
                    }

                    // Controles de tamaño de letra (A- / A+)
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Surface(
                            onClick = { if (fontSizeSp > 13) fontSizeSp-- },
                            color = Color.DarkGray.copy(alpha = 0.4f),
                            shape = CircleShape,
                            modifier = Modifier.size(28.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(Icons.Default.Remove, "Reducir letra", tint = Color.White, modifier = Modifier.size(14.dp))
                            }
                        }
                        Text(
                            text = "${fontSizeSp}sp",
                            fontSize = 10.sp,
                            color = Color.Gray,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 6.dp)
                        )
                        Surface(
                            onClick = { if (fontSizeSp < 22) fontSizeSp++ },
                            color = Color.DarkGray.copy(alpha = 0.4f),
                            shape = CircleShape,
                            modifier = Modifier.size(28.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(Icons.Default.Add, "Aumentar letra", tint = Color.White, modifier = Modifier.size(14.dp))
                            }
                        }
                    }
                }

                Spacer(Modifier.height(8.dp))

                LinearProgressIndicator(
                    progress = { if (isReadLocal) 1f else scrollProgress },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(6.dp)
                        .clip(RoundedCornerShape(3.dp)),
                    color = if (isReadLocal) StatusSuccess else AccentCyan,
                    trackColor = Color.DarkGray.copy(alpha = 0.5f)
                )
            }
        }

        // TARJETA E-BOOK DE LECTURA TEÓRICA
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 20.dp),
            colors = CardDefaults.cardColors(containerColor = CosmicDarkSurface),
            shape = RoundedCornerShape(20.dp),
            border = BorderStroke(1.dp, Color.Gray.copy(alpha = 0.15f))
        ) {
            Column(Modifier.padding(18.dp)) {
                // Header de la Unidad en el E-Book
                Row(
                    modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "UNIDAD ${unit.index}",
                        fontWeight = FontWeight.ExtraBold,
                        color = AccentCyan,
                        fontSize = 11.sp,
                        letterSpacing = 1.sp
                    )

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Timer, null, tint = Color.Gray, modifier = Modifier.size(12.dp))
                        Spacer(Modifier.width(4.dp))
                        Text(
                            text = "~10-15 min",
                            fontSize = 10.sp,
                            color = Color.Gray
                        )
                    }
                }

                Text(
                    text = unit.title,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    fontSize = 18.sp,
                    lineHeight = 22.sp,
                    modifier = Modifier.padding(bottom = 16.dp)
                )

                HorizontalDivider(Modifier.padding(bottom = 16.dp), color = Color.Gray.copy(alpha = 0.2f))

                // Parseador Tipográfico del Contenido Teórico
                val lines = unit.topicText.split("\n")
                lines.forEach { line ->
                    val trimmed = line.trim()
                    when {
                        trimmed.startsWith("###") || trimmed.startsWith("##") || trimmed.startsWith("#") -> {
                            val headerText = trimmed.removePrefix("#").removePrefix("#").removePrefix("#").trim()
                            Text(
                                text = headerText,
                                fontWeight = FontWeight.ExtraBold,
                                color = AccentCyan,
                                fontSize = (fontSizeSp + 2).sp,
                                lineHeight = ((fontSizeSp + 2) * 1.3f).sp,
                                modifier = Modifier.padding(top = 16.dp, bottom = 8.dp)
                            )
                        }
                        trimmed.startsWith("-") || trimmed.startsWith("*") -> {
                            val bulletText = trimmed.removePrefix("-").removePrefix("*").trim()
                            Row(modifier = Modifier.padding(start = 8.dp, bottom = 6.dp)) {
                                Text("•", color = AccentCyan, fontWeight = FontWeight.Bold, fontSize = fontSizeSp.sp)
                                Spacer(Modifier.width(8.dp))
                                Text(
                                    text = bulletText,
                                    color = Color.White.copy(alpha = 0.9f),
                                    fontSize = fontSizeSp.sp,
                                    lineHeight = (fontSizeSp * 1.45f).sp
                                )
                            }
                        }
                        trimmed.isNotEmpty() -> {
                            Text(
                                text = trimmed,
                                color = Color.White.copy(alpha = 0.9f),
                                fontSize = fontSizeSp.sp,
                                lineHeight = (fontSizeSp * 1.45f).sp,
                                modifier = Modifier.padding(bottom = 12.dp)
                            )
                        }
                        else -> {
                            Spacer(Modifier.height(6.dp))
                        }
                    }
                }
            }
        }

        // Botón de Confirmación de Lectura (si aún no está leída)
        if (!isReadLocal) {
            Button(
                onClick = {
                    isReadLocal = true
                    onMarkRead(unit.index)
                },
                enabled = isScrollAtBottom,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(54.dp)
                    .padding(bottom = 20.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = StatusSuccess,
                    disabledContainerColor = Color.DarkGray.copy(alpha = 0.5f)
                ),
                shape = RoundedCornerShape(14.dp)
            ) {
                Icon(Icons.Default.Check, null)
                Spacer(Modifier.width(8.dp))
                Text(
                    text = if (isScrollAtBottom) "📖 COMPLETAR LECTURA Y DESBLOQUEAR VIDEO" else "📜 DESPLÁZATE HASTA EL FINAL PARA COMPLETAR",
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 11.sp
                )
            }
        }

        // 2. VIDEO EXPLICATIVO (CANDADO ESTRICTO SI NO HA LEÍDO)
        if (videoUri != null || !unit.videoFile.isNullOrEmpty()) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    Icons.Default.OndemandVideo,
                    contentDescription = null,
                    tint = if (isReadLocal) AccentCyan else Color.Gray,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(Modifier.width(8.dp))
                Text(
                    text = "Video Explicativo de la Clase",
                    color = if (isReadLocal) AccentCyan else Color.Gray,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp
                )
            }

            if (!isReadLocal) {
                // CANDADO ESTRICTO DE VIDEO
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 24.dp),
                    colors = CardDefaults.cardColors(containerColor = CosmicDarkSurface),
                    border = BorderStroke(1.dp, AccentAmber.copy(alpha = 0.4f)),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(20.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(48.dp)
                                .clip(CircleShape)
                                .background(AccentAmber.copy(alpha = 0.2f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.Lock, null, tint = AccentAmber, modifier = Modifier.size(24.dp))
                        }
                        Spacer(Modifier.width(16.dp))
                        Column(Modifier.weight(1f)) {
                            Text(
                                text = "🔒 Video Bloqueado",
                                fontWeight = FontWeight.Bold,
                                color = AccentAmber,
                                fontSize = 14.sp
                            )
                            Spacer(Modifier.height(4.dp))
                            Text(
                                text = "Lee todo el contenido teórico de la clase y presiona 'Completar Lectura' para desbloquear el video.",
                                color = Color.LightGray,
                                fontSize = 11.sp,
                                lineHeight = 15.sp
                            )
                        }
                    }
                }
            } else if (videoUri != null) {
                VideoPlayer(videoUri = videoUri)
                Spacer(Modifier.height(24.dp))
            } else {
                VideoMissingAlert(fileName = unit.videoFile ?: "")
                Spacer(Modifier.height(24.dp))
            }
        }

        // 3. EVALUACIÓN (EXAMEN / RECUPERATIVO)
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
