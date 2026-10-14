package com.example.ui.screens

import android.net.Uri
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.material.icons.filled.Psychology
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
import com.example.data.model.Question
import com.example.data.model.UnitData
import com.example.ui.components.CosmicCard
import com.example.ui.components.PrimaryActionButton
import com.example.ui.components.ReadingProgressTopBar
import com.example.ui.components.ReadingChallengeDialog
import com.example.ui.components.VideoMissingAlert
import com.example.ui.components.VideoPlayer
import com.example.ui.theme.AccentAmber
import com.example.ui.theme.AccentCyan
import com.example.ui.theme.CosmicDarkSurface
import com.example.ui.theme.StatusError
import com.example.ui.theme.StatusSuccess
import java.util.Locale
import kotlinx.coroutines.launch

@Composable
fun StudentStudyContent(
    unit: UnitData,
    videoUri: Uri?,
    isLocked: Boolean,
    enabledExams: String,
    enabledNotebookUnits: String = "",
    studyProgress: List<UnitProgress> = emptyList(),
    onTakeExam: () -> Unit,
    onMarkRead: (Int) -> Unit = {},
    onMarkChallengeDone: (Int) -> Unit = {},
    onUpdateReadingProgress: (unitIndex: Int, percentage: Int, scrollPos: Int) -> Unit = { _, _, _ -> },
    onRequestRecuperativo: () -> Unit = {},
    onRequestNotebookRecuperativo: () -> Unit = {},
    onEvaluateNotebook: suspend (android.graphics.Bitmap) -> com.example.domain.OcrEvaluationResult = { com.example.domain.OcrEvaluationResult("", 0f, 0f, 0f, 0f, "", "", emptyList()) },
    onSaveNotebookGrade: (Int, com.example.domain.OcrEvaluationResult) -> Unit = { _, _ -> }
) {
    val scrollState = rememberScrollState()
    val maxScroll = scrollState.maxValue
    val currentScroll = scrollState.value
    val scope = rememberCoroutineScope()

    var isScanningNotebook by remember { mutableStateOf(false) }
    var ocrResultDialog by remember { mutableStateOf<com.example.domain.OcrEvaluationResult?>(null) }
    val cameraLauncher = androidx.activity.compose.rememberLauncherForActivityResult(androidx.activity.result.contract.ActivityResultContracts.TakePicturePreview()) { bitmap ->
        if (bitmap != null) {
            isScanningNotebook = true
            scope.launch {
                val result = onEvaluateNotebook(bitmap)
                isScanningNotebook = false
                ocrResultDialog = result
            }
        }
    }

    val examActive = isLocked && enabledExams.split(",").filter { it.isNotEmpty() }.contains(unit.index.toString())
    val unitProgress = studyProgress.find { it.unitIndex == unit.index }
    val hasCompletedExam = unitProgress?.examCompleted == true || unitProgress?.examGrade != null
    var isReadLocal by remember(unit.index, unitProgress?.isRead, unitProgress?.isChallengeDone) { 
        mutableStateOf(unitProgress?.isRead == true && unitProgress?.isChallengeDone == true) 
    }

    var showChallengeDialog by remember { mutableStateOf(false) }

    // Calcular progreso real
    val scrollProgress = when {
        isReadLocal -> 1f
        maxScroll > 0 -> (currentScroll.toFloat() / maxScroll.toFloat()).coerceIn(0f, 1f)
        else -> 0f
    }
    // Habilitar botón de completado si ya está leída, si el texto es corto (maxScroll == 0) o si se desplazó al final (umbral tolerante 75%)
    val isScrollAtBottom = isReadLocal || (maxScroll == 0) || (maxScroll > 0 && scrollProgress >= 0.75f)

    // Control dinámico del tamaño de fuente (Lector Inteligente)
    var fontSizeSp by remember { mutableIntStateOf(16) }

    // RESTAURAR POSICIÓN DE DESPLAZAMIENTO (Scroll Memory)
    LaunchedEffect(unit.index) {
        val initialScroll = unitProgress?.lastScrollPosition ?: 0
        if (initialScroll > 0) {
            scrollState.scrollTo(initialScroll)
        }
    }

    // ACTUALIZAR PROGRESO CONTINUO DE LECTURA (Solo con desplazamiento o completado real)
    val currentPct = if (isReadLocal) 100 else (scrollProgress * 100).toInt()
    LaunchedEffect(currentScroll, currentPct, isReadLocal) {
        if (currentScroll > 0 || isReadLocal) {
            onUpdateReadingProgress(unit.index, currentPct, currentScroll)
        }
    }

    if (showChallengeDialog) {
        ReadingChallengeDialog(
            unit = unit,
            onDismiss = { showChallengeDialog = false },
            onSuccess = {
                showChallengeDialog = false
                isReadLocal = true
                onMarkRead(unit.index)
                onMarkChallengeDone(unit.index)
            }
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 8.dp)
    ) {
        // BARRA SUPERIOR DE PROGRESO Y CONTROLES DE LECTURA (FIJA / STICKY)
        ReadingProgressTopBar(
            isReadLocal = isReadLocal,
            scrollProgress = scrollProgress,
            fontSizeSp = fontSizeSp,
            onDecreaseFontSize = { if (fontSizeSp > 13) fontSizeSp-- },
            onIncreaseFontSize = { if (fontSizeSp < 22) fontSizeSp++ }
        )

        Spacer(Modifier.height(8.dp))

        // CONTENIDO DESPLAZABLE (E-BOOK, VÍDEO, EXAMEN)
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(scrollState)
        ) {

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

        // Botón de Confirmación con Mini-Reto de Comprensión
        if (!isReadLocal) {
            Button(
                onClick = { showChallengeDialog = true },
                enabled = isScrollAtBottom,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(54.dp)
                    .padding(bottom = 20.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = AccentAmber,
                    disabledContainerColor = Color.DarkGray.copy(alpha = 0.5f)
                ),
                shape = RoundedCornerShape(14.dp)
            ) {
                Icon(Icons.Default.Psychology, null, tint = if (isScrollAtBottom) Color.Black else Color.Gray)
                Spacer(Modifier.width(8.dp))
                Text(
                    text = if (isScrollAtBottom) "🎯 INICIAR MINI-RETO DE COMPRENSIÓN (+50 Pts 🔥)" else "📜 DESPLÁZATE HASTA EL FINAL PARA EL RETO",
                    color = if (isScrollAtBottom) Color.Black else Color.White,
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
                                text = "Lee todo el contenido de la clase y supera el Mini-Reto de Comprensión para desbloquear el video.",
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
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
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
                        }
                    }

                    Spacer(Modifier.height(12.dp))
                    if (unitProgress?.isRecuperativoUsed == true) {
                        Text(
                            text = "🔒 Máximo alcanzado: 1 recuperativo por unidad.",
                            color = Color.Gray,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                    } else {
                        var requested by remember { mutableStateOf(unitProgress?.recuperativoRequested == true) }
                        OutlinedButton(
                            onClick = {
                                requested = true
                                onRequestRecuperativo()
                            },
                            enabled = !requested,
                            modifier = Modifier.fillMaxWidth(),
                            border = BorderStroke(1.dp, AccentAmber)
                        ) {
                            Icon(Icons.Default.Psychology, null, tint = AccentAmber, modifier = Modifier.size(16.dp))
                            Spacer(Modifier.width(8.dp))
                            Text(
                                text = if (requested) "SOLICITUD ENVIADA AL PROFESOR" else "🔄 SOLICITAR RECUPERATIVO POR WI-FI",
                                color = AccentAmber,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
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

        // 📷 ESCANEAR RESUMEN EN CUADERNO (OCR OFFLINE)
        if (isReadLocal) {
            Spacer(Modifier.height(16.dp))
            val hasNotebookGrade = unitProgress?.notebookEvaluated == true
            val notebookActive = !isLocked || enabledNotebookUnits.split(",").filter { it.isNotEmpty() }.contains(unit.index.toString())

            if (hasNotebookGrade) {
                Card(
                    modifier = Modifier.fillMaxWidth().padding(bottom = 24.dp),
                    colors = CardDefaults.cardColors(containerColor = StatusSuccess.copy(alpha = 0.1f)),
                    border = BorderStroke(1.dp, StatusSuccess.copy(alpha = 0.5f)),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.CheckCircle, null, tint = StatusSuccess, modifier = Modifier.size(28.dp))
                            Spacer(Modifier.width(12.dp))
                            Column {
                                Text("Cuaderno Escaneado y Evaluado", fontWeight = FontWeight.Bold, color = StatusSuccess, fontSize = 14.sp)
                                val notebookGrade = unitProgress?.notebookGrade ?: 0f
                                Text("Calificación OCR: ${"%.2f".format(Locale.US, notebookGrade)} / 20", color = Color.White, fontWeight = FontWeight.ExtraBold, fontSize = 13.sp)
                                if (!unitProgress?.notebookFeedback.isNullOrEmpty()) {
                                    Spacer(Modifier.height(4.dp))
                                    Text(unitProgress?.notebookFeedback ?: "", color = Color.LightGray, fontSize = 10.sp)
                                }
                            }
                        }

                        Spacer(Modifier.height(12.dp))
                        if (unitProgress?.isRecuperativoUsed == true) {
                            Text("🔒 Máximo alcanzado: 1 recuperativo por unidad.", color = Color.Gray, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        } else {
                            var requested by remember { mutableStateOf(unitProgress?.recuperativoRequested == true) }
                            OutlinedButton(
                                onClick = {
                                    requested = true
                                    onRequestNotebookRecuperativo()
                                },
                                enabled = !requested,
                                modifier = Modifier.fillMaxWidth(),
                                border = BorderStroke(1.dp, AccentCyan)
                            ) {
                                Icon(Icons.Default.Psychology, null, tint = AccentCyan, modifier = Modifier.size(16.dp))
                                Spacer(Modifier.width(8.dp))
                                Text(if (requested) "SOLICITUD DE RECUPERATIVO ENVIADA" else "🔄 SOLICITAR RECUPERATIVO DE CUADERNO", color = AccentCyan, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            } else if (notebookActive) {
                PrimaryActionButton(
                    text = "📷 ESCANEAR RESUMEN EN MI CUADERNO",
                    onClick = { cameraLauncher.launch(null) },
                    containerColor = AccentCyan,
                    contentColor = Color.Black,
                    icon = Icons.Default.MenuBook
                )
                Spacer(Modifier.height(16.dp))
            } else {
                Card(
                    modifier = Modifier.fillMaxWidth().padding(bottom = 24.dp),
                    colors = CardDefaults.cardColors(containerColor = CosmicDarkSurface),
                    border = BorderStroke(1.dp, Color.Gray.copy(alpha = 0.3f)),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Lock, null, tint = Color.Gray, modifier = Modifier.size(24.dp))
                        Spacer(Modifier.width(12.dp))
                        Column {
                            Text("Escaneo de Cuaderno Bloqueado", fontWeight = FontWeight.Bold, color = Color.Gray, fontSize = 13.sp)
                            Text("El profesor habilitará el escaneo OCR de esta unidad en breve.", color = Color.Gray, fontSize = 11.sp)
                        }
                    }
                }
            }
        }

        if (isScanningNotebook) {
            AlertDialog(
                onDismissRequest = {},
                containerColor = CosmicDarkSurface,
                title = { Text("Procesando OCR Offline...", color = Color.White) },
                text = {
                    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth().padding(20.dp)) {
                        CircularProgressIndicator(color = AccentCyan)
                        Spacer(Modifier.height(16.dp))
                        Text("Analizando texto manuscrito y rúbrica sobre 20 pts...", color = Color.LightGray, fontSize = 12.sp)
                    }
                },
                confirmButton = {}
            )
        }

        ocrResultDialog?.let { result ->
            AlertDialog(
                onDismissRequest = { ocrResultDialog = null },
                containerColor = CosmicDarkSurface,
                title = { Text("Evaluación OCR del Cuaderno", color = AccentCyan, fontWeight = FontWeight.Bold) },
                text = {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .verticalScroll(rememberScrollState())
                    ) {
                        Text(
                            text = "Calificación Final: ${"%.2f".format(Locale.US, result.totalScore)} / 20.00",
                            fontWeight = FontWeight.ExtraBold,
                            color = StatusSuccess,
                            fontSize = 16.sp
                        )
                        Spacer(Modifier.height(12.dp))
                        Text("• Contenido y Comprensión: ${"%.1f".format(result.contentScore)}/10", color = Color.White, fontSize = 13.sp)
                        Text(result.contentFeedback, color = Color.Gray, fontSize = 11.sp)
                        Spacer(Modifier.height(8.dp))
                        Text("• Redacción y Coherencia: ${"%.1f".format(result.coherenceScore)}/5", color = Color.White, fontSize = 13.sp)
                        Text(result.coherenceFeedback, color = Color.Gray, fontSize = 11.sp)
                        Spacer(Modifier.height(8.dp))
                        Text("• Ortografía y Gramática: ${"%.1f".format(result.spellingScore)}/5", color = Color.White, fontSize = 13.sp)
                        if (result.spellingCorrections.isNotEmpty()) {
                            Text("Correcciones: ${result.spellingCorrections.joinToString(", ")}", color = AccentAmber, fontSize = 11.sp)
                        } else {
                            Text("¡Excelente ortografía detectada!", color = StatusSuccess, fontSize = 11.sp)
                        }
                    }
                },
                confirmButton = {
                    Button(
                        onClick = {
                            onSaveNotebookGrade(unit.index, result)
                            ocrResultDialog = null
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = AccentCyan)
                    ) {
                        Text("Aceptar y Enviar", color = Color.Black, fontWeight = FontWeight.Bold)
                    }
                },
                dismissButton = {
                    TextButton(onClick = { ocrResultDialog = null }) {
                        Text("Reintentar Foto", color = Color.Gray)
                    }
                }
            )
        }
    }
}
}
