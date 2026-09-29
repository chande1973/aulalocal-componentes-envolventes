package com.example.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Wifi
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.shape.RoundedCornerShape
import com.example.ui.viewmodels.StudentViewModel
import com.example.ui.theme.*
import java.util.Locale
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.ui.text.TextStyle
import com.example.data.UnitProgress
import com.example.data.UserProfile

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ConnectionDialog(
    studentViewModel: StudentViewModel,
    onLinkFolder: () -> Unit,
    unitsEmpty: Boolean,
    onDismiss: () -> Unit
) {
    val isDiscovering by studentViewModel.isDiscovering.collectAsState()
    val detectedIp by studentViewModel.detectedTeacherIp.collectAsState()
    val detectedSection by studentViewModel.detectedSectionName.collectAsState()
    val manualIp by studentViewModel.manualServerIp.collectAsState()

    var showManualIp by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        studentViewModel.discoverActiveTeacher()
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Conexión con el Aula") },
        text = {
            Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
                if (isDiscovering) {
                    CircularProgressIndicator(color = AccentCyan)
                    Spacer(Modifier.height(16.dp))
                    Text("Buscando profesor...", color = Color.Gray)
                } else if (detectedIp != null) {
                    Icon(Icons.Default.CheckCircle, null, tint = Color.Green, modifier = Modifier.size(48.dp))
                    Spacer(Modifier.height(8.dp))
                    Text("¡Aula Detectada!", fontWeight = FontWeight.Bold)
                    Text(detectedSection ?: "Aula", fontSize = 20.sp)
                    Spacer(Modifier.height(16.dp))
                    Button(onClick = { studentViewModel.performStudentCheckIn { onDismiss() } }, modifier = Modifier.fillMaxWidth()) {
                        Text("ENTRAR A CLASE")
                    }
                } else {
                    Icon(Icons.Default.Wifi, null, modifier = Modifier.size(64.dp), tint = AccentCyan)
                    Spacer(Modifier.height(16.dp))
                    Text("No se detectó profesor automáticamente.", fontSize = 12.sp)
                    Spacer(Modifier.height(16.dp))
                    Button(onClick = { studentViewModel.discoverActiveTeacher() }, modifier = Modifier.fillMaxWidth()) {
                        Text("REINTENTAR")
                    }
                    
                    if (unitsEmpty) {
                        Spacer(Modifier.height(16.dp))
                        Text("¿No tienes internet? Vincula tus materiales manualmente:", fontSize = 11.sp, color = Color.Gray, textAlign = androidx.compose.ui.text.style.TextAlign.Center)
                        OutlinedButton(onClick = onLinkFolder, modifier = Modifier.fillMaxWidth().padding(top = 8.dp)) {
                            Text("VINCULAR CARPETA")
                        }
                    }
                    
                    if (showManualIp) {
                        Spacer(Modifier.height(8.dp))
                        OutlinedTextField(
                            value = manualIp,
                            onValueChange = { studentViewModel.updateManualServerIp(it) },
                            label = { Text("IP") },
                            modifier = Modifier.fillMaxWidth()
                        )
                        Button(onClick = { 
                            studentViewModel.connectManuallyToClass(manualIp, "9999", { studentViewModel.performStudentCheckIn { onDismiss() } }, {}) 
                        }) { Text("CONECTAR") }
                    } else {
                        TextButton(onClick = { showManualIp = true }) { Text("IP MANUAL") }
                    }
                }
            }
        },
        confirmButton = { },
        dismissButton = { TextButton(onClick = onDismiss) { Text("CANCELAR") } }
    )
}

@Composable
fun IncomingFileDialog(
    fileData: org.json.JSONObject,
    downloadProgress: Float,
    isDownloading: Boolean,
    onDownload: () -> Unit,
    onDismiss: () -> Unit
) {
    val fileName = fileData.optString("name")
    val fileSize = fileData.optLong("size")
    val isSystem = fileData.optBoolean("isSystemUpdate", false)

    AlertDialog(
        onDismissRequest = { if (!isDownloading) onDismiss() },
        title = { Text(if (isSystem) "Actualización de Curso" else "Archivo Recibido") },
        text = {
            Column {
                Text(if (isSystem) "El profesor ha enviado una actualización completa del curso." else "El profesor ha compartido un archivo contigo:")
                Text(fileName, fontWeight = FontWeight.Bold, color = AccentTeal)
                Text("${fileSize / 1024} KB", fontSize = 10.sp, color = Color.Gray)
                
                if (isDownloading) {
                    Spacer(Modifier.height(16.dp))
                    val downloadedKb = ((fileSize * downloadProgress) / 1024).toLong()
                    val totalKb = fileSize / 1024
                    LinearProgressIndicator(
                        progress = { downloadProgress },
                        modifier = Modifier.fillMaxWidth().height(8.dp),
                        color = AccentCyan
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text("Descargando: ${(downloadProgress * 100).toInt()}%", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            Text("$downloadedKb KB / $totalKb KB", fontSize = 10.sp, color = Color.Gray)
                        }
                        TextButton(onClick = onDismiss, colors = ButtonDefaults.textButtonColors(contentColor = Color.Red)) {
                            Text("CANCELAR", fontSize = 11.sp)
                        }
                    }
                }
            }
        },
        confirmButton = {
            if (!isDownloading) {
                Button(onClick = onDownload) {
                    Text(if (isSystem) "ACTUALIZAR" else "DESCARGAR")
                }
            }
        },
        dismissButton = {
            if (!isDownloading) {
                TextButton(onClick = onDismiss) {
                    Text("AHORA NO")
                }
            }
        }
    )
}

@Composable
fun StudentReportDialog(
    profile: UserProfile,
    studyProgress: List<UnitProgress>,
    totalClasses: Int,
    onDownloadReport: () -> Unit,
    onDismiss: () -> Unit
) {
    val examGrades = studyProgress.filter { it.examCompleted && it.examGrade != null }
    val avgGrade = if (examGrades.isNotEmpty()) examGrades.sumOf { it.examGrade!!.toDouble() }.toFloat() / examGrades.size else 0f
    val attCount = profile.attendanceCount
    val attPct = if (totalClasses > 0) (attCount.toFloat() / totalClasses * 100).toInt() else 100

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("📄 Boleta Académica Personal", fontWeight = FontWeight.Bold, fontSize = 16.sp) },
        text = {
            Column(modifier = Modifier.fillMaxWidth()) {
                Text("${profile.lastName}, ${profile.firstName} (CI: ${profile.cardId})", fontSize = 12.sp, color = Color.Gray)
                Spacer(Modifier.height(12.dp))
                Card(
                    colors = CardDefaults.cardColors(containerColor = CosmicDarkSurface),
                    shape = RoundedCornerShape(12.dp),
                    border = BorderStroke(1.dp, AccentCyan.copy(alpha = 0.3f))
                ) {
                    Column(Modifier.padding(16.dp)) {
                        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("Promedio de Notas:", color = TextSecondary, fontSize = 13.sp)
                            Text(if (examGrades.isNotEmpty()) "%.2f".format(Locale.US, avgGrade) else "Sin evaluar", color = TextPrimary, fontWeight = FontWeight.Bold)
                        }
                        Spacer(Modifier.height(6.dp))
                        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("Asistencia Global:", color = TextSecondary, fontSize = 13.sp)
                            Text("$attCount de $totalClasses ($attPct%)", color = AccentCyan, fontWeight = FontWeight.Black)
                        }
                        Spacer(Modifier.height(6.dp))
                        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("Unidades Completadas:", color = TextSecondary, fontSize = 13.sp)
                            Text("${studyProgress.count { it.isRead || it.examCompleted }}", color = TextPrimary, fontWeight = FontWeight.Bold)
                        }
                    }
                }
                Spacer(Modifier.height(16.dp))
                Text("Genera tu archivo de boleta personal para compartirlo o guardarlo en tu dispositivo.", fontSize = 11.sp, color = Color.Gray)
            }
        },
        confirmButton = {
            Button(
                onClick = onDownloadReport,
                colors = ButtonDefaults.buttonColors(containerColor = AccentCyan)
            ) {
                Text("📥 DESCARGAR BOLETA TXT", fontWeight = FontWeight.Bold, color = Color.Black)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("CERRAR")
            }
        }
    )
}

@Composable
fun StudentMessageDialog(
    title: String,
    message: String,
    onReply: (replyText: String) -> Unit,
    onDismiss: () -> Unit
) {
    var replyText by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Wifi, null, tint = AccentCyan)
                Spacer(Modifier.width(8.dp))
                Text(title, fontWeight = FontWeight.Bold, fontSize = 16.sp)
            }
        },
        text = {
            Column(modifier = Modifier.fillMaxWidth()) {
                Card(
                    colors = CardDefaults.cardColors(containerColor = CosmicDarkSurface),
                    shape = RoundedCornerShape(12.dp),
                    border = BorderStroke(1.dp, AccentCyan.copy(alpha = 0.4f)),
                    modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp)
                ) {
                    Text(
                        text = message,
                        color = Color.White,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium,
                        modifier = Modifier.padding(16.dp)
                    )
                }

                OutlinedTextField(
                    value = replyText,
                    onValueChange = { replyText = it },
                    label = { Text("Escribe tu respuesta al profesor...") },
                    modifier = Modifier.fillMaxWidth(),
                    textStyle = TextStyle(fontSize = 13.sp)
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (replyText.trim().isNotEmpty()) {
                        onReply(replyText.trim())
                        onDismiss()
                    }
                },
                enabled = replyText.trim().isNotEmpty(),
                colors = ButtonDefaults.buttonColors(containerColor = AccentCyan)
            ) {
                Text("✉️ RESPONDER", fontWeight = FontWeight.Bold, color = Color.Black)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("CERRAR")
            }
        }
    )
}
