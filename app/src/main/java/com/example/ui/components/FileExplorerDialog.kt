package com.example.ui.components

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.OpenInNew
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.FolderZip
import androidx.compose.material.icons.filled.Movie
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.documentfile.provider.DocumentFile
import com.example.data.AppLogger
import com.example.ui.theme.*

data class LocalFileItem(
    val name: String,
    val sizeBytes: Long,
    val isDirectory: Boolean,
    val uri: Uri,
    val extension: String
)

@Composable
fun FileExplorerDialog(
    folderUri: Uri?,
    onFileSelected: (Uri) -> Unit = {},
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    var filesList by remember { mutableStateOf<List<LocalFileItem>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }

    LaunchedEffect(folderUri) {
        isLoading = true
        if (folderUri != null) {
            try {
                val docDir = DocumentFile.fromTreeUri(context, folderUri)
                if (docDir != null && docDir.exists()) {
                    val items = docDir.listFiles().map { file ->
                        val name = file.name ?: "Archivo"
                        val ext = name.substringAfterLast(".", "").lowercase()
                        LocalFileItem(
                            name = name,
                            sizeBytes = file.length(),
                            isDirectory = file.isDirectory,
                            uri = file.uri,
                            extension = ext
                        )
                    }.sortedWith(compareBy({ !it.isDirectory }, { it.name.lowercase() }))
                    filesList = items
                }
            } catch (e: Exception) {
                AppLogger.e("FileExplorerDialog", "Error listing files", e)
            }
        }
        isLoading = false
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Folder, null, tint = AccentCyan)
                Spacer(Modifier.width(8.dp))
                Text("Explorador de Archivos de Curso", fontWeight = FontWeight.Bold, fontSize = 16.sp)
            }
        },
        text = {
            Column(modifier = Modifier.fillMaxWidth().heightIn(max = 400.dp)) {
                if (isLoading) {
                    Box(Modifier.fillMaxWidth().padding(24.dp), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(color = AccentCyan)
                    }
                } else if (filesList.isEmpty()) {
                    Box(Modifier.fillMaxWidth().padding(24.dp), contentAlignment = Alignment.Center) {
                        Text("No se encontraron archivos en la carpeta vinculada.", color = Color.Gray, fontSize = 12.sp)
                    }
                } else {
                    LazyColumn(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        items(filesList) { item ->
                            Card(
                                modifier = Modifier.fillMaxWidth().clickable {
                                    if (!item.isDirectory) {
                                        onFileSelected(item.uri)
                                    }
                                },
                                colors = CardDefaults.cardColors(containerColor = CosmicDarkSurface),
                                shape = RoundedCornerShape(10.dp),
                                border = BorderStroke(1.dp, Color.DarkGray.copy(alpha = 0.5f))
                            ) {
                                Row(
                                    modifier = Modifier.padding(12.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    val icon = when {
                                        item.isDirectory -> Icons.Default.Folder
                                        item.extension in listOf("mp4", "mkv", "avi", "mov") -> Icons.Default.Movie
                                        item.extension == "zip" -> Icons.Default.FolderZip
                                        else -> Icons.Default.Description
                                    }
                                    val iconColor = when {
                                        item.isDirectory -> AccentCyan
                                        item.extension in listOf("mp4", "mkv", "avi", "mov") -> AccentAmber
                                        item.extension == "zip" -> StatusWarning
                                        else -> AccentTeal
                                    }

                                    Icon(icon, null, tint = iconColor, modifier = Modifier.size(20.dp))
                                    Spacer(Modifier.width(12.dp))

                                    Column(Modifier.weight(1f)) {
                                        Text(
                                            text = item.name,
                                            color = Color.White,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 13.sp,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                        if (!item.isDirectory) {
                                            val sizeKb = item.sizeBytes / 1024
                                            val sizeStr = if (sizeKb > 1024) "${"%.1f".format(sizeKb / 1024f)} MB" else "$sizeKb KB"
                                            Text(sizeStr, color = Color.Gray, fontSize = 10.sp)
                                        }
                                    }

                                    if (!item.isDirectory) {
                                        IconButton(
                                            onClick = {
                                                try {
                                                    val intent = Intent(Intent.ACTION_VIEW).apply {
                                                        setDataAndType(item.uri, context.contentResolver.getType(item.uri) ?: "*/*")
                                                        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                                                    }
                                                    context.startActivity(Intent.createChooser(intent, "Abrir archivo con..."))
                                                } catch (e: Exception) {
                                                    Toast.makeText(context, "No se pudo abrir el archivo", Toast.LENGTH_SHORT).show()
                                                }
                                            },
                                            modifier = Modifier.size(28.dp)
                                        ) {
                                            Icon(Icons.AutoMirrored.Filled.OpenInNew, "Abrir", tint = Color.Gray, modifier = Modifier.size(16.dp))
                                        }
                                    }
                                }
                            }
                        }
                    }
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
