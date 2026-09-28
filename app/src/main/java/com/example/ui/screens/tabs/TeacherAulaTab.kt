package com.example.ui.screens.tabs

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.data.LocalSection
import com.example.data.SyncedStudent
import com.example.data.LocalNetworkManager
import com.example.ui.components.StudentRowTeacher
import com.example.ui.theme.*
import org.json.JSONArray
import org.json.JSONObject

@Composable
fun TeacherAulaTab(
    isServerRunning: Boolean,
    liveSection: String,
    liveActiveUnit: Int = 1,
    sections: List<LocalSection>,
    allConnectedStudents: List<SyncedStudent>,
    onAddSection: (String, Int) -> Unit,
    onSelectSection: (String) -> Unit,
    onDeleteSection: (Int) -> Unit,
    onShareFile: () -> Unit,
    onShareZip: () -> Unit,
    onSendGlobalMsg: (String) -> Unit,
    onStudentMessage: (SyncedStudent) -> Unit,
    onStudentAttach: (SyncedStudent) -> Unit,
    onStudentDoubt: (SyncedStudent) -> Unit,
    onStudentEnroll: (SyncedStudent) -> Unit
) {
    var globalMsg by remember { mutableStateOf("") }

    LazyColumn(Modifier.fillMaxSize()) {
        item {
            Text(stringResource(R.string.label_aula_management), style = MaterialTheme.typography.titleLarge, color = Color.White)
            Text(stringResource(R.string.desc_aula_management), fontSize = 12.sp, color = Color.Gray)
            Spacer(Modifier.height(16.dp))
        }

        if (!isServerRunning) {
            item {
                Card(Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = CosmicDarkSurface)) {
                    Column(Modifier.padding(16.dp)) {
                        Text(stringResource(R.string.label_add_section), fontWeight = FontWeight.Bold, color = Color.White)
                        Spacer(Modifier.height(8.dp))
                        var tempSectionName by remember { mutableStateOf("") }
                        var tempWeeks by remember { mutableStateOf("36") }
                        Column {
                            Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                                OutlinedTextField(
                                    value = tempSectionName, 
                                    onValueChange = { tempSectionName = it }, 
                                    placeholder = { Text(stringResource(R.string.placeholder_section_name)) }, 
                                    modifier = Modifier.weight(1f), 
                                    singleLine = true
                                )
                                Spacer(Modifier.width(8.dp))
                                OutlinedTextField(
                                    value = tempWeeks,
                                    onValueChange = { if (it.all { c -> c.isDigit() }) tempWeeks = it },
                                    label = { Text("Semanas", fontSize = 10.sp) },
                                    modifier = Modifier.width(80.dp),
                                    singleLine = true,
                                    textStyle = androidx.compose.ui.text.TextStyle(fontSize = 12.sp)
                                )
                                Spacer(Modifier.width(8.dp))
                                IconButton(onClick = { 
                                    val weeks = tempWeeks.toIntOrNull() ?: 36
                                    onAddSection(tempSectionName, weeks)
                                    tempSectionName = "" 
                                }) {
                                    Icon(Icons.Default.AddCircle, null, tint = AccentTeal)
                                }
                            }
                            Text("Indica el total de semanas del año escolar (normalmente 36)", fontSize = 10.sp, color = Color.Gray, modifier = Modifier.padding(top = 4.dp))
                        }
                    }
                }
                Spacer(Modifier.height(16.dp))
                Text(stringResource(R.string.label_my_sections), fontWeight = FontWeight.Bold, color = Color.White)
                Spacer(Modifier.height(8.dp))
            }

            items(sections) { section ->
                val isSelected = liveSection == section.name
                Card(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp).clickable { onSelectSection(section.name) },
                    colors = CardDefaults.cardColors(containerColor = if (isSelected) AccentTeal.copy(alpha = 0.1f) else Color.Transparent),
                    border = if (isSelected) BorderStroke(1.dp, AccentTeal) else BorderStroke(1.dp, Color.DarkGray)
                ) {
                    Row(Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                        RadioButton(isSelected, { onSelectSection(section.name) })
                        Column(Modifier.weight(1f)) {
                            Text(section.name, color = Color.White, fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal)
                            val weeksText = "Clases dadas: ${section.totalClasses} | Semanas meta: ${section.plannedClasses}"
                            Text(weeksText, fontSize = 10.sp, color = Color.Gray)
                        }
                        IconButton(onClick = { onDeleteSection(section.id) }) {
                            Icon(Icons.Default.Delete, null, tint = StatusError.copy(alpha = 0.7f), modifier = Modifier.size(20.dp))
                        }
                    }
                }
            }
        } else {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp),
                    colors = CardDefaults.cardColors(containerColor = AccentTeal),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Row(Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Class, null, tint = Color.White, modifier = Modifier.size(32.dp))
                        Spacer(Modifier.width(16.dp))
                        Column {
                            Text(
                                text = "ESTÁS EN CLASE CON:", 
                                fontSize = 12.sp, 
                                color = Color.White, 
                                fontWeight = FontWeight.Black,
                                letterSpacing = 1.sp
                            )
                            Text(liveSection, style = MaterialTheme.typography.headlineMedium, color = Color.White, fontWeight = FontWeight.ExtraBold)
                        }
                    }
                }

                val officialStudents = allConnectedStudents.filter { LocalNetworkManager.areSectionsEqual(it.sectionName, liveSection) }
                val totalOfficial = officialStudents.size
                var totalClassReadPercentage = 0f
                var readStudentsCount = 0

                officialStudents.forEach { student ->
                    val readsList = try { JSONArray(student.readProgressString) } catch (e: Exception) { JSONArray() }
                    var p = 0
                    for (i in 0 until readsList.length()) {
                        val item = readsList.opt(i)
                        if (item is Int && item == liveActiveUnit) p = 100
                        else if (item is JSONObject && item.optInt("unitIndex") == liveActiveUnit) {
                            p = item.optInt("pct", 0)
                        }
                    }
                    if (p >= 100) readStudentsCount++
                    totalClassReadPercentage += p
                }
                val avgClassReadPct = if (totalOfficial > 0) (totalClassReadPercentage / (totalOfficial * 100f)).coerceIn(0f, 1f) else 0f

                Card(
                    modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp),
                    colors = CardDefaults.cardColors(containerColor = CosmicDarkSurface),
                    shape = RoundedCornerShape(12.dp),
                    border = BorderStroke(1.dp, AccentCyan.copy(alpha = 0.3f))
                ) {
                    Column(Modifier.padding(14.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.weight(1f)
                            ) {
                                Icon(Icons.AutoMirrored.Filled.MenuBook, null, tint = AccentCyan, modifier = Modifier.size(18.dp))
                                Spacer(Modifier.width(8.dp))
                                Text(
                                    text = "Progreso Lectura (U.$liveActiveUnit)",
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White,
                                    fontSize = 13.sp,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                            Spacer(Modifier.width(8.dp))
                            Surface(
                                color = AccentCyan.copy(alpha = 0.15f),
                                shape = RoundedCornerShape(6.dp)
                            ) {
                                Text(
                                    text = "$readStudentsCount / $totalOfficial (${(avgClassReadPct * 100).toInt()}%)",
                                    fontWeight = FontWeight.ExtraBold,
                                    color = AccentCyan,
                                    fontSize = 12.sp,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                )
                            }
                        }
                        Spacer(Modifier.height(10.dp))
                        LinearProgressIndicator(
                            progress = { avgClassReadPct },
                            modifier = Modifier.fillMaxWidth().height(8.dp).clip(RoundedCornerShape(4.dp)),
                            color = AccentCyan,
                            trackColor = Color.DarkGray.copy(alpha = 0.5f)
                        )
                    }
                }
            }
        }

        item {
            Text(stringResource(R.string.label_students_in_section), fontWeight = FontWeight.Bold, color = Color.White)
            if (isServerRunning) {
                Column {
                    Button(onClick = onShareFile, modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp), colors = ButtonDefaults.buttonColors(containerColor = AccentTeal)) {
                        Icon(Icons.Default.AttachFile, null); Spacer(Modifier.width(8.dp)); Text(stringResource(R.string.btn_share_group_file))
                    }
                    Button(onClick = onShareZip, modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp), colors = ButtonDefaults.buttonColors(containerColor = AccentAmber)) {
                        Icon(Icons.Default.Inventory, null); Spacer(Modifier.width(8.dp)); Text(stringResource(R.string.btn_share_zip))
                    }
                }
            }
            Row(Modifier.fillMaxWidth().padding(vertical = 8.dp), verticalAlignment = Alignment.CenterVertically) {
                OutlinedTextField(
                    value = globalMsg, 
                    onValueChange = { globalMsg = it }, 
                    placeholder = { Text(stringResource(R.string.placeholder_global_msg)) }, 
                    modifier = Modifier.weight(1f), 
                    textStyle = androidx.compose.ui.text.TextStyle(fontSize = 12.sp)
                )
                IconButton(onClick = { 
                    if (globalMsg.isNotBlank()) {
                        onSendGlobalMsg(globalMsg)
                        globalMsg = "" 
                    }
                }) { Icon(Icons.AutoMirrored.Filled.Send, null, tint = AccentCyan) }
            }
        }

        item {
            val officialStudents = allConnectedStudents.filter { LocalNetworkManager.areSectionsEqual(it.sectionName, liveSection) }
            val listenerStudents = allConnectedStudents.filter { !LocalNetworkManager.areSectionsEqual(it.sectionName, liveSection) }

            Column {
                Text(stringResource(R.string.label_official_students, officialStudents.size), fontWeight = FontWeight.Bold, color = AccentTeal)
                officialStudents.forEach { student ->
                    StudentRowTeacher(student, false, onStudentMessage, onStudentAttach, onStudentDoubt, onStudentEnroll)
                }
                Spacer(Modifier.height(24.dp))
                Text(stringResource(R.string.label_listener_students, listenerStudents.size), fontWeight = FontWeight.Bold, color = AccentAmber)
                listenerStudents.forEach { student ->
                    StudentRowTeacher(student, true, onStudentMessage, onStudentAttach, onStudentDoubt, onStudentEnroll)
                }
            }
        }
    }
}
