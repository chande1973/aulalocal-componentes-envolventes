package com.example.ui.screens.tabs

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BugReport
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Terminal
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
import com.example.ui.theme.AccentAmber
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import com.example.ui.theme.AccentAmber
import com.example.ui.theme.AccentCyan
import com.example.ui.theme.AccentTeal
import com.example.ui.theme.CosmicDarkSurface

@Composable
fun TeacherCloseTab(
    onGenerateAcademicReport: () -> Unit,
    onGenerateAttendanceReport: () -> Unit,
    onGenerateDetailedMasterReport: () -> Unit,
    onOpenReportViewer: () -> Unit,
    onViewLogs: () -> Unit,
    onShareLogs: () -> Unit,
    onResetApp: () -> Unit,
    onInjectData: (Int) -> Unit,
    onGenerateAllReports: () -> Unit
) {
    val scrollState = rememberScrollState()
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
    ) {
        Text(stringResource(R.string.label_close_year_title), style = MaterialTheme.typography.titleLarge, color = Color.White)
        Spacer(Modifier.height(16.dp))

        Card(Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = AccentCyan.copy(alpha = 0.1f))) {
            Column(Modifier.padding(16.dp)) {
                Text("Simulación de Datos (Pruebas)", fontWeight = FontWeight.Bold, color = AccentCyan)
                Text("Inyecta datos ficticios para probar métricas y reportes.", color = Color.White.copy(alpha = 0.7f), fontSize = 11.sp)
                Spacer(Modifier.height(12.dp))
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    val mod = Modifier.weight(1f)
                    Button(onClick = { onInjectData(10) }, modifier = mod, contentPadding = PaddingValues(0.dp)) { Text("10e", fontSize = 12.sp) }
                    Button(onClick = { onInjectData(20) }, modifier = mod, contentPadding = PaddingValues(0.dp)) { Text("20e", fontSize = 12.sp) }
                    Button(onClick = { onInjectData(40) }, modifier = mod, contentPadding = PaddingValues(0.dp)) { Text("40e", fontSize = 12.sp) }
                    Button(onClick = { onInjectData(80) }, modifier = mod, contentPadding = PaddingValues(0.dp)) { Text("80e", fontSize = 12.sp) }
                }
            }
        }

        Spacer(Modifier.height(16.dp))
        Card(Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = Color(0x33FF0000))) {
            Column(Modifier.padding(16.dp)) {
                Text(stringResource(R.string.label_warning_title), fontWeight = FontWeight.Bold, color = Color.Red)
                Text(stringResource(R.string.msg_close_year_warning), color = Color.White, fontSize = 12.sp)
                Spacer(Modifier.height(16.dp))
                
                Button(onClick = onGenerateAcademicReport, modifier = Modifier.fillMaxWidth()) { Text("Exportar Acta Final (Seleccionada)") }
                Spacer(Modifier.height(8.dp))
                Button(onClick = onGenerateDetailedMasterReport, modifier = Modifier.fillMaxWidth(), colors = ButtonDefaults.buttonColors(containerColor = AccentCyan)) {
                    Text("📈 Exportar Reporte Maestro (Asistencia, Examen y OCR)", color = Color.Black, fontWeight = FontWeight.Bold)
                }
                Spacer(Modifier.height(8.dp))
                Button(onClick = onGenerateAllReports, modifier = Modifier.fillMaxWidth(), colors = ButtonDefaults.buttonColors(containerColor = AccentAmber)) {
                    Text("📦 Exportar TODAS las Aulas")
                }
                Spacer(Modifier.height(8.dp))
                Button(onClick = onGenerateAttendanceReport, modifier = Modifier.fillMaxWidth(), colors = ButtonDefaults.buttonColors(containerColor = AccentTeal)) { Text("Exportar Asistencia (Seleccionada)") }
                Spacer(Modifier.height(8.dp))
                Button(onClick = onOpenReportViewer, modifier = Modifier.fillMaxWidth(), colors = ButtonDefaults.buttonColors(containerColor = AccentTeal)) {
                    Text("📂 Ver Reportes y Actas Guardadas", color = Color.White, fontWeight = FontWeight.Bold)
                }
                
                Spacer(Modifier.height(16.dp))
                
                OutlinedButton(onClick = onViewLogs, modifier = Modifier.fillMaxWidth()) {
                    Icon(Icons.Default.Terminal, null); Spacer(Modifier.width(8.dp)); Text("Ver Consola")
                }
                Spacer(Modifier.height(8.dp))
                OutlinedButton(onClick = onShareLogs, modifier = Modifier.fillMaxWidth()) {
                    Icon(Icons.Default.BugReport, null); Spacer(Modifier.width(8.dp)); Text("Enviar Logs")
                }
                
                Spacer(Modifier.height(24.dp))
                Button(onClick = onResetApp, modifier = Modifier.fillMaxWidth(), colors = ButtonDefaults.buttonColors(containerColor = Color.Red)) {
                    Text("BORRAR TODO / RESET", fontWeight = FontWeight.Bold)
                }
            }
        }

        Spacer(Modifier.height(24.dp))
        
        // Sección Informativa de Ubicación de Archivos
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = CosmicDarkSurface),
            border = BorderStroke(1.dp, Color.Gray.copy(alpha = 0.5f))
        ) {
            Row(Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Info, null, tint = AccentCyan, modifier = Modifier.size(24.dp))
                Spacer(Modifier.width(16.dp))
                Column {
                    Text("Ubicación de Reportes", fontWeight = FontWeight.ExtraBold, color = Color.White, fontSize = 14.sp)
                    Text("Carpeta: Descargas/AulaLocal_Reportes", color = AccentCyan, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                    Text("Usa la aplicación 'Archivos' de tu teléfono para ver los CSV.", color = Color.Gray, fontSize = 11.sp)
                }
            }
        }
    }
}
