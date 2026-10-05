package com.example.ui.screens.tabs

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Assessment
import androidx.compose.material.icons.filled.BugReport
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.EventBusy
import androidx.compose.material.icons.filled.FolderOpen
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Terminal
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.AttendanceRecord
import com.example.data.UnitProgress
import com.example.data.UserProfile
import com.example.data.model.UnitData
import com.example.ui.components.AcademicStatusCard
import com.example.ui.components.StreakCard
import com.example.ui.components.SystemActionRow
import com.example.ui.theme.AccentAmber
import com.example.ui.theme.AccentCyan
import com.example.ui.theme.AccentTeal
import com.example.ui.theme.CosmicDarkSurface
import com.example.ui.theme.StatusError
import com.example.ui.theme.StatusSuccess
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun StudentStatusTab(
    profile: UserProfile?,
    studyProgress: List<UnitProgress>,
    units: List<UnitData>,
    totalClasses: Int,
    isLocked: Boolean,
    attendanceHistory: List<AttendanceRecord> = emptyList(),
    onAbandonClass: () -> Unit,
    onViewConsole: () -> Unit,
    onReportBug: () -> Unit,
    onDownloadReport: () -> Unit,
    onOpenReportViewer: () -> Unit = {}
) {
    val dateFormat = SimpleDateFormat("EEEE dd/MM/yyyy - HH:mm", Locale("es", "ES"))

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {
        val examGrades = studyProgress.filter { it.examCompleted && it.examGrade != null }
        val hasEvaluations = examGrades.isNotEmpty()
        val averageGrade = if (hasEvaluations) examGrades.sumOf { it.examGrade!!.toDouble() }.toFloat() / examGrades.size else 0f
        val attendanceCount = profile?.attendanceCount ?: 0
        val attendancePct = if (totalClasses > 0) (attendanceCount.toFloat() / totalClasses).coerceIn(0f, 1f) else 0f
        val completedUnits = studyProgress.count { it.isRead || it.examCompleted }
        val missedClasses = (totalClasses - attendanceCount).coerceAtLeast(0)
        val failedExamsCount = examGrades.count { (it.examGrade ?: 0f) < 11.5f }

        // 1. Tarjeta de Racha de Constancia
        StreakCard(
            streakCount = attendanceCount,
            completedUnitsCount = completedUnits
        )

        // 2. Tarjeta Resumen de Estado Académico
        AcademicStatusCard(
            averageGrade = averageGrade,
            hasEvaluations = hasEvaluations,
            attendancePct = attendancePct,
            totalClasses = totalClasses,
            attendanceCount = attendanceCount,
            onAbandonClass = if (isLocked) onAbandonClass else null
        )

        Spacer(Modifier.height(16.dp))

        // 3. TARJETA "DIAGNÓSTICO ACADÉMICO: LO BUENO Y LO MALO"
        Card(
            modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp),
            colors = CardDefaults.cardColors(containerColor = CosmicDarkSurface),
            shape = RoundedCornerShape(16.dp),
            border = BorderStroke(1.dp, AccentCyan.copy(alpha = 0.2f))
        ) {
            Column(Modifier.padding(16.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Assessment, null, tint = AccentCyan, modifier = Modifier.size(20.dp))
                    Spacer(Modifier.width(8.dp))
                    Text(
                        text = "Diagnóstico Académico Integral",
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        fontSize = 14.sp
                    )
                }

                Spacer(Modifier.height(12.dp))

                // PUNTOS FORTALEZAS (LO BUENO)
                Card(
                    modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp),
                    colors = CardDefaults.cardColors(containerColor = StatusSuccess.copy(alpha = 0.1f)),
                    border = BorderStroke(1.dp, StatusSuccess.copy(alpha = 0.4f)),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Column(Modifier.padding(12.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.CheckCircle, null, tint = StatusSuccess, modifier = Modifier.size(16.dp))
                            Spacer(Modifier.width(6.dp))
                            Text("PUNTOS FUERTES", fontWeight = FontWeight.ExtraBold, color = StatusSuccess, fontSize = 11.sp)
                        }
                        Spacer(Modifier.height(6.dp))
                        Text(
                            text = "• Asistencia al ${(attendancePct * 100).toInt()}% de los encuentros ($attendanceCount de $totalClasses clases).",
                            color = Color.White,
                            fontSize = 11.sp
                        )
                        Text(
                            text = "• $completedUnits unidades estudiadas/completadas de ${units.size} totales.",
                            color = Color.White,
                            fontSize = 11.sp
                        )
                        if (averageGrade >= 12f) {
                            Text(
                                text = "• Promedio académico aprobado: ${"%.2f".format(Locale.US, averageGrade)} / 20 Pts.",
                                color = Color.White,
                                fontSize = 11.sp
                            )
                        }
                    }
                }

                // ÁREAS DE ATENCIÓN (LO MALO / MEJORAS)
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = if (missedClasses > 0 || failedExamsCount > 0) StatusError.copy(alpha = 0.1f) else AccentTeal.copy(alpha = 0.1f)),
                    border = BorderStroke(1.dp, if (missedClasses > 0 || failedExamsCount > 0) StatusError.copy(alpha = 0.4f) else AccentTeal.copy(alpha = 0.4f)),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Column(Modifier.padding(12.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = if (missedClasses > 0 || failedExamsCount > 0) Icons.Default.Warning else Icons.Default.Star,
                                contentDescription = null,
                                tint = if (missedClasses > 0 || failedExamsCount > 0) StatusError else AccentTeal,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(Modifier.width(6.dp))
                            Text(
                                text = if (missedClasses > 0 || failedExamsCount > 0) "ÁREAS DE ATENCIÓN" else "EXCELENTE RENDIMIENTO",
                                fontWeight = FontWeight.ExtraBold,
                                color = if (missedClasses > 0 || failedExamsCount > 0) StatusError else AccentTeal,
                                fontSize = 11.sp
                            )
                        }
                        Spacer(Modifier.height(6.dp))
                        if (missedClasses > 0) {
                            Text(
                                text = "• Has faltado a $missedClasses ${if (missedClasses == 1) "encuentro" else "encuentros"} de clase.",
                                color = StatusError,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                        } else {
                            Text("• ¡Asistencia perfecta! No registras faltas.", color = Color.White, fontSize = 11.sp)
                        }

                        if (failedExamsCount > 0) {
                            Text(
                                text = "• Tienes $failedExamsCount ${if (failedExamsCount == 1) "examen reprobado" else "exámenes reprobados"} (< 11.5 Pts). Solicita recuperativo a tu profesor.",
                                color = StatusError,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                        } else if (hasEvaluations) {
                            Text("• Todos los exámenes presentados están aprobados.", color = Color.White, fontSize = 11.sp)
                        }
                    }
                }
            }
        }

        // 4. DESGLOSE DE EVALUACIONES POR UNIDAD
        Card(
            modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp),
            colors = CardDefaults.cardColors(containerColor = CosmicDarkSurface),
            shape = RoundedCornerShape(16.dp),
            border = BorderStroke(1.dp, Color.Gray.copy(alpha = 0.2f))
        ) {
            Column(Modifier.padding(16.dp)) {
                Text(
                    text = "Detalle de Exámenes por Unidad",
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    fontSize = 13.sp,
                    modifier = Modifier.padding(bottom = 10.dp)
                )

                if (units.isEmpty()) {
                    Text("No hay unidades disponibles", color = Color.Gray, fontSize = 11.sp)
                } else {
                    units.forEach { unit ->
                        val p = studyProgress.find { it.unitIndex == unit.index }
                        val grade = p?.examGrade
                        val hasGrade = p?.examCompleted == true && grade != null
                        val isPassed = (grade ?: 0f) >= 11.5f

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Unidad ${unit.index}: ${unit.title}",
                                color = Color.White,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Medium,
                                modifier = Modifier.weight(1f)
                            )
                            if (hasGrade) {
                                Text(
                                    text = "${"%.2f".format(Locale.US, grade)} / 20 ${if (isPassed) "🟢" else "🔴"}",
                                    color = if (isPassed) StatusSuccess else StatusError,
                                    fontWeight = FontWeight.ExtraBold,
                                    fontSize = 11.sp
                                )
                            } else {
                                Text(
                                    text = "Sin Presentar ⏳",
                                    color = Color.Gray,
                                    fontSize = 10.sp
                                )
                            }
                        }
                    }
                }
            }
        }

        // 5. HISTORIAL FECHADO DE ASISTENCIAS
        Card(
            modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp),
            colors = CardDefaults.cardColors(containerColor = CosmicDarkSurface),
            shape = RoundedCornerShape(16.dp),
            border = BorderStroke(1.dp, Color.Gray.copy(alpha = 0.2f))
        ) {
            Column(Modifier.padding(16.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.DateRange, null, tint = AccentTeal, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(8.dp))
                    Text(
                        text = "Historial Fechado de Asistencias",
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        fontSize = 13.sp
                    )
                }

                Spacer(Modifier.height(10.dp))

                if (attendanceHistory.isEmpty()) {
                    Text(
                        text = "Aún no hay registros de asistencia marcados.",
                        color = Color.Gray,
                        fontSize = 11.sp
                    )
                } else {
                    attendanceHistory.forEach { record ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = dateFormat.format(Date(record.date)),
                                color = Color.LightGray,
                                fontSize = 11.sp
                            )
                            Text(
                                text = record.status,
                                color = if (record.status == "Presente") StatusSuccess else StatusError,
                                fontWeight = FontWeight.Bold,
                                fontSize = 11.sp
                            )
                        }
                    }
                }
            }
        }

        // ACCIONES DE SISTEMA
        SystemActionRow(
            icon = Icons.Default.Terminal,
            label = "Ver Consola de Logs",
            color = AccentAmber,
            onClick = onViewConsole
        )

        Spacer(Modifier.height(8.dp))

        SystemActionRow(
            icon = Icons.Default.BugReport,
            label = "Reportar Fallo por Correo/Soporte",
            color = Color.LightGray,
            onClick = onReportBug
        )

        Spacer(Modifier.height(8.dp))

        SystemActionRow(
            icon = Icons.Default.Assessment,
            label = "📄 Descargar Boleta y Métricas Personales",
            color = AccentCyan,
            onClick = onDownloadReport
        )

        Spacer(Modifier.height(8.dp))

        SystemActionRow(
            icon = Icons.Default.FolderOpen,
            label = "📂 Ver Mis Boletas y Reportes Guardados",
            color = AccentTeal,
            onClick = onOpenReportViewer
        )
    }
}
