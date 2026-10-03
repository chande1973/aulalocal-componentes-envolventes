package com.example.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.LockOpen
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.AttendanceRecord
import com.example.data.SyncedStudent
import com.example.data.model.UnitData
import com.example.ui.theme.*
import kotlinx.coroutines.flow.Flow
import org.json.JSONArray
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun StudentDetailAndRecuperativoDialog(
    student: SyncedStudent,
    units: List<UnitData>,
    historyFlow: Flow<List<AttendanceRecord>>,
    onEnableRecuperativo: (studentCardId: String, unitIndex: Int) -> Unit,
    onAuthorizeProfileEdit: (studentCardId: String) -> Unit = {},
    onClearDoubt: (studentCardId: String) -> Unit = {},
    onDismiss: () -> Unit
) {
    LaunchedEffect(student.cardId) {
        if (student.hasDoubt || student.isProfileUnlockRequested) {
            onClearDoubt(student.cardId)
        }
    }

    val history by historyFlow.collectAsState(initial = emptyList())
    val dateFormat = remember { SimpleDateFormat("dd/MM HH:mm", Locale.getDefault()) }
    val gradesObj = remember(student.examGradesString) {
        try {
            JSONObject(student.examGradesString)
        } catch (e: Exception) {
            JSONObject()
        }
    }
    val notebookGradesObj = remember(student.notebookGradesString) {
        try { JSONObject(student.notebookGradesString) } catch (e: Exception) { JSONObject() }
    }
    val notebookFeedbacksObj = remember(student.notebookFeedbacksString) {
        try { JSONObject(student.notebookFeedbacksString) } catch (e: Exception) { JSONObject() }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { 
            Column {
                Text("${student.lastName}, ${student.firstName}", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                Text("Cédula: ${student.cardId} | Sección: ${student.sectionName}", fontSize = 11.sp, color = Color.Gray)
            }
        },
        text = {
            LazyColumn(Modifier.heightIn(max = 400.dp)) {
                if (student.hasDoubt || student.doubtMessage.isNotEmpty()) {
                    item {
                        Card(
                            modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp),
                            colors = CardDefaults.cardColors(containerColor = AccentAmber.copy(alpha = 0.15f)),
                            border = BorderStroke(1.dp, AccentAmber)
                        ) {
                            Column(Modifier.padding(12.dp)) {
                                Text(
                                    text = if (student.isProfileUnlockRequested) "🔓 SOLICITUD DE DESBLOQUEO DE PERFIL" else "💬 Mensaje / Duda del Alumno:",
                                    fontWeight = FontWeight.ExtraBold,
                                    color = AccentAmber,
                                    fontSize = 11.sp
                                )
                                Spacer(Modifier.height(4.dp))
                                Text(student.doubtMessage, color = Color.White, fontSize = 13.sp)
                            }
                        }
                    }
                }

                item {
                    OutlinedButton(
                        onClick = { 
                            onAuthorizeProfileEdit(student.cardId)
                            onDismiss()
                        },
                        modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp),
                        border = BorderStroke(1.dp, if (student.isProfileUnlockRequested) StatusWarning else AccentAmber)
                    ) {
                        Icon(Icons.Default.LockOpen, null, tint = if (student.isProfileUnlockRequested) StatusWarning else AccentAmber, modifier = Modifier.size(16.dp))
                        Spacer(Modifier.width(8.dp))
                        Text(if (student.isProfileUnlockRequested) "🔓 AUTORIZAR DESBLOQUEO SOLICITADO" else "AUTORIZAR EDICIÓN DE PERFIL", fontSize = 11.sp, color = if (student.isProfileUnlockRequested) StatusWarning else AccentAmber, fontWeight = FontWeight.Bold)
                    }

                    Text("Exámenes y Recuperativos", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = AccentTeal, modifier = Modifier.padding(bottom = 8.dp))
                }
                
                if (units.isEmpty()) {
                    item { Text("No hay unidades cargadas", fontSize = 11.sp, color = Color.Gray) }
                } else {
                    items(units) { unit ->
                        val gradeStr = gradesObj.optString(unit.index.toString(), "")
                        val hasGrade = gradeStr.isNotEmpty()

                        val notebookGradeStr = notebookGradesObj.optString(unit.index.toString(), "")
                        val hasNotebookGrade = notebookGradeStr.isNotEmpty()
                        val notebookFeedback = notebookFeedbacksObj.optString(unit.index.toString(), "")

                        val readsList = remember(student.readProgressString) {
                            try {
                                JSONArray(student.readProgressString)
                            } catch (e: Exception) {
                                JSONArray()
                            }
                        }
                        var readPct = 0
                        for (i in 0 until readsList.length()) {
                            val item = readsList.opt(i)
                            if (item is Int && item == unit.index) readPct = 100
                            else if (item is JSONObject && item.optInt("unitIndex") == unit.index) {
                                readPct = item.optInt("pct", 0)
                            }
                        }

                        val challengesList = remember(student.challengeProgressString) {
                            try { JSONArray(student.challengeProgressString) } catch (e: Exception) { JSONArray() }
                        }
                        var isChallengeDone = false
                        for (i in 0 until challengesList.length()) {
                            if (challengesList.optInt(i) == unit.index) isChallengeDone = true
                        }

                        val readingText = when {
                            readPct >= 100 -> "🟢 Lectura: Completada" + (if (isChallengeDone) " ⭐ Reto Superado" else "")
                            readPct >= 50 -> "🔵 Lectura: En Progreso ($readPct%)"
                            readPct > 0 -> "🟡 Lectura: Iniciada ($readPct%)"
                            else -> "🔴 Lectura: Sin Iniciar (0%)"
                        }
                        val readingColor = when {
                            readPct >= 100 -> StatusSuccess
                            readPct >= 50 -> AccentCyan
                            readPct > 0 -> AccentAmber
                            else -> Color.Gray
                        }

                        Card(
                            modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                            colors = CardDefaults.cardColors(containerColor = CosmicDarkSurface),
                            border = BorderStroke(1.dp, Color.Gray.copy(alpha = 0.3f))
                        ) {
                            Row(
                                modifier = Modifier.padding(10.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Column(Modifier.weight(1f)) {
                                    Text("Unidad ${unit.index}: ${unit.title}", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color.White)
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text(
                                            text = readingText,
                                            fontSize = 10.sp,
                                            color = readingColor,
                                            fontWeight = if (readPct > 0) FontWeight.Bold else FontWeight.Normal
                                        )
                                        Spacer(Modifier.width(8.dp))
                                        Text(
                                            text = if (hasGrade) "|  Examen: $gradeStr / 20" else "|  Examen: Sin presentar",
                                            fontSize = 10.sp,
                                            color = if (hasGrade) AccentCyan else Color.Gray,
                                            fontWeight = if (hasGrade) FontWeight.Bold else FontWeight.Normal
                                        )
                                    }
                                    if (hasNotebookGrade) {
                                        Spacer(Modifier.height(2.dp))
                                        Text(
                                            text = "📓 Cuaderno (OCR): $notebookGradeStr / 20",
                                            fontSize = 10.sp,
                                            color = AccentCyan,
                                            fontWeight = FontWeight.Bold
                                        )
                                        if (notebookFeedback.isNotEmpty()) {
                                            Text(
                                                text = notebookFeedback,
                                                fontSize = 9.sp,
                                                color = Color.LightGray
                                            )
                                        }
                                    }
                                }

                                if (hasGrade) {
                                    Button(
                                        onClick = { onEnableRecuperativo(student.cardId, unit.index) },
                                        colors = ButtonDefaults.buttonColors(containerColor = AccentAmber),
                                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp)
                                    ) {
                                        Text("🔄 Recuperativo", color = Color.Black, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                    }
                                }
                            }
                        }
                    }
                }

                item {
                    Spacer(Modifier.height(16.dp))
                    Text("Historial de Asistencia", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = AccentTeal, modifier = Modifier.padding(bottom = 8.dp))
                }

                if (history.isEmpty()) {
                    item { Text("Sin registros de asistencia", fontSize = 11.sp, color = Color.Gray) }
                } else {
                    items(history) { record ->
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(vertical = 2.dp),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = dateFormat.format(Date(record.date)),
                                color = Color.Gray,
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
        },
        confirmButton = {
            Button(onClick = onDismiss) {
                Text("CERRAR")
            }
        }
    )
}

@Composable
fun TeacherReportDialog(
    sectionName: String,
    totalClasses: Int,
    students: List<SyncedStudent>,
    totalUnits: Int,
    onGenerateCsv: () -> Unit,
    onDismiss: () -> Unit
) {
    val totalStudents = students.size
    val totalAsistenciasSum = students.sumOf { it.attendanceCount }
    val avgAttendance = if (totalStudents > 0 && totalClasses > 0) (totalAsistenciasSum.toFloat() / (totalStudents * totalClasses) * 100).toInt() else 0

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("📊 Reporte Consolidado - $sectionName", fontWeight = FontWeight.Bold, fontSize = 16.sp) },
        text = {
            Column(modifier = Modifier.fillMaxWidth()) {
                Text("Resumen Estadístico del Aula", fontSize = 12.sp, color = Color.Gray)
                Spacer(Modifier.height(12.dp))
                Card(
                    colors = CardDefaults.cardColors(containerColor = CosmicDarkSurface),
                    shape = RoundedCornerShape(12.dp),
                    border = BorderStroke(1.dp, AccentTeal.copy(alpha = 0.3f))
                ) {
                    Column(Modifier.padding(16.dp)) {
                        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("Alumnos Registrados:", color = TextSecondary, fontSize = 13.sp)
                            Text("$totalStudents", color = TextPrimary, fontWeight = FontWeight.Bold)
                        }
                        Spacer(Modifier.height(6.dp))
                        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("Clases Impartidas:", color = TextSecondary, fontSize = 13.sp)
                            Text("$totalClasses", color = TextPrimary, fontWeight = FontWeight.Bold)
                        }
                        Spacer(Modifier.height(6.dp))
                        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("Asistencia Promedio:", color = TextSecondary, fontSize = 13.sp)
                            Text("$avgAttendance%", color = AccentTeal, fontWeight = FontWeight.Black)
                        }
                    }
                }
                Spacer(Modifier.height(16.dp))
                Text("Puedes generar y descargar el acta de calificaciones y asistencia en formato CSV.", fontSize = 11.sp, color = Color.Gray)
            }
        },
        confirmButton = {
            Button(
                onClick = onGenerateCsv,
                colors = ButtonDefaults.buttonColors(containerColor = AccentTeal)
            ) {
                Text("📥 DESCARGAR / COMPARTIR CSV", fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("CERRAR")
            }
        }
    )
}
