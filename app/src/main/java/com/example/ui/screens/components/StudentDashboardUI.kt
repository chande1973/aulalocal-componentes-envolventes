package com.example.ui.screens.components

import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.data.model.UnitData
import com.example.ui.components.ConnectionDialog
import com.example.ui.components.IncomingFileDialog
import com.example.ui.screens.LogViewerDialog
import com.example.ui.theme.*
import com.example.ui.viewmodels.StudentViewModel
import org.json.JSONObject

@Composable
fun StudentDashboardBanners(
    isListenerMode: Boolean,
    isStudyModeActive: Boolean,
    teacherMsg: String,
    versionUpdateMsg: String? = null,
    classEndedMsg: String? = null,
    isReconnecting: Boolean = false,
    onDismissVersionMsg: () -> Unit = {},
    onDismissClassEndedMsg: () -> Unit = {},
    onConnectClick: () -> Unit
) {
    Column {
        // Banner de Reconexión Inofensiva por Wi-Fi
        if (isReconnecting && classEndedMsg.isNullOrEmpty()) {
            Card(
                modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp),
                colors = CardDefaults.cardColors(containerColor = StatusWarning.copy(alpha = 0.15f)),
                border = BorderStroke(1.dp, StatusWarning.copy(alpha = 0.6f)),
                shape = RoundedCornerShape(12.dp)
            ) {
                Row(Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(18.dp),
                        color = StatusWarning,
                        strokeWidth = 2.dp
                    )
                    Spacer(Modifier.width(12.dp))
                    Column(Modifier.weight(1f)) {
                        Text(
                            text = "CONEXIÓN INESTABLE",
                            color = StatusWarning,
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 11.sp,
                            letterSpacing = 1.sp
                        )
                        Text(
                            text = "Sincronizando con el profesor por Wi-Fi...",
                            color = TextPrimary,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }
        }
        // Banner de Clase Finalizada por el Profesor
        if (!classEndedMsg.isNullOrEmpty()) {
            Card(
                modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp),
                colors = CardDefaults.cardColors(containerColor = StatusSuccess.copy(alpha = 0.15f)),
                border = BorderStroke(1.dp, StatusSuccess.copy(alpha = 0.6f)),
                shape = RoundedCornerShape(12.dp)
            ) {
                Row(Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.CheckCircle, null, tint = StatusSuccess)
                    Spacer(Modifier.width(12.dp))
                    Column(Modifier.weight(1f)) {
                        Text(
                            text = "CLASE FINALIZADA",
                            color = StatusSuccess,
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 11.sp,
                            letterSpacing = 1.sp
                        )
                        Text(
                            text = classEndedMsg,
                            color = TextPrimary,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                    IconButton(onClick = onDismissClassEndedMsg) {
                        Icon(Icons.Default.Close, null, tint = TextSecondary, modifier = Modifier.size(16.dp))
                    }
                }
            }
        }

        // Banner de Actualización de Versión de Curso
        if (!versionUpdateMsg.isNullOrEmpty()) {
            Card(
                modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp),
                colors = CardDefaults.cardColors(containerColor = AccentTeal.copy(alpha = 0.15f)),
                border = BorderStroke(1.dp, AccentTeal.copy(alpha = 0.6f)),
                shape = RoundedCornerShape(12.dp)
            ) {
                Row(Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.NewReleases, null, tint = AccentTeal)
                    Spacer(Modifier.width(12.dp))
                    Text(versionUpdateMsg, color = TextPrimary, fontSize = 11.sp, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f))
                    IconButton(onClick = onDismissVersionMsg) {
                        Icon(Icons.Default.Close, null, tint = TextSecondary, modifier = Modifier.size(16.dp))
                    }
                }
            }
        }

        // Banner de Oyente
        if (isListenerMode) {
            Card(
                modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp),
                colors = CardDefaults.cardColors(containerColor = StatusError.copy(alpha = 0.1f)),
                border = BorderStroke(1.dp, StatusError.copy(alpha = 0.5f)),
                shape = RoundedCornerShape(12.dp)
            ) {
                Row(Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Info, null, tint = StatusError)
                    Spacer(Modifier.width(12.dp))
                    Text(stringResource(R.string.msg_listener_warning), color = StatusError, fontSize = 11.sp, fontWeight = FontWeight.ExtraBold, letterSpacing = 1.sp)
                }
            }
        }

        // Banner de Modo Offline
        if (isStudyModeActive) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp)
                    .clickable { onConnectClick() },
                colors = CardDefaults.cardColors(containerColor = AccentCyan.copy(alpha = 0.05f)),
                border = BorderStroke(1.dp, AccentCyan.copy(alpha = 0.4f)),
                shape = RoundedCornerShape(12.dp)
            ) {
                Row(
                    modifier = Modifier.padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        Modifier.size(32.dp).background(AccentCyan.copy(alpha = 0.1f), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Default.Wifi, null, tint = AccentCyan, modifier = Modifier.size(18.dp))
                    }
                    Spacer(Modifier.width(12.dp))
                    Column(Modifier.weight(1f)) {
                        Text(
                            text = stringResource(R.string.msg_offline_mode_desc), 
                            color = TextPrimary, 
                            fontSize = 11.sp, 
                            lineHeight = 14.sp
                        )
                        Text(
                            text = stringResource(R.string.msg_tap_to_connect),
                            color = AccentCyan,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Black
                        )
                    }
                    Icon(Icons.Default.ChevronRight, null, tint = AccentCyan)
                }
            }
        }

        // Banner de Mensaje del Profesor
        if (teacherMsg.isNotEmpty()) {
            Card(
                modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp),
                colors = CardDefaults.cardColors(containerColor = AccentAmber.copy(alpha = 0.1f)),
                border = BorderStroke(1.dp, AccentAmber.copy(alpha = 0.4f)),
                shape = RoundedCornerShape(12.dp)
            ) {
                Row(Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Campaign, null, tint = AccentAmber)
                    Spacer(Modifier.width(12.dp))
                    Text("${stringResource(R.string.dialog_private_msg_title)}: $teacherMsg", color = TextPrimary, fontSize = 12.sp, fontWeight = FontWeight.Medium)
                }
            }
        }
    }
}

@Composable
fun StudentDashboardDialogs(
    incomingMsg: String?,
    incomingFile: JSONObject?,
    pendingUnit: Int?,
    units: List<UnitData>,
    showDoubtDialog: Boolean,
    showLogViewer: Boolean,
    showConnectionDialog: Boolean,
    downloadProgress: Float,
    isDownloading: Boolean,
    studentViewModel: StudentViewModel,
    onDoubtDismiss: () -> Unit,
    onLogDismiss: () -> Unit,
    onConnectionDismiss: () -> Unit,
    onFolderLink: () -> Unit,
    onUnitAccepted: (UnitData) -> Unit,
    onDownloadStarted: () -> Unit,
    onDownloadFinished: () -> Unit,
    onProgressUpdate: (Float) -> Unit
) {
    val context = LocalContext.current

    if (incomingMsg != null) {
        AlertDialog(
            onDismissRequest = { studentViewModel.dismissPrivateMessage() },
            title = { Text(stringResource(R.string.dialog_private_msg_title), color = AccentCyan, fontWeight = FontWeight.Bold) },
            text = { Text(incomingMsg, color = Color.White) },
            confirmButton = { Button(onClick = { studentViewModel.dismissPrivateMessage() }) { Text(stringResource(R.string.btn_understood)) } }
        )
    }

    if (incomingFile != null) {
        IncomingFileDialog(
            fileData = incomingFile,
            downloadProgress = downloadProgress,
            isDownloading = isDownloading,
            onDownload = {
                onDownloadStarted()
                studentViewModel.downloadSharedFile(
                    incomingFile.optString("id"),
                    incomingFile.optString("name"),
                    incomingFile.optLong("size"),
                    onProgress = { progress -> onProgressUpdate(progress) },
                    onComplete = {
                        onDownloadFinished()
                        studentViewModel.dismissIncomingFile()
                        if (it != null) Toast.makeText(context, context.getString(R.string.msg_file_saved), Toast.LENGTH_SHORT).show()
                    }
                )
            },
            onDismiss = { studentViewModel.dismissIncomingFile() }
        )
    }

    if (showLogViewer) LogViewerDialog(onDismiss = onLogDismiss)
    
    if (showConnectionDialog) {
        ConnectionDialog(
            studentViewModel = studentViewModel,
            onLinkFolder = onFolderLink,
            unitsEmpty = units.isEmpty(),
            onDismiss = onConnectionDismiss
        )
    }

    if (pendingUnit != null) {
        val targetUnit = units.find { it.index == pendingUnit }
        AlertDialog(
            onDismissRequest = { studentViewModel.dismissUnitUpdate() },
            title = { Text(stringResource(R.string.dialog_new_unit_title)) },
            text = { Text(stringResource(R.string.msg_new_unit_available, targetUnit?.title ?: "Unidad $pendingUnit")) },
            confirmButton = { 
                Button(onClick = { 
                    targetUnit?.let { onUnitAccepted(it) }
                    studentViewModel.acceptUnitUpdate() 
                }) { Text(stringResource(R.string.btn_go_now)) } 
            },
            dismissButton = { TextButton(onClick = { studentViewModel.dismissUnitUpdate() }) { Text(stringResource(R.string.btn_keep_here)) } }
        )
    }

    if (showDoubtDialog) {
        var doubtText by remember { mutableStateOf("") }
        AlertDialog(
            onDismissRequest = onDoubtDismiss,
            title = { Text(stringResource(R.string.dialog_doubt_title, "")) },
            text = { 
                OutlinedTextField(
                    value = doubtText, 
                    onValueChange = { doubtText = it }, 
                    label = { Text(stringResource(R.string.hint_write_doubt)) }, 
                    modifier = Modifier.fillMaxWidth()
                ) 
            },
            confirmButton = { 
                Button(onClick = { 
                    studentViewModel.sendDoubtToTeacher(doubtText)
                    onDoubtDismiss() 
                }) { Text(stringResource(R.string.btn_send)) } 
            }
        )
    }
}
