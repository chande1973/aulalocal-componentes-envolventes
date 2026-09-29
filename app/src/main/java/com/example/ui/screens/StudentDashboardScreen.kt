package com.example.ui.screens

import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.R
import com.example.data.AppLogger
import com.example.data.model.UnitData
import com.example.ui.MainViewModel
import com.example.ui.components.ConnectionStatusIcon
import com.example.ui.components.ExamNotificationCard
import com.example.ui.components.StudentReportDialog
import com.example.domain.ReportManager
import com.example.ui.components.StudentMessageDialog
import com.example.ui.screens.components.StudentDashboardBanners
import com.example.ui.screens.components.StudentDashboardDialogs
import com.example.ui.screens.models.StudentTab
import com.example.ui.screens.tabs.StudentClassTab
import com.example.ui.screens.tabs.StudentInboxTab
import com.example.ui.screens.tabs.StudentProfileTab
import com.example.ui.screens.tabs.StudentStatusTab
import com.example.ui.theme.*
import com.example.ui.viewmodels.StudentViewModel
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StudentDashboardScreen(mainViewModel: MainViewModel, studentViewModel: StudentViewModel) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val studentProfile by mainViewModel.userProfile.collectAsStateWithLifecycle()
    val units by mainViewModel.unitsFlow.collectAsStateWithLifecycle()
    val enabledReads by studentViewModel.enabledReadUnits.collectAsStateWithLifecycle()
    val enabledExams by studentViewModel.enabledExamUnits.collectAsStateWithLifecycle()
    val teacherMsg by studentViewModel.teacherMessageState.collectAsStateWithLifecycle()
    val totalClasses by studentViewModel.totalClassesFromTeacher.collectAsStateWithLifecycle()
    val pendingUnit by studentViewModel.pendingUnitUpdate.collectAsStateWithLifecycle()
    val pendingExam by studentViewModel.pendingExamUpdate.collectAsStateWithLifecycle()
    val isSyncing by studentViewModel.isSyncing.collectAsStateWithLifecycle()
    val isStudyModeActive by studentViewModel.isStudyModeActive.collectAsStateWithLifecycle()
    val isListenerMode by studentViewModel.isListenerMode.collectAsStateWithLifecycle()
    val incomingMsg by studentViewModel.incomingPrivateMessage.collectAsStateWithLifecycle()
    val incomingFile by studentViewModel.incomingFile.collectAsStateWithLifecycle()
    val updateState by studentViewModel.updateState.collectAsStateWithLifecycle()

    val studyProgress by mainViewModel.studyProgress.collectAsStateWithLifecycle()
    val inboxMessages by studentViewModel.inboxMessages.collectAsStateWithLifecycle()
    val activeUnitIndex by studentViewModel.activeUnitIndexStudent.collectAsStateWithLifecycle()
    
    var studyingUnit by remember { mutableStateOf<UnitData?>(null) }
    var isTakingExam by remember { mutableStateOf(false) }
    var activeTab by remember { mutableStateOf<StudentTab>(StudentTab.Class) }
    var showDoubtDialog by remember { mutableStateOf(false) }
    var showLogViewer by remember { mutableStateOf(false) }
    var showConnectionDialog by remember { mutableStateOf(false) }
    var showSettingsDialog by remember { mutableStateOf(false) }
    var showStudentReportDialog by remember { mutableStateOf(false) }
    var showMessageDialog by remember { mutableStateOf(false) }

    LaunchedEffect(incomingMsg) {
        if (!incomingMsg.isNullOrEmpty()) {
            showMessageDialog = true
        }
    }

    val isProfileEditUnlocked by studentViewModel.isProfileEditUnlocked.collectAsStateWithLifecycle()

    var downloadProgress by remember { mutableFloatStateOf(0f) }
    var isDownloading by remember { mutableStateOf(false) }

    val isLocked by remember(studentProfile) {
        derivedStateOf { studentProfile?.isLocked == true }
    }

    // Folder linking logic
    val folderLauncher = androidx.activity.compose.rememberLauncherForActivityResult(
        androidx.activity.result.contract.ActivityResultContracts.OpenDocumentTree()
    ) { uri ->
        uri?.let {
            context.contentResolver.takePersistableUriPermission(it, android.content.Intent.FLAG_GRANT_READ_URI_PERMISSION)
            scope.launch {
                val result = mainViewModel.validateAndCacheCoursePack(it)
                if (result is com.example.data.model.LoadResult.Success || result is com.example.data.model.LoadResult.PartialSuccess) {
                    val profile = mainViewModel.userProfile.value
                    if (profile != null) {
                        mainViewModel.confirmSetupAndSaveProfile(it, profile)
                        Toast.makeText(context, context.getString(R.string.msg_materials_linked_success), Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }
    }

    // Manejo de botón físico "Atrás"
    BackHandler(enabled = studyingUnit != null) {
        if (isTakingExam) isTakingExam = false
        else studyingUnit = null
    }

    // Eventos de UI
    LaunchedEffect(studentViewModel) {
        studentViewModel.uiEvents.collectLatest { event ->
            Toast.makeText(context, event, Toast.LENGTH_SHORT).show()
        }
    }

    // Autocorrección de estado: Si la unidad tenía isRead=true pero isChallengeDone=false, reiniciar a estado inicial
    LaunchedEffect(studyingUnit) {
        studyingUnit?.let { unit ->
            val p = studyProgress.find { it.unitIndex == unit.index }
            if (p != null && p.isRead && !p.isChallengeDone) {
                studentViewModel.resetUnitReadingProgress(unit.index)
            }
        }
    }

    // Dialogs Management
    StudentDashboardDialogs(
        incomingMsg = incomingMsg,
        incomingFile = incomingFile,
        pendingUnit = pendingUnit,
        units = units,
        showDoubtDialog = showDoubtDialog,
        showLogViewer = showLogViewer,
        showConnectionDialog = showConnectionDialog,
        downloadProgress = downloadProgress,
        isDownloading = isDownloading,
        studentViewModel = studentViewModel,
        onDoubtDismiss = { showDoubtDialog = false },
        onLogDismiss = { showLogViewer = false },
        onConnectionDismiss = { showConnectionDialog = false },
        onFolderLink = { folderLauncher.launch(null) },
        onUnitAccepted = { studyingUnit = it },
        onDownloadStarted = { isDownloading = true },
        onDownloadFinished = { isDownloading = false },
        onProgressUpdate = { downloadProgress = it }
    )

    Box(Modifier.fillMaxSize()) {
        Scaffold(
            topBar = { 
                TopAppBar(
                    title = { 
                        Text(
                            text = studyingUnit?.title ?: stringResource(R.string.app_name),
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 18.sp,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        ) 
                    },
                    navigationIcon = {
                        if (studyingUnit != null) {
                            IconButton(onClick = { 
                                if (isTakingExam) isTakingExam = false
                                else studyingUnit = null 
                            }) {
                                Icon(Icons.AutoMirrored.Filled.ArrowBack, null, tint = Color.White)
                            }
                        }
                    },
                    actions = {
                        if (studyingUnit == null) {
                            ConnectionStatusIcon(
                                isLocked = isLocked,
                                isSyncing = isSyncing,
                                isListenerMode = isListenerMode,
                                onClick = { showConnectionDialog = true }
                            )
                            IconButton(onClick = { showSettingsDialog = true }) {
                                Icon(Icons.Default.Settings, contentDescription = "Configuración", tint = AccentCyan)
                            }
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(containerColor = CosmicDarkSurface)
                ) 
            },
            bottomBar = {
                if (studyingUnit == null) {
                    NavigationBar {
                        StudentTab.items.forEach { tab ->
                            NavigationBarItem(
                                selected = activeTab == tab,
                                onClick = { activeTab = tab },
                                icon = { Icon(tab.icon, null) },
                                label = { Text(stringResource(tab.labelRes)) }
                            )
                        }
                    }
                }
            },
            floatingActionButton = {
                // Caso 1: En clase activa (isLocked) - FABs de Dudas + Presencia
                if (activeTab == StudentTab.Class && isLocked && isSyncing && studyingUnit == null) {
                    Column(horizontalAlignment = Alignment.End) {
                        SmallFloatingActionButton(
                            onClick = { showDoubtDialog = true },
                            containerColor = CosmicDarkSurface,
                            contentColor = AccentCyan,
                            modifier = Modifier.padding(bottom = 8.dp)
                        ) {
                            Icon(Icons.Default.QuestionAnswer, stringResource(R.string.label_doubt))
                        }
                        ExtendedFloatingActionButton(
                            onClick = { studentViewModel.markPresenceInClass() }, 
                            containerColor = AccentAmber,
                            icon = { Text("🙋‍♂️", fontSize = 24.sp) },
                            text = { Text(stringResource(R.string.btn_present), fontWeight = FontWeight.Bold, color = Color.Black) }
                        )
                    }
                }
                // Caso 2: Sin clase activa - FAB para buscar nueva clase
                else if (activeTab == StudentTab.Class && !isLocked && studyingUnit == null) {
                    ExtendedFloatingActionButton(
                        onClick = { showConnectionDialog = true },
                        containerColor = AccentCyan,
                        icon = { Icon(Icons.Default.Wifi, null, tint = Color.Black) },
                        text = { Text("BUSCAR CLASE", fontWeight = FontWeight.Black, color = Color.Black) },
                        modifier = Modifier.padding(bottom = 16.dp)
                    )
                }
            }
        ) { padding ->
            Column(Modifier.padding(padding).fillMaxSize().padding(16.dp)) {
                if (activeTab == StudentTab.Class && studyingUnit == null) {
                    val versionUpdateMsg by mainViewModel.courseVersionUpdateMessage.collectAsStateWithLifecycle()
                    val classEndedNotice by studentViewModel.classEndedNotice.collectAsStateWithLifecycle()
                    StudentDashboardBanners(
                        isListenerMode = isListenerMode,
                        isStudyModeActive = isStudyModeActive,
                        teacherMsg = teacherMsg,
                        versionUpdateMsg = versionUpdateMsg,
                        classEndedMsg = classEndedNotice,
                        isReconnecting = isLocked && !isSyncing,
                        onDismissVersionMsg = { mainViewModel.dismissVersionNotification() },
                        onDismissClassEndedMsg = { studentViewModel.dismissClassEndedNotice() },
                        onConnectClick = { showConnectionDialog = true },
                        onJoinNewClass = { showConnectionDialog = true },
                        updateState = updateState,
                        onRetryDownload = { studentViewModel.retryDownload() }
                    )
                }

                if (studyingUnit == null) {
                    when (activeTab) {
                        StudentTab.Class -> StudentClassTab(
                            profile = studentProfile,
                            units = units, isLocked = isLocked, isSyncing = isSyncing,
                            isStudyModeActive = isStudyModeActive, enabledReads = enabledReads,
                            studyProgress = studyProgress,
                            onUnitClick = { studyingUnit = it },
                            onConnectClick = { showConnectionDialog = true },
                            onLinkFolderClick = { folderLauncher.launch(null) }
                        )
                        StudentTab.Inbox -> StudentInboxTab(
                            messages = inboxMessages,
                            onClearInbox = { studentViewModel.clearBuzon() }
                        )
                        StudentTab.Status -> {
                            val attendanceHistory by studentViewModel.getStudentAttendanceHistory(studentProfile?.cardId ?: "").collectAsStateWithLifecycle(emptyList())
                            StudentStatusTab(
                                profile = studentProfile,
                                studyProgress = studyProgress,
                                units = units,
                                totalClasses = totalClasses,
                                isLocked = isLocked,
                                attendanceHistory = attendanceHistory,
                                onAbandonClass = { studentViewModel.unlockAndDisconnectStudent() },
                                onViewConsole = { showLogViewer = true },
                                onReportBug = { AppLogger.shareLogFile(context) },
                                onDownloadReport = { showStudentReportDialog = true }
                            )
                        }
                        StudentTab.Profile -> StudentProfileTab(
                            profile = studentProfile,
                            isUnlocked = isProfileEditUnlocked,
                            onRequestUnlock = {
                                studentViewModel.sendDoubtToTeacher("Solicitud de desbloqueo de perfil para corrección de datos")
                                Toast.makeText(context, "Solicitud enviada al profesor por Wi-Fi", Toast.LENGTH_SHORT).show()
                            },
                            onPinUnlockSuccess = {
                                studentViewModel.unlockProfileEdit()
                                Toast.makeText(context, "Perfil desbloqueado correctamente", Toast.LENGTH_SHORT).show()
                            },
                            onSaveProfile = { updated ->
                                mainViewModel.updateUserProfile(updated)
                                studentViewModel.lockProfileEdit()
                                Toast.makeText(context, "✅ Perfil actualizado correctamente", Toast.LENGTH_SHORT).show()
                            },
                            onExportBackup = {
                                mainViewModel.exportStudentBackup(context) { file ->
                                    if (file != null) {
                                        Toast.makeText(context, "✅ Respaldo guardado en: ${file.absolutePath}", Toast.LENGTH_LONG).show()
                                    } else {
                                        Toast.makeText(context, "❌ Error al generar respaldo", Toast.LENGTH_SHORT).show()
                                    }
                                }
                            }
                        )
                    }
                } else {
                    // Contenido de la Unidad
                    val currentUnit = studyingUnit
                    if (currentUnit != null) {
                        val videoUri = remember(currentUnit) { mainViewModel.getMediaUri(currentUnit.videoFile) }
                        StudentStudyContent(
                            unit = currentUnit,
                            videoUri = videoUri,
                            isLocked = isLocked,
                            enabledExams = enabledExams,
                            studyProgress = studyProgress,
                            onTakeExam = { isTakingExam = true },
                            onMarkRead = { studentViewModel.markUnitAsRead(it) },
                            onMarkChallengeDone = { studentViewModel.markChallengeAsDone(it) },
                            onUpdateReadingProgress = { idx, pct, scroll -> studentViewModel.updateReadingProgress(idx, pct, scroll) },
                            onRequestRecuperativo = {
                                studentViewModel.sendDoubtToTeacher("Solicitud de recuperativo para Unidad ${currentUnit.index}")
                                Toast.makeText(context, "Solicitud de recuperativo enviada al profesor por Wi-Fi", Toast.LENGTH_SHORT).show()
                            }
                        )
                    }
                }
            }
        }

        // CAPAS SUPERIORES (Overlays)
        val currentStudyingUnit = studyingUnit
        if (isTakingExam && currentStudyingUnit != null) {
            ExamScreen(unit = currentStudyingUnit, studentViewModel = studentViewModel, onDismiss = { isTakingExam = false })
        }

        // Alerta de Examen
        if (pendingExam == true && !isTakingExam) {
            ExamNotificationCard(
                unitIndex = activeUnitIndex,
                unitTitle = units.find { it.index == activeUnitIndex }?.title ?: "",
                onAccept = {
                    units.find { it.index == activeUnitIndex }?.let { studyingUnit = it }
                    isTakingExam = true
                    studentViewModel.acceptExamUpdate()
                },
                onDismiss = { studentViewModel.dismissExamUpdate() }
            )
        }

        if (showSettingsDialog) {
            AlertDialog(
                onDismissRequest = { showSettingsDialog = false },
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
                                showSettingsDialog = false
                                showLogViewer = true
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
                                showSettingsDialog = false
                                showConnectionDialog = true
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
                    TextButton(onClick = { showSettingsDialog = false }) {
                        Text("CERRAR")
                    }
                }
            )
        }
    }

    if (showStudentReportDialog && studentProfile != null) {
        val attendanceHistoryForDialog by studentViewModel.getStudentAttendanceHistory(studentProfile!!.cardId).collectAsStateWithLifecycle(emptyList())
        StudentReportDialog(
            profile = studentProfile!!,
            studyProgress = studyProgress,
            totalClasses = totalClasses,
            onDownloadReport = {
                val file = ReportManager.generateStudentPersonalReport(context, studentProfile!!, studyProgress, attendanceHistoryForDialog, totalClasses)
                if (file != null) {
                    Toast.makeText(context, "✅ Boleta guardada en: ${file.absolutePath}", Toast.LENGTH_LONG).show()
                } else {
                    Toast.makeText(context, "❌ Error al generar boleta", Toast.LENGTH_SHORT).show()
                }
                showStudentReportDialog = false
            },
            onDismiss = { showStudentReportDialog = false }
        )
    }

    if (showMessageDialog && (!incomingMsg.isNullOrEmpty() || teacherMsg.isNotEmpty())) {
        val displayMsg = incomingMsg ?: teacherMsg
        StudentMessageDialog(
            title = if (!incomingMsg.isNullOrEmpty()) "💬 Mensaje del Profesor" else "📢 Aviso de la Clase",
            message = displayMsg,
            onReply = { reply ->
                studentViewModel.sendDoubtToTeacher(reply)
                Toast.makeText(context, "✉️ Respuesta enviada al profesor", Toast.LENGTH_SHORT).show()
                showMessageDialog = false
            },
            onDismiss = { showMessageDialog = false }
        )
    }
}
