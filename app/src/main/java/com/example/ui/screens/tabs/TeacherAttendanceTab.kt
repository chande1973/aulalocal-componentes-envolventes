package com.example.ui.screens.tabs

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.data.SyncedStudent
import com.example.data.LocalNetworkManager
import com.example.ui.theme.*

@Composable
fun TeacherAttendanceTab(
    syncedStudents: List<SyncedStudent>,
    onUpdateAttendance: (String, String?) -> Unit
) {
    Column(Modifier.fillMaxSize()) {
        Text(stringResource(R.string.label_attendance_live), style = MaterialTheme.typography.titleLarge, color = Color.White)
        Text(stringResource(R.string.desc_attendance_live), fontSize = 12.sp, color = Color.Gray)
        Spacer(Modifier.height(16.dp))
        
        val totalCheckpoints = LocalNetworkManager.getSessionCheckpoints()
        
        LazyColumn(Modifier.fillMaxSize()) {
            items(syncedStudents) { student ->
                val isOnline = (System.currentTimeMillis() - student.lastSeen) < 15000
                val autoPresent = student.checkpointCount >= totalCheckpoints * 0.6
                val currentStatus = student.manualAttendanceStatus ?: if (autoPresent) "Presente" else "Ausente"

                Card(Modifier.fillMaxWidth().padding(vertical = 4.dp), colors = CardDefaults.cardColors(containerColor = CosmicDarkSurface)) {
                    Column(Modifier.padding(12.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text("${student.lastName}, ${student.firstName}", color = Color.White, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f))
                            Text(if (isOnline) "ONLINE" else "OFFLINE", color = if (isOnline) Color.Green else Color.Gray, fontSize = 10.sp)
                        }
                        Spacer(Modifier.height(8.dp))
                        Row(horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
                            Text("Estatus: $currentStatus", color = if (currentStatus == "Presente") Color.Green else AccentAmber, fontSize = 12.sp)
                            Row {
                                FilterChip(
                                    selected = student.manualAttendanceStatus == "Presente",
                                    onClick = { onUpdateAttendance(student.cardId, if (student.manualAttendanceStatus == "Presente") null else "Presente") },
                                    label = { Text("P", fontSize = 10.sp) }
                                )
                                Spacer(Modifier.width(4.dp))
                                FilterChip(
                                    selected = student.manualAttendanceStatus == "Ausente",
                                    onClick = { onUpdateAttendance(student.cardId, if (student.manualAttendanceStatus == "Ausente") null else "Ausente") },
                                    label = { Text("A", fontSize = 10.sp) }
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
