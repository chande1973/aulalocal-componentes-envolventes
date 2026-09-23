package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DeleteSweep
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.data.AppLogger
import kotlinx.coroutines.flow.collectLatest

@Composable
fun LogViewerDialog(onDismiss: () -> Unit) {
    val logs = remember { mutableStateListOf<String>() }
    val listState = rememberLazyListState()

    LaunchedEffect(Unit) {
        // Cargar logs existentes
        logs.addAll(AppLogger.getMemoryLogs())
        
        // Escuchar nuevos logs
        AppLogger.logFlow.collectLatest { newLog ->
            logs.add(newLog)
            if (logs.size > 200) logs.removeAt(0)
            // Auto-scroll al final
            listState.animateScrollToItem(logs.size)
        }
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = Color(0xFF121212) // Fondo oscuro tipo terminal
        ) {
            Column(Modifier.fillMaxSize()) {
                // Header
                Row(
                    Modifier.fillMaxWidth().background(Color(0xFF1E1E1E)).padding(8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("CONSOLA DE DIAGNÓSTICO", color = Color.Cyan, fontWeight = androidx.compose.ui.text.font.FontWeight.Bold, fontSize = 14.sp)
                    Row {
                        val context = androidx.compose.ui.platform.LocalContext.current
                        IconButton(onClick = { AppLogger.shareLogFile(context) }) {
                            Icon(Icons.Default.Share, "Enviar", tint = Color.Cyan)
                        }
                        IconButton(onClick = { logs.clear(); AppLogger.clearLogs() }) {
                            Icon(Icons.Default.DeleteSweep, "Limpiar", tint = Color.Gray)
                        }
                        IconButton(onClick = onDismiss) {
                            Icon(Icons.Default.Close, "Cerrar", tint = Color.White)
                        }
                    }
                }

                // Log List
                LazyColumn(
                    modifier = Modifier.weight(1f).fillMaxWidth().padding(horizontal = 8.dp),
                    state = listState,
                    contentPadding = PaddingValues(vertical = 8.dp)
                ) {
                    items(logs) { log ->
                        val color = when {
                            log.contains("[ERROR]") -> Color(0xFFFF5252)
                            log.contains("[WARN]") -> Color(0xFFFFB74D)
                            log.contains("[INFO]") -> Color(0xFF4FC3F7)
                            else -> Color(0xFF81C784)
                        }
                        Text(
                            text = log,
                            color = color,
                            fontSize = 11.sp,
                            fontFamily = FontFamily.Monospace,
                            modifier = Modifier.padding(vertical = 2.dp)
                        )
                    }
                }
                
                // Footer
                Box(Modifier.fillMaxWidth().background(Color(0xFF1E1E1E)).padding(8.dp)) {
                    Text("Mostrando últimos 200 eventos", color = Color.Gray, fontSize = 10.sp)
                }
            }
        }
    }
}
