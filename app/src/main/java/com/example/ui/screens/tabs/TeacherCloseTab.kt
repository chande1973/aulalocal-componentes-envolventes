package com.example.ui.screens.tabs

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BugReport
import androidx.compose.material.icons.filled.DeleteForever
import androidx.compose.material.icons.filled.FolderOpen
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
import com.example.ui.components.CosmicCard
import com.example.ui.components.PrimaryActionButton
import com.example.ui.theme.*

@Composable
fun TeacherCloseTab(
    currentThemeVariant: com.example.ui.theme.ThemeVariant = com.example.ui.theme.ThemeVariant.TEAL,
    onToggleTheme: () -> Unit = {},
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

        // Tarjeta de Selector de Tema Visual
        CosmicCard {
            Column(Modifier.padding(16.dp)) {
                Text(stringResource(R.string.label_theme_title), fontWeight = FontWeight.Bold, color = Color.White)
                Text(stringResource(R.string.label_theme_desc), color = Color.Gray, fontSize = 11.sp)
                Spacer(Modifier.height(12.dp))
                val themeName = if (currentThemeVariant == ThemeVariant.TEAL) {
                    stringResource(R.string.theme_teal)
                } else {
                    stringResource(R.string.theme_indigo)
                }
                Button(
                    onClick = onToggleTheme,
                    colors = ButtonDefaults.buttonColors(containerColor = AccentCyan),
                    modifier = Modifier.fillMaxWidth().height(Dimensions.ButtonHeight),
                    shape = RoundedCornerShape(Dimensions.CornerMedium)
                ) {
                    Text(
                        text = stringResource(R.string.label_theme_btn, themeName),
                        fontWeight = FontWeight.Bold,
                        color = Color.Black,
                        fontSize = 12.sp
                    )
                }
            }
        }

        Spacer(Modifier.height(16.dp))

        // Tarjeta de Simulación
        Card(
            Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = CosmicDarkSurface),
            border = BorderStroke(1.dp, Color.Gray.copy(alpha = 0.2f))
        ) {
            Column(Modifier.padding(16.dp)) {
                Text("Simulación de Datos (Pruebas)", fontWeight = FontWeight.Bold, color = Color.White)
                Text("Inyecta datos ficticios para probar métricas y reportes.", color = Color.Gray, fontSize = 11.sp)
                Spacer(Modifier.height(12.dp))
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    val mod = Modifier.weight(1f)
                    OutlinedButton(onClick = { onInjectData(10) }, modifier = mod, contentPadding = PaddingValues(0.dp), border = BorderStroke(1.dp, Color.Gray.copy(alpha = 0.4f))) { Text("10e", fontSize = 12.sp, color = Color.White) }
                    OutlinedButton(onClick = { onInjectData(20) }, modifier = mod, contentPadding = PaddingValues(0.dp), border = BorderStroke(1.dp, Color.Gray.copy(alpha = 0.4f))) { Text("20e", fontSize = 12.sp, color = Color.White) }
                    OutlinedButton(onClick = { onInjectData(40) }, modifier = mod, contentPadding = PaddingValues(0.dp), border = BorderStroke(1.dp, Color.Gray.copy(alpha = 0.4f))) { Text("40e", fontSize = 12.sp, color = Color.White) }
                    OutlinedButton(onClick = { onInjectData(80) }, modifier = mod, contentPadding = PaddingValues(0.dp), border = BorderStroke(1.dp, Color.Gray.copy(alpha = 0.4f))) { Text("80e", fontSize = 12.sp, color = Color.White) }
                }
            }
        }

        Spacer(Modifier.height(16.dp))

        // Tarjeta Principal de Exportación
        Card(
            Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = CosmicDarkSurface),
            border = BorderStroke(1.dp, Color.Gray.copy(alpha = 0.2f))
        ) {
            Column(Modifier.padding(16.dp)) {
                Text("Exportación de Reportes y Sistema", fontWeight = FontWeight.Bold, color = Color.White)
                Text(stringResource(R.string.msg_close_year_warning), color = Color.Gray, fontSize = 12.sp)
                Spacer(Modifier.height(16.dp))
                
                // Botones de Exportación Unificados en Teal
                PrimaryActionButton(
                    text = "📈 Exportar Reporte Maestro (Asistencia, Examen y OCR)",
                    onClick = onGenerateDetailedMasterReport,
                    containerColor = AccentTeal,
                    contentColor = Color.White
                )
                Spacer(Modifier.height(8.dp))
                
                PrimaryActionButton(
                    text = "📦 Exportar TODAS las Aulas",
                    onClick = onGenerateAllReports,
                    containerColor = AccentTeal,
                    contentColor = Color.White
                )
                Spacer(Modifier.height(8.dp))
                PrimaryActionButton(
                    text = "Exportar Acta Final (Seleccionada)",
                    onClick = onGenerateAcademicReport,
                    containerColor = AccentTeal,
                    contentColor = Color.White
                )
                Spacer(Modifier.height(8.dp))
                PrimaryActionButton(
                    text = "Exportar Asistencia (Seleccionada)",
                    onClick = onGenerateAttendanceReport,
                    containerColor = AccentTeal,
                    contentColor = Color.White
                )
                Spacer(Modifier.height(12.dp))
                
                // Visor de Reportes (Utilitario Neutro)
                OutlinedButton(
                    onClick = onOpenReportViewer,
                    modifier = Modifier.fillMaxWidth().height(50.dp),
                    border = BorderStroke(1.dp, Color.Gray.copy(alpha = 0.4f)),
                    shape = androidx.compose.foundation.shape.RoundedCornerShape(14.dp)
                ) {
                    Icon(Icons.Default.FolderOpen, null, tint = Color.LightGray)
                    Spacer(Modifier.width(8.dp))
                    Text("VER REPORTES Y ACTAS GUARDADAS", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                }
                
                Spacer(Modifier.height(12.dp))
                
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedButton(onClick = onViewLogs, modifier = Modifier.weight(1f).height(48.dp), border = BorderStroke(1.dp, Color.Gray.copy(alpha = 0.4f))) {
                        Icon(Icons.Default.Terminal, null, tint = Color.LightGray); Spacer(Modifier.width(6.dp)); Text("Consola", color = Color.LightGray, fontSize = 11.sp)
                    }
                    OutlinedButton(onClick = onShareLogs, modifier = Modifier.weight(1f).height(48.dp), border = BorderStroke(1.dp, Color.Gray.copy(alpha = 0.4f))) {
                        Icon(Icons.Default.BugReport, null, tint = Color.LightGray); Spacer(Modifier.width(6.dp)); Text("Logs", color = Color.LightGray, fontSize = 11.sp)
                    }
                }
            }
        }

        Spacer(Modifier.height(16.dp))

        // Zona Crítica de Borrado (ÚNICO ELEMENTO ROJO)
        Card(
            Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = StatusError.copy(alpha = 0.08f)),
            border = BorderStroke(1.dp, StatusError.copy(alpha = 0.3f))
        ) {
            Column(Modifier.padding(16.dp)) {
                Text("Zona de Administración Crítica", fontWeight = FontWeight.Bold, color = StatusError, fontSize = 13.sp)
                Text("El reseteo de fábrica borrará todas las secciones, perfiles y registros de forma permanente.", color = Color.LightGray, fontSize = 11.sp)
                Spacer(Modifier.height(12.dp))
                PrimaryActionButton(
                    text = "BORRAR TODO / RESET",
                    onClick = onResetApp,
                    containerColor = StatusError,
                    contentColor = Color.White,
                    icon = Icons.Default.DeleteForever
                )
            }
        }

        Spacer(Modifier.height(24.dp))
        
        // Sección Informativa
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = CosmicDarkSurface),
            border = BorderStroke(1.dp, Color.Gray.copy(alpha = 0.3f))
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
