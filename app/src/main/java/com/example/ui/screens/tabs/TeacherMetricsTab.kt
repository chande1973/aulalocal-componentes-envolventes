package com.example.ui.screens.tabs

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.History
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
import com.example.data.SyncedStudent
import com.example.data.LocalSection
import com.example.ui.components.TeacherReportDialog
import com.example.ui.theme.*
import com.example.util.toSafeJSONObject
import org.json.JSONObject

@Composable
fun TeacherMetricsTab(
    liveSection: String,
    sections: List<LocalSection>,
    syncedStudents: List<SyncedStudent>,
    units: List<com.example.data.model.UnitData>,
    onGenerateAcademicReport: () -> Unit,
    onGenerateAttendanceReport: () -> Unit,
    onStudentClick: (SyncedStudent) -> Unit
) {
    val currentSection = sections.find { it.name == liveSection }
    val totalClases = currentSection?.totalClasses ?: 0
    val totalUnitsCount = units.size
    
    if (liveSection.isEmpty()) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text(stringResource(R.string.msg_select_aula_for_metrics), color = Color.Gray)
        }
    } else {
        val totalStudents = syncedStudents.size
        val avgAttendance = if (totalStudents > 0 && totalClases > 0) {
            (syncedStudents.sumOf { it.attendanceCount }.toFloat() / (totalStudents * totalClases)) * 100
        } else 0f
        
        var totalGradesSum = 0f
        var totalGradesCount = 0
        syncedStudents.forEach { s ->
            val gradesObj = s.examGradesString.toSafeJSONObject()
            units.forEach { unit ->
                val g = gradesObj.optString(unit.index.toString(), "")
                if (g.isNotEmpty()) {
                    totalGradesSum += g.toFloatOrNull() ?: 0f
                    totalGradesCount++
                }
            }
        }
        val globalAvgGrade = if (totalGradesCount > 0) totalGradesSum / totalGradesCount else 0f

        val scrollState = rememberScrollState()
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(scrollState)
        ) {
            Text(stringResource(R.string.label_metrics_title), style = MaterialTheme.typography.titleLarge, color = Color.White)
            
            Row(Modifier.fillMaxWidth().padding(vertical = 8.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                MetricSummaryCard(Modifier.weight(1f), "Asistencia", "${avgAttendance.toInt()}%", AccentTeal)
                MetricSummaryCard(Modifier.weight(1f), "Promedio", "%.2f".format(globalAvgGrade), AccentCyan)
            }

            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Button(
                    onClick = onGenerateAcademicReport,
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(containerColor = CosmicDarkSurface),
                    border = BorderStroke(1.dp, Color.Gray),
                    contentPadding = PaddingValues(4.dp)
                ) {
                    Icon(Icons.Default.Download, null, modifier = Modifier.size(14.dp))
                    Spacer(Modifier.width(4.dp))
                    Text("Acta", fontSize = 10.sp)
                }
                Button(
                    onClick = onGenerateAttendanceReport,
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(containerColor = CosmicDarkSurface),
                    border = BorderStroke(1.dp, Color.Gray),
                    contentPadding = PaddingValues(4.dp)
                ) {
                    Icon(Icons.Default.History, null, modifier = Modifier.size(14.dp))
                    Spacer(Modifier.width(4.dp))
                    Text("Asist", fontSize = 10.sp)
                }
            }

            Spacer(Modifier.height(8.dp))
            var showReportDialog by remember { mutableStateOf(false) }
            Button(
                onClick = { showReportDialog = true },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(containerColor = AccentTeal.copy(alpha = 0.15f)),
                border = BorderStroke(1.dp, AccentTeal)
            ) {
                Icon(Icons.Default.Download, null, tint = AccentTeal, modifier = Modifier.size(16.dp))
                Spacer(Modifier.width(8.dp))
                Text("📊 VER REPORTE CONSOLIDADO", fontWeight = FontWeight.Bold, color = AccentTeal)
            }

            if (showReportDialog) {
                TeacherReportDialog(
                    sectionName = liveSection,
                    totalClasses = totalClases,
                    students = syncedStudents,
                    totalUnits = totalUnitsCount,
                    onGenerateCsv = {
                        onGenerateAcademicReport()
                        showReportDialog = false
                    },
                    onDismiss = { showReportDialog = false }
                )
            }

            Spacer(Modifier.height(16.dp))
            Text("Semaforización Académica", fontWeight = FontWeight.Bold, color = Color.White, fontSize = 14.sp)
            
            LazyColumn(Modifier.weight(1f)) {
                items(syncedStudents) { student ->
                    val gradesObj = student.examGradesString.toSafeJSONObject()
                    var sum = 0f
                    var count = 0
                    units.forEach { unit ->
                        val g = gradesObj.optString(unit.index.toString(), "")
                        if (g.isNotEmpty()) { sum += g.toFloatOrNull() ?: 0f; count++ }
                    }
                    val avg = if (count > 0) sum / count else 0f
                    val attPct = if (totalClases > 0) (student.attendanceCount.toFloat() / totalClases * 100).toInt() else 0
                    
                    val isNewStudent = totalClases == 0 && count == 0
                    val isPassingAttendance = totalClases == 0 || attPct >= 75
                    val isPassingGrade = count > 0 && avg >= 11.5f

                    val (statusColor, statusText) = when {
                        isNewStudent -> AccentCyan to "AL DÍA"
                        count > 0 && avg >= 11.5f && isPassingAttendance -> StatusSuccess to "APROBADO"
                        count > 0 && avg >= 6.0f -> StatusWarning to "PER"
                        count > 0 && avg < 6.0f -> StatusError to "REPROBADO"
                        count == 0 && isPassingAttendance -> StatusSuccess to "AL DÍA"
                        !isPassingAttendance && (count > 0 && !isPassingGrade) -> StatusError to "CRÍTICO"
                        else -> StatusWarning to "EN REVISIÓN"
                    }

                    Card(
                        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp).clickable { onStudentClick(student) },
                        colors = CardDefaults.cardColors(containerColor = CosmicDarkSurface),
                        border = BorderStroke(1.dp, statusColor.copy(alpha = 0.4f))
                    ) {
                        Row(Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                            Box(Modifier.size(8.dp).clip(CircleShape).background(statusColor))
                            Spacer(Modifier.width(12.dp))
                            Column(Modifier.weight(1f)) {
                                Text("${student.lastName}, ${student.firstName}", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                Text("Nota: %.1f | Asist: $attPct%%".format(java.util.Locale.US, avg), fontSize = 11.sp, color = Color.Gray)
                            }
                            Surface(color = statusColor.copy(alpha = 0.1f), shape = RoundedCornerShape(4.dp)) {
                                Text(statusText, color = statusColor, fontSize = 9.sp, fontWeight = FontWeight.ExtraBold, modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp))
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun MetricSummaryCard(modifier: Modifier = Modifier, label: String, value: String, color: Color) {
    Card(
        modifier = modifier, 
        colors = CardDefaults.cardColors(containerColor = color.copy(alpha = 0.1f)), 
        border = BorderStroke(1.dp, color.copy(alpha = 0.3f))
    ) {
        Column(
            modifier = Modifier
                .padding(12.dp)
                .fillMaxWidth(), 
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(label, fontSize = 10.sp, color = Color.Gray)
            Text(value, fontSize = 20.sp, fontWeight = FontWeight.ExtraBold, color = color)
        }
    }
}
