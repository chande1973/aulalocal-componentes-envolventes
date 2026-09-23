package com.example.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
    onDismiss: () -> Unit
) {
    val history by historyFlow.collectAsState(initial = emptyList())
    val dateFormat = remember { SimpleDateFormat("dd/MM HH:mm", Locale.getDefault()) }
    val gradesObj = remember(student.examGradesString) {
        try {
            JSONObject(student.examGradesString)
        } catch (e: Exception) {
            JSONObject()
        }
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
                item {
                    OutlinedButton(
                        onClick = { onAuthorizeProfileEdit(student.cardId) },
                        modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp),
                        border = BorderStroke(1.dp, AccentAmber)
                    ) {
                        Icon(Icons.Default.LockOpen, null, tint = AccentAmber, modifier = Modifier.size(16.dp))
                        Spacer(Modifier.width(8.dp))
                        Text("AUTORIZAR EDICIÓN DE PERFIL", fontSize = 11.sp, color = AccentAmber, fontWeight = FontWeight.Bold)
                    }

                    Text("Exámenes y Recuperativos", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = AccentTeal, modifier = Modifier.padding(bottom = 8.dp))
                }
                
                if (units.isEmpty()) {
                    item { Text("No hay unidades cargadas", fontSize = 11.sp, color = Color.Gray) }
                } else {
                    items(units) { unit ->
                        val gradeStr = gradesObj.optString(unit.index.toString(), "")
                        val hasGrade = gradeStr.isNotEmpty()

                        val readsList = remember(student.readProgressString) {
                            try {
                                JSONArray(student.readProgressString)
                            } catch (e: Exception) {
                                JSONArray()
                            }
                        }
                        var isUnitRead = false
                        for (i in 0 until readsList.length()) {
                            if (readsList.optInt(i) == unit.index) isUnitRead = true
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
                                            text = if (isUnitRead) "📖 Lectura: Completada" else "⏳ Lectura: Pendiente",
                                            fontSize = 10.sp,
                                            color = if (isUnitRead) StatusSuccess else Color.Gray,
                                            fontWeight = if (isUnitRead) FontWeight.Bold else FontWeight.Normal
                                        )
                                        Spacer(Modifier.width(8.dp))
                                        Text(
                                            text = if (hasGrade) "|  Nota: $gradeStr / 20" else "|  Examen: Sin presentar",
                                            fontSize = 10.sp,
                                            color = if (hasGrade) AccentCyan else Color.Gray,
                                            fontWeight = if (hasGrade) FontWeight.Bold else FontWeight.Normal
                                        )
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
