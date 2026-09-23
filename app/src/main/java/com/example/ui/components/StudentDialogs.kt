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
import com.example.ui.viewmodels.StudentViewModel
import com.example.ui.theme.AccentCyan
import com.example.ui.theme.AccentTeal
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Arrangement

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
                        Text("Descargando: ${(downloadProgress * 100).toInt()}%", fontSize = 11.sp, fontWeight = FontWeight.Bold)
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
