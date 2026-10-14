package com.example.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BugReport
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Wifi
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.AccentCyan
import com.example.ui.theme.AccentTeal

@Composable
fun StudentSettingsDialog(
    show: Boolean,
    onDismiss: () -> Unit,
    onOpenLogs: () -> Unit,
    onOpenConnection: () -> Unit
) {
    if (show) {
        AlertDialog(
            onDismissRequest = onDismiss,
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Settings, null, tint = AccentCyan)
                    Spacer(Modifier.width(8.dp))
                    Text("Configuración de Estudiante", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                }
            },
            text = {
                Column {
                    OutlinedButton(
                        onClick = {
                            onDismiss()
                            onOpenLogs()
                        },
                        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                        border = BorderStroke(1.dp, AccentCyan)
                    ) {
                        Icon(Icons.Default.BugReport, null, tint = AccentCyan)
                        Spacer(Modifier.width(8.dp))
                        Text("Ver Registro de Consola / Logs (🐛)", fontSize = 12.sp, color = Color.White)
                    }

                    OutlinedButton(
                        onClick = {
                            onDismiss()
                            onOpenConnection()
                        },
                        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)
                    ) {
                        Icon(Icons.Default.Wifi, null, tint = AccentTeal)
                        Spacer(Modifier.width(8.dp))
                        Text("Estado de Conexión en Vivo", fontSize = 12.sp, color = Color.White)
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = onDismiss) {
                    Text("CERRAR")
                }
            }
        )
    }
}
