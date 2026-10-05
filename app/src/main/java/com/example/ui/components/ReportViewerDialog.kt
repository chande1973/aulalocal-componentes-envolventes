package com.example.ui.components

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Environment
import androidx.core.content.FileProvider
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.FolderOpen
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.*
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun ReportViewerDialog(
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    var reportFiles by remember { mutableStateOf(getStoredReportFiles(context)) }
    var selectedFileContent by remember { mutableStateOf<Pair<String, String>?>(null) } // Pair(FileName, Content)

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = CosmicDarkSurface,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.FolderOpen, null, tint = AccentCyan, modifier = Modifier.size(24.dp))
                Spacer(Modifier.width(10.dp))
                Text("Visor de Reportes Guardados", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 16.sp)
            }
        },
        text = {
            if (selectedFileContent != null) {
                // Vista de lectura de archivo
                val (fileName, content) = selectedFileContent!!
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(350.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(fileName, color = AccentTeal, fontWeight = FontWeight.Bold, fontSize = 12.sp, maxLines = 1)
                        TextButton(onClick = { selectedFileContent = null }) {
                            Text("← Volver a la lista", color = AccentCyan, fontSize = 11.sp)
                        }
                    }
                    Spacer(Modifier.height(8.dp))
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f),
                        colors = CardDefaults.cardColors(containerColor = Color.Black.copy(alpha = 0.4f)),
                        border = BorderStroke(1.dp, Color.Gray.copy(alpha = 0.3f))
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(8.dp)
                                .verticalScroll(rememberScrollState())
                        ) {
                            Text(
                                text = content,
                                color = Color.White,
                                fontSize = 11.sp,
                                fontFamily = FontFamily.Monospace,
                                lineHeight = 14.sp
                            )
                        }
                    }
                }
            } else if (reportFiles.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "No se encontraron reportes o boletas guardadas en la carpeta de la app.",
                        color = Color.Gray,
                        fontSize = 12.sp,
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center
                    )
                }
            } else {
                // Lista de archivos
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(350.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(reportFiles) { file ->
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(containerColor = Color.DarkGray.copy(alpha = 0.3f)),
                            border = BorderStroke(1.dp, Color.Gray.copy(alpha = 0.2f))
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(12.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = file.name,
                                        color = Color.White,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 12.sp,
                                        maxLines = 1
                                    )
                                    val dateStr = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault()).format(Date(file.lastModified()))
                                    val sizeKb = file.length() / 1024
                                    Text(
                                        text = "$dateStr • ${sizeKb} KB",
                                        color = Color.Gray,
                                        fontSize = 10.sp
                                    )
                                }

                                Row {
                                    // Ver
                                    IconButton(
                                        onClick = {
                                            try {
                                                val text = file.readText()
                                                selectedFileContent = Pair(file.name, text)
                                            } catch (e: Exception) {
                                                // ignore
                                            }
                                        },
                                        modifier = Modifier.size(32.dp)
                                    ) {
                                        Icon(Icons.Default.Visibility, "Ver", tint = AccentCyan, modifier = Modifier.size(18.dp))
                                    }
                                    // Compartir
                                    IconButton(
                                        onClick = {
                                            shareReportFile(context, file)
                                        },
                                        modifier = Modifier.size(32.dp)
                                    ) {
                                        Icon(Icons.Default.Share, "Compartir", tint = AccentAmber, modifier = Modifier.size(18.dp))
                                    }
                                    // Eliminar
                                    IconButton(
                                        onClick = {
                                            try {
                                                file.delete()
                                                reportFiles = getStoredReportFiles(context)
                                            } catch (e: Exception) {
                                                // ignore
                                            }
                                        },
                                        modifier = Modifier.size(32.dp)
                                    ) {
                                        Icon(Icons.Default.Delete, "Eliminar", tint = StatusError, modifier = Modifier.size(18.dp))
                                    }
                                }
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = onDismiss,
                colors = ButtonDefaults.buttonColors(containerColor = AccentCyan)
            ) {
                Text("CERRAR", color = Color.Black, fontWeight = FontWeight.Bold)
            }
        }
    )
}

fun getStoredReportFiles(context: Context): List<File> {
    val list = mutableListOf<File>()
    try {
        val downloadsDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
        val reportsDir = File(downloadsDir, "AulaLocal_Reportes")
        if (reportsDir.exists() && reportsDir.isDirectory) {
            reportsDir.listFiles()?.let { files ->
                list.addAll(files.filter { it.isFile && (it.name.endsWith(".txt", true) || it.name.endsWith(".csv", true)) })
            }
        }
        val internalDir = context.getExternalFilesDir(null)
        if (internalDir != null && internalDir.exists() && internalDir.isDirectory) {
            internalDir.listFiles()?.let { files ->
                list.addAll(files.filter { it.isFile && (it.name.endsWith(".txt", true) || it.name.endsWith(".csv", true)) })
            }
        }
    } catch (e: Exception) {
        // ignore
    }
    return list.sortedByDescending { it.lastModified() }
}

fun shareReportFile(context: Context, file: File) {
    try {
        val uri: Uri = FileProvider.getUriForFile(
            context,
            "${context.packageName}.fileprovider",
            file
        )
        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "*/*"
            putExtra(Intent.EXTRA_STREAM, uri)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        context.startActivity(Intent.createChooser(intent, "Compartir reporte con...").apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        })
    } catch (e: Exception) {
        // ignore
    }
}
