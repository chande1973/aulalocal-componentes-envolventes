package com.example.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Wifi
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.UnitData
import com.example.ui.theme.*
import java.util.Locale

@Composable
fun UnitCard(
    unit: UnitData,
    isEnabled: Boolean,
    isRead: Boolean,
    onClick: () -> Unit
) {
    Card(
        Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
            .clickable(isEnabled) { onClick() },
        colors = CardDefaults.cardColors(
            containerColor = if (isEnabled) CosmicDarkSurface else Color.DarkGray.copy(alpha = 0.05f)
        ),
        border = BorderStroke(
            1.dp,
            if (isRead) StatusSuccess.copy(alpha = 0.5f)
            else if (isEnabled) AccentCyan.copy(alpha = 0.4f)
            else Color.Gray.copy(alpha = 0.15f)
        ),
        shape = RoundedCornerShape(20.dp),
        elevation = if (isEnabled && !isRead) CardDefaults.cardElevation(defaultElevation = 6.dp) else CardDefaults.cardElevation(0.dp)
    ) {
        Column(Modifier.padding(16.dp)) {
            Row(
                Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Badge de número de unidad
                Box(
                    Modifier
                        .size(48.dp)
                        .clip(RoundedCornerShape(14.dp))
                        .background(
                            if (isEnabled) {
                                if (isRead) StatusSuccess.copy(alpha = 0.15f)
                                else AccentCyan.copy(alpha = 0.15f)
                            } else Color.Gray.copy(alpha = 0.1f)
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = String.format(Locale.ROOT, "%02d", unit.index),
                        color = if (isEnabled) (if (isRead) StatusSuccess else AccentCyan) else Color.Gray,
                        fontWeight = FontWeight.Black,
                        fontSize = 16.sp
                    )
                }
                Spacer(Modifier.width(16.dp))
                Column(Modifier.weight(1f)) {
                    Row(
                        Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = unit.title,
                            color = if (isEnabled) TextPrimary else Color.Gray,
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.weight(1f)
                        )
                        Spacer(Modifier.width(8.dp))
                        // Estado / Badge
                        Surface(
                            color = if (isRead) StatusSuccess.copy(alpha = 0.15f) else if (isEnabled) AccentCyan.copy(alpha = 0.15f) else Color.Gray.copy(alpha = 0.1f),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = if (isRead) Icons.Default.CheckCircle else if (isEnabled) Icons.AutoMirrored.Filled.MenuBook else Icons.Default.Lock,
                                    contentDescription = null,
                                    tint = if (isRead) StatusSuccess else if (isEnabled) AccentCyan else Color.Gray,
                                    modifier = Modifier.size(12.dp)
                                )
                                Spacer(Modifier.width(4.dp))
                                Text(
                                    text = if (isRead) "COMPLETADA" else if (isEnabled) "DISPONIBLE" else "BLOQUEADA",
                                    color = if (isRead) StatusSuccess else if (isEnabled) AccentCyan else Color.Gray,
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    letterSpacing = 0.5.sp
                                )
                            }
                        }
                    }
                    if (unit.description.isNotEmpty()) {
                        Spacer(Modifier.height(4.dp))
                        Text(
                            text = unit.description,
                            color = TextSecondary,
                            fontSize = 12.sp,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
            }

            // Insignias de Recursos (Video, Teoría, Reto, Examen)
            Spacer(Modifier.height(12.dp))
            HorizontalDivider(color = Color.DarkGray.copy(alpha = 0.3f), thickness = 1.dp)
            Spacer(Modifier.height(10.dp))
            
            Row(
                Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (unit.topicText.isNotEmpty() || unit.summary.isNotEmpty()) {
                    ResourceBadge(icon = "📖", label = "Teoría")
                }
                if (!unit.videoFile.isNullOrEmpty() || unit.extendedContent.isNotEmpty()) {
                    ResourceBadge(icon = "🎥", label = "Video/Media")
                }
                if (unit.challengeInstructions.isNotEmpty() || unit.challengeQuestions.isNotEmpty()) {
                    ResourceBadge(icon = "⚡", label = "Reto")
                }
                if (unit.examQuestions.isNotEmpty()) {
                    ResourceBadge(icon = "📝", label = "Examen (${unit.examQuestions.size})")
                }
            }
        }
    }
}

@Composable
fun ResourceBadge(icon: String, label: String) {
    Surface(
        color = Color.DarkGray.copy(alpha = 0.25f),
        shape = RoundedCornerShape(8.dp)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(text = icon, fontSize = 11.sp)
            Spacer(Modifier.width(4.dp))
            Text(
                text = label,
                color = Color.LightGray,
                fontSize = 10.sp,
                fontWeight = FontWeight.Medium
            )
        }
    }
}

@Composable
fun StudentProfileHeader(profile: com.example.data.UserProfile?) {
    if (profile == null) return

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 16.dp),
        colors = CardDefaults.cardColors(containerColor = CosmicDarkSurface),
        shape = RoundedCornerShape(24.dp),
        border = BorderStroke(1.dp, AccentCyan.copy(alpha = 0.1f))
    ) {
        Row(
            modifier = Modifier.padding(20.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(64.dp)
                    .clip(CircleShape)
                    .background(AccentCyan.copy(alpha = 0.1f)),
                contentAlignment = Alignment.Center
            ) {
                Text(profile.avatar.ifEmpty { "👤" }, fontSize = 32.sp)
            }
            Spacer(Modifier.width(20.dp))
            Column {
                Text(
                    text = "${profile.lastName}, ${profile.firstName}",
                    color = TextPrimary,
                    fontWeight = FontWeight.Black,
                    fontSize = 22.sp,
                    lineHeight = 26.sp
                )
                Text(
                    text = "CI: ${profile.cardId}",
                    color = TextSecondary,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium
                )
                if (profile.activeSection.isNotEmpty()) {
                    Surface(
                        color = AccentCyan.copy(alpha = 0.1f),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.padding(top = 8.dp)
                    ) {
                        Text(
                            text = "SECCIÓN: ${profile.activeSection}",
                            color = AccentCyan,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Black,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                            letterSpacing = 1.sp
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun StreakCard(
    streakCount: Int,
    completedUnitsCount: Int
) {
    val totalStreak = streakCount + completedUnitsCount
    val motivationalText = when {
        totalStreak == 0 -> "¡Asiste a tu primera clase para iniciar tu racha!"
        totalStreak in 1..2 -> "¡Buen comienzo! Mantén la constancia en las siguientes clases."
        totalStreak in 3..5 -> "🔥 ¡Vas genial! Llevas $totalStreak interacciones consecutivas."
        else -> "⚡ ¡Rendimiento sobresaliente! $totalStreak clases/unidades en racha."
    }

    Card(
        modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp),
        colors = CardDefaults.cardColors(containerColor = AccentAmber.copy(alpha = 0.1f)),
        shape = RoundedCornerShape(20.dp),
        border = BorderStroke(1.dp, AccentAmber.copy(alpha = 0.4f))
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(AccentAmber.copy(alpha = 0.2f)),
                contentAlignment = Alignment.Center
            ) {
                Text("🔥", fontSize = 26.sp)
            }
            Spacer(Modifier.width(16.dp))
            Column(Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "Racha de Constancia",
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        fontSize = 14.sp
                    )
                    Spacer(Modifier.width(8.dp))
                    Surface(
                        color = AccentAmber,
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text(
                            text = "🔥 $totalStreak",
                            color = Color.Black,
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 11.sp,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                        )
                    }
                }
                Spacer(Modifier.height(4.dp))
                Text(
                    text = motivationalText,
                    color = Color.LightGray,
                    fontSize = 11.sp,
                    lineHeight = 14.sp
                )
            }
        }
    }
}

@Composable
fun AcademicStatusCard(
    averageGrade: Float,
    hasEvaluations: Boolean = false,
    attendancePct: Float,
    totalClasses: Int,
    attendanceCount: Int,
    onAbandonClass: (() -> Unit)? = null
) {
    val isNewCourse = totalClasses == 0 && !hasEvaluations
    
    val (statusText, statusColor) = when {
        isNewCourse -> "AL DÍA" to AccentCyan
        hasEvaluations && averageGrade >= 11.5f && (totalClasses == 0 || attendancePct >= 0.75f) -> "EXCELENTE" to StatusSuccess
        !hasEvaluations && attendancePct >= 0.75f -> "AL DÍA" to StatusSuccess
        attendancePct >= 0.60f || (hasEvaluations && averageGrade >= 9.5f) -> "EN REVISIÓN" to StatusWarning
        else -> "EN RIESGO" to StatusError
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = CosmicDarkSurface),
        shape = RoundedCornerShape(24.dp),
        border = BorderStroke(1.dp, statusColor.copy(alpha = 0.2f))
    ) {
        Column(Modifier.padding(20.dp)) {
            Text("ESTATUS ACADÉMICO", fontWeight = FontWeight.ExtraBold, color = AccentCyan, fontSize = 11.sp, letterSpacing = 1.sp)
            Spacer(Modifier.height(12.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(Modifier.size(8.dp).clip(CircleShape).background(statusColor))
                Spacer(Modifier.width(8.dp))
                Text(
                    text = statusText,
                    color = statusColor,
                    fontWeight = FontWeight.Black,
                    fontSize = 20.sp
                )
            }
            
            Spacer(Modifier.height(24.dp))
            
            // Sección de Notas
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text("Promedio Actual", color = TextSecondary, fontSize = 13.sp)
                Text(if (hasEvaluations) "%.2f".format(Locale.US, averageGrade) else "Sin evaluar", color = TextPrimary, fontWeight = FontWeight.Bold)
            }
            Spacer(Modifier.height(8.dp))
            LinearProgressIndicator(
                progress = { if (hasEvaluations) averageGrade / 20f else 1f },
                modifier = Modifier.fillMaxWidth().height(10.dp).clip(CircleShape),
                color = if (!hasEvaluations || averageGrade >= 11.5f) AccentCyan else StatusError,
                trackColor = CosmicDarkCard
            )
            
            Spacer(Modifier.height(20.dp))
            
            // Sección de Asistencia
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text("Asistencia", color = TextSecondary, fontSize = 13.sp)
                Text(if (totalClasses > 0) "${(attendancePct * 100).toInt()}%" else "100%", color = TextPrimary, fontWeight = FontWeight.Bold)
            }
            Spacer(Modifier.height(8.dp))
            LinearProgressIndicator(
                progress = { if (totalClasses > 0) attendancePct.coerceIn(0f, 1f) else 1f },
                modifier = Modifier.fillMaxWidth().height(10.dp).clip(CircleShape),
                color = if (totalClasses == 0 || attendancePct >= 0.75f) StatusSuccess else StatusError,
                trackColor = CosmicDarkCard
            )

            Spacer(Modifier.height(16.dp))
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceAround) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("$totalClasses", color = TextPrimary, fontWeight = FontWeight.Bold)
                    Text("Clases", color = TextSecondary, fontSize = 11.sp)
                }
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("$attendanceCount", color = TextPrimary, fontWeight = FontWeight.Bold)
                    Text("Asistencias", color = TextSecondary, fontSize = 11.sp)
                }
            }
            
            if (onAbandonClass != null) {
                Spacer(Modifier.height(24.dp))
                Button(
                    onClick = onAbandonClass,
                    modifier = Modifier.fillMaxWidth().height(48.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = StatusError.copy(alpha = 0.2f), contentColor = StatusError),
                    shape = RoundedCornerShape(12.dp),
                    border = BorderStroke(1.dp, StatusError)
                ) {
                    Text("SALIR DE LA SESIÓN", fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}
@Composable
fun ConnectionStatusIcon(isLocked: Boolean, isSyncing: Boolean, isListenerMode: Boolean, onClick: () -> Unit) {
    if (isLocked) {
        val color = if (isSyncing) (if (isListenerMode) StatusError else StatusSuccess) else Color.Gray
        Surface(
            color = color.copy(alpha = 0.15f),
            shape = CircleShape,
            modifier = Modifier.padding(end = Dimensions.PaddingSmall).clickable { onClick() }
        ) {
            Box(Modifier.padding(Dimensions.PaddingSmall).size(8.dp).clip(CircleShape).background(color))
        }
    } else {
        IconButton(onClick = onClick) {
            Icon(Icons.Default.Wifi, null, tint = AccentCyan)
        }
    }
}

@Composable
fun ExamNotificationCard(unitIndex: Int, unitTitle: String, onAccept: () -> Unit, onDismiss: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
            .padding(top = 40.dp),
        colors = CardDefaults.cardColors(containerColor = CosmicDarkSurface),
        border = BorderStroke(2.dp, AccentAmber),
        shape = RoundedCornerShape(24.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
    ) {
        Column(Modifier.padding(20.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Box(
                modifier = Modifier
                    .size(56.dp)
                    .clip(CircleShape)
                    .background(AccentAmber.copy(alpha = 0.2f)),
                contentAlignment = Alignment.Center
            ) {
                Text("📝", fontSize = 30.sp)
            }
            Spacer(Modifier.height(12.dp))
            Text(
                text = "¡EXAMEN DISPONIBLE!",
                fontWeight = FontWeight.Black,
                color = AccentAmber,
                fontSize = 18.sp,
                letterSpacing = 1.sp
            )
            Spacer(Modifier.height(4.dp))
            Text(
                text = "Unidad $unitIndex: $unitTitle",
                color = TextPrimary,
                fontWeight = FontWeight.Bold,
                fontSize = 15.sp,
                textAlign = TextAlign.Center
            )
            Spacer(Modifier.height(6.dp))
            Text(
                text = "El profesor ha habilitado el examen para esta unidad. ¿Deseas presentarlo ahora?",
                color = TextSecondary,
                fontSize = 12.sp,
                textAlign = TextAlign.Center
            )
            Spacer(Modifier.height(20.dp))
            Button(
                onClick = onAccept,
                modifier = Modifier.fillMaxWidth().height(48.dp),
                colors = ButtonDefaults.buttonColors(containerColor = AccentAmber),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text("📝 PRESENTAR EXAMEN AHORA", color = Color.Black, fontWeight = FontWeight.ExtraBold)
            }
            Spacer(Modifier.height(8.dp))
            TextButton(
                onClick = onDismiss,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("LUEGO / CONTINUAR LEYENDO", color = Color.Gray, fontSize = 12.sp)
            }
        }
    }
}