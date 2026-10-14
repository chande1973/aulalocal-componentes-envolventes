package com.example.ui.screens

import android.content.Intent
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.data.AttendanceRecord
import com.example.data.LocalSection
import com.example.data.SyncedStudent
import com.example.data.UserProfile
import com.example.data.model.UnitData
import com.example.ui.MainViewModel
import com.example.ui.components.FileExplorerDialog
import com.example.ui.components.ReportViewerDialog
import com.example.ui.components.ResetAppDialog
import com.example.ui.components.StudentDetailAndRecuperativoDialog
import com.example.ui.screens.tabs.*
import com.example.ui.theme.*
import com.example.ui.viewmodels.TeacherViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.emptyFlow

@Composable
fun TeacherDashboardScreen(mainViewModel: MainViewModel, teacherViewModel: TeacherViewModel) {
    val teacherProfile by mainViewModel.userProfile.collectAsState()
    val sections by teacherViewModel.sections.collectAsState()
    val units by mainViewModel.unitsFlow.collectAsState()
    val isServerRunning by teacherViewModel.isServerRunningState.collectAsState()
    val liveSection by teacherViewModel.liveSelectedSection.collectAsState()
    val liveActiveUnit by teacherViewModel.liveActiveUnitIndex.collectAsState()
    val enabledReads by teacherViewModel.enabledReadUnits.collectAsState()
    val enabledExams by teacherViewModel.enabledExamUnits.collectAsState()
    val enabledNotebooks by teacherViewModel.enabledNotebookUnits.collectAsState()
    val generatedOtp by teacherViewModel.generatedOtpDisplay.collectAsState()
    val localIp by mainViewModel.localIpAddress.collectAsState()

    var syncedStudents by remember { mutableStateOf<List<SyncedStudent>>(emptyList()) }
    var allConnectedStudents by remember { mutableStateOf<List<SyncedStudent>>(emptyList()) }

    LaunchedEffect(teacherViewModel) {
        teacherViewModel.syncedStudents.collect { syncedStudents = it }
    }
    LaunchedEffect(teacherViewModel) {
        teacherViewModel.allConnectedStudents.collect { allConnectedStudents = it }
    }

    val currentThemeVariant by mainViewModel.currentThemeVariant.collectAsState()

    TeacherDashboardContent(
        teacherProfile = teacherProfile,
        sections = sections,
        units = units,
        isServerRunning = isServerRunning,
        liveSection = liveSection,
        liveActiveUnit = liveActiveUnit,
        enabledReads = enabledReads,
        enabledExams = enabledExams,
        enabledNotebooks = enabledNotebooks,
        generatedOtp = generatedOtp,
        localIp = localIp,
        syncedStudents = syncedStudents,
        allConnectedStudents = allConnectedStudents,
        currentThemeVariant = currentThemeVariant,
        onToggleTheme = { mainViewModel.toggleThemeVariant() },
        uiEventsFlow = teacherViewModel.uiEvents,
        onStartClassServer = { teacherViewModel.startClassServer(it) },
        onIncrementTotalClasses = { teacherViewModel.incrementTotalClasses() },
        onDismissOtpDisplay = { teacherViewModel.dismissOtpDisplay() },
        onShareFile = { teacherViewModel.shareFile(it) },
        onSharePrivateFile = { cardId, uri -> teacherViewModel.sharePrivateFile(cardId, uri) },
        onShareFullCoursePack = { uri -> teacherViewModel.shareFullCoursePack(uri) },
        getStudentAttendanceHistory = { teacherViewModel.getStudentAttendanceHistory(it) },
        onEnableRecuperativo = { cardId, uIdx -> teacherViewModel.enableRecuperativoForStudent(cardId, uIdx) },
        onEnableNotebookRecuperativo = { cardId, uIdx -> teacherViewModel.enableNotebookRecuperativo(cardId, uIdx) },
        onAuthorizeProfileEdit = { teacherViewModel.authorizeStudentProfileEdit(it) },
        onClearStudentDoubt = { teacherViewModel.clearStudentDoubt(it) },
        onSendPrivateMessage = { cardId, msg -> teacherViewModel.sendPrivateMessage(cardId, msg) },
        onRefreshCourse = { mainViewModel.refreshCurrentCourse() },
        onUpdateActiveUnit = { teacherViewModel.updateActiveUnit(it) },
        onUpdateTeacherUnitToggle = { idx, state, isExam -> teacherViewModel.updateTeacherUnitToggle(idx, state, isExam) },
        onUpdateTeacherNotebookToggle = { idx, state -> teacherViewModel.updateTeacherNotebookToggle(idx, state) },
        onAddSection = { name, weeks -> teacherViewModel.addSection(name, weeks, {}, {}) },
        onSelectSection = { teacherViewModel.selectSection(it) },
        onDeleteSection = { teacherViewModel.deleteSection(it) },
        onSendGlobalMessage = { teacherViewModel.sendGlobalMessage(it) },
        onUpdateManualAttendance = { id, stat -> teacherViewModel.updateManualAttendance(id, stat) },
        onGenerateAcademicReport = { ctx, size -> teacherViewModel.generateAcademicReport(ctx, size) },
        onGenerateAttendanceReport = { ctx -> teacherViewModel.generateAttendanceReport(ctx) },
        onGenerateDetailedMasterReport = { ctx, size -> teacherViewModel.generateDetailedMasterReport(ctx, size) },
        onGenerateAllAcademicReports = { ctx, size -> teacherViewModel.generateAllAcademicReports(ctx, size) },
        onResetApp = { mainViewModel.resetApp(it) },
        onInjectTestData = { teacherViewModel.injectTestData(it) }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TeacherDashboardContent(
    teacherProfile: UserProfile?,
    sections: List<LocalSection>,
    units: List<UnitData>,
    isServerRunning: Boolean,
    liveSection: String,
    liveActiveUnit: Int,
    enabledReads: String,
    enabledExams: String,
    enabledNotebooks: String,
    generatedOtp: String?,
    localIp: String,
    syncedStudents: List<SyncedStudent>,
    allConnectedStudents: List<SyncedStudent>,
    uiEventsFlow: Flow<String>? = null,
    onStartClassServer: (String) -> Unit = {},
    onIncrementTotalClasses: () -> Unit = {},
    onDismissOtpDisplay: () -> Unit = {},
    onShareFile: (android.net.Uri) -> Unit = {},
    onSharePrivateFile: (String, android.net.Uri) -> Unit = { _, _ -> },
    onShareFullCoursePack: (android.net.Uri) -> Unit = {},
    getStudentAttendanceHistory: (String) -> Flow<List<AttendanceRecord>> = { emptyFlow() },
    onEnableRecuperativo: (String, Int) -> Unit = { _, _ -> },
    onEnableNotebookRecuperativo: (String, Int) -> Unit = { _, _ -> },
    onAuthorizeProfileEdit: (String) -> Unit = {},
    onClearStudentDoubt: (String) -> Unit = {},
    currentThemeVariant: com.example.ui.theme.ThemeVariant = com.example.ui.theme.ThemeVariant.TEAL,
    onToggleTheme: () -> Unit = {},
    onSendPrivateMessage: (String, String) -> Unit = { _, _ -> },
    onRefreshCourse: () -> Unit = {},
    onUpdateActiveUnit: (Int) -> Unit = {},
    onUpdateTeacherUnitToggle: (Int, Boolean, Boolean) -> Unit = { _, _, _ -> },
    onUpdateTeacherNotebookToggle: (Int, Boolean) -> Unit = { _, _ -> },
    onAddSection: (String, Int) -> Unit = { _, _ -> },
    onSelectSection: (String) -> Unit = {},
    onDeleteSection: (Int) -> Unit = {},
    onSendGlobalMessage: (String) -> Unit = {},
    onUpdateManualAttendance: (String, String?) -> Unit = { _, _ -> },
    onGenerateAcademicReport: (android.content.Context, Int) -> Unit = { _, _ -> },
    onGenerateAttendanceReport: (android.content.Context) -> Unit = {},
    onGenerateDetailedMasterReport: (android.content.Context, Int) -> Unit = { _, _ -> },
    onGenerateAllAcademicReports: (android.content.Context, Int) -> Unit = { _, _ -> },
    onResetApp: (android.content.Context) -> Unit = {},
    onInjectTestData: (Int) -> Unit = {}
) {
    val context = LocalContext.current
    var activeTab by remember { mutableIntStateOf(0) }

    var showFinishClassDialog by remember { mutableStateOf(false) }
    var showResetAppDialog by remember { mutableStateOf(false) }
    var showLogViewer by remember { mutableStateOf(false) }
    var showReportViewer by remember { mutableStateOf(false) }
    var showFileExplorer by remember { mutableStateOf(false) }
    var selectedStudentDoubt by remember { mutableStateOf<SyncedStudent?>(null) }
    var selectedStudentHistory by remember { mutableStateOf<SyncedStudent?>(null) }
    var studentToMessage by remember { mutableStateOf<SyncedStudent?>(null) }
    var studentToAttach by remember { mutableStateOf<SyncedStudent?>(null) }
    var studentToEnroll by remember { mutableStateOf<SyncedStudent?>(null) }
    var privateMsgText by remember { mutableStateOf("") }

    // Pickers
    val filePicker = rememberLauncherForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri -> uri?.let { onShareFile(it) } }

    val privateFilePicker = rememberLauncherForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri ->
        uri?.let { uriVal ->
            studentToAttach?.let { student ->
                onSharePrivateFile(student.cardId, uriVal)
                studentToAttach = null
            }
        }
    }

    val zipFolderPicker = rememberLauncherForActivityResult(
        ActivityResultContracts.OpenDocumentTree()
    ) { uri ->
        uri?.let {
            context.contentResolver.takePersistableUriPermission(it, Intent.FLAG_GRANT_READ_URI_PERMISSION)
            onShareFullCoursePack(it)
        }
    }

    var showReportSuccessDialog by remember { mutableStateOf<String?>(null) }

    // Observe UI Events
    LaunchedEffect(uiEventsFlow) {
        uiEventsFlow?.collectLatest { event ->
            if (event.contains("✅")) {
                showReportSuccessDialog = event
            } else {
                Toast.makeText(context, event, Toast.LENGTH_SHORT).show()
            }
        }
    }

    if (showReportSuccessDialog != null) {
        AlertDialog(
            onDismissRequest = { showReportSuccessDialog = null },
            title = { Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.CheckCircle, null, tint = StatusSuccess)
                Spacer(Modifier.width(8.dp))
                Text("Reporte Generado")
            }},
            text = { 
                Column {
                    Text("El archivo se guardó correctamente en tu teléfono:", fontSize = 12.sp)
                    Spacer(Modifier.height(8.dp))
                    Surface(
                        color = Color.Black.copy(alpha = 0.2f),
                        shape = RoundedCornerShape(4.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = showReportSuccessDialog!!.replace("✅ ", ""),
                            modifier = Modifier.padding(12.dp),
                            fontWeight = FontWeight.Bold,
                            color = AccentCyan,
                            fontSize = 13.sp
                        )
                    }
                    Spacer(Modifier.height(8.dp))
                    Text("Puedes encontrarlo usando la app 'Archivos' en la carpeta Descargas.", fontSize = 11.sp, color = Color.Gray)
                }
            },
            confirmButton = {
                Button(onClick = { showReportSuccessDialog = null }) { Text("ENTENDIDO") }
            }
        )
    }

    if (generatedOtp != null) {
        val parts = generatedOtp.split("|")
        val pin = parts.getOrNull(0) ?: ""
        val studentName = parts.getOrNull(1) ?: "Alumno"
        AlertDialog(
            onDismissRequest = { onDismissOtpDisplay() },
            title = { Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.LockOpen, null, tint = AccentAmber)
                Spacer(Modifier.width(8.dp))
                Text("🔑 PIN OTP Generado y Enviado")
            }},
            text = {
                Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
                    Text("Se ha generado un PIN temporal de un solo uso para $studentName:", fontSize = 12.sp, color = Color.LightGray, textAlign = TextAlign.Center)
                    Spacer(Modifier.height(16.dp))
                    Surface(
                        color = AccentAmber.copy(alpha = 0.2f),
                        shape = RoundedCornerShape(16.dp),
                        border = BorderStroke(2.dp, AccentAmber)
                    ) {
                        Text(
                            text = pin,
                            modifier = Modifier.padding(horizontal = 32.dp, vertical = 16.dp),
                            fontWeight = FontWeight.Black,
                            fontSize = 40.sp,
                            color = AccentAmber,
                            letterSpacing = 4.sp
                        )
                    }
                    Spacer(Modifier.height(16.dp))
                    Text("El alumno ya recibió la notificación con esta clave en su dispositivo. Este PIN expirará tras su primer uso.", fontSize = 11.sp, color = Color.Gray, textAlign = TextAlign.Center)
                }
            },
            confirmButton = {
                Button(onClick = { onDismissOtpDisplay() }, colors = ButtonDefaults.buttonColors(containerColor = AccentAmber)) {
                    Text("ENTENDIDO", color = Color.Black, fontWeight = FontWeight.Bold)
                }
            }
        )
    }

    // Diálogos
    if (showLogViewer) LogViewerDialog(onDismiss = { showLogViewer = false })
    if (showReportViewer) ReportViewerDialog(onDismiss = { showReportViewer = false })
    if (showFileExplorer) {
        FileExplorerDialog(
            folderUri = teacherProfile?.materialsFolderUri?.takeIf { it.isNotEmpty() }?.let { android.net.Uri.parse(it) },
            onFileSelected = { uri ->
                try {
                    val intent = Intent(Intent.ACTION_VIEW).apply {
                        setDataAndType(uri, context.contentResolver.getType(uri) ?: "*/*")
                        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                    }
                    context.startActivity(Intent.createChooser(intent, "Abrir archivo con..."))
                } catch (e: Exception) {}
            },
            onDismiss = { showFileExplorer = false }
        )
    }
    
    if (selectedStudentHistory != null) {
        val student = selectedStudentHistory!!
        StudentDetailAndRecuperativoDialog(
            student = student,
            units = units,
            historyFlow = getStudentAttendanceHistory(student.cardId),
            onEnableRecuperativo = { cardId, uIdx -> onEnableRecuperativo(cardId, uIdx) },
            onEnableNotebookRecuperativo = { cardId, uIdx -> onEnableNotebookRecuperativo(cardId, uIdx) },
            onAuthorizeProfileEdit = { cardId -> onAuthorizeProfileEdit(cardId) },
            onClearDoubt = { cardId -> onClearStudentDoubt(cardId) },
            onDismiss = { selectedStudentHistory = null }
        )
    }

    if (showFinishClassDialog) {
        AlertDialog(
            onDismissRequest = { showFinishClassDialog = false },
            title = { Text("Finalizar Clase") },
            text = { Text("¿Deseas cerrar la clase actual? Se guardará la asistencia de todos los alumnos.") },
            confirmButton = {
                Button(onClick = { onIncrementTotalClasses(); showFinishClassDialog = false }, colors = ButtonDefaults.buttonColors(containerColor = StatusError)) {
                    Text("FINALIZAR")
                }
            },
            dismissButton = { TextButton(onClick = { showFinishClassDialog = false }) { Text("CANCELAR") } }
        )
    }

    if (selectedStudentDoubt != null) {
        AlertDialog(
            onDismissRequest = { selectedStudentDoubt = null },
            title = { Text("Duda de ${selectedStudentDoubt?.firstName}") },
            text = { Text(selectedStudentDoubt?.doubtMessage ?: "Sin detalle") },
            confirmButton = {
                Button(onClick = { onClearStudentDoubt(selectedStudentDoubt!!.cardId); selectedStudentDoubt = null }) {
                    Text("RESUELTA")
                }
            }
        )
    }

    if (studentToMessage != null) {
        AlertDialog(
            onDismissRequest = { studentToMessage = null },
            title = { Text("Mensaje a ${studentToMessage?.firstName}") },
            text = { OutlinedTextField(value = privateMsgText, onValueChange = { privateMsgText = it }, label = { Text("Escribe el mensaje...") }, modifier = Modifier.fillMaxWidth()) },
            confirmButton = {
                Button(onClick = {
                    if (privateMsgText.isNotBlank()) {
                        onSendPrivateMessage(studentToMessage!!.cardId, privateMsgText)
                        privateMsgText = ""; studentToMessage = null
                    }
                }) { Text("ENVIAR") }
            }
        )
    }

    Scaffold(
        topBar = {
            Column {
                TopAppBar(
                    title = { 
                        Text(
                            text = "Servidor", 
                            fontWeight = FontWeight.ExtraBold,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        ) 
                    },
                    actions = {
                        if (isServerRunning) {
                            Button(onClick = { showFinishClassDialog = true }, colors = ButtonDefaults.buttonColors(containerColor = StatusError), shape = RoundedCornerShape(8.dp)) {
                                Icon(Icons.Default.Stop, null); Spacer(Modifier.width(4.dp)); Text("FINALIZAR")
                            }
                        } else {
                            Button(onClick = { onStartClassServer(liveSection) }, enabled = liveSection.isNotEmpty(), colors = ButtonDefaults.buttonColors(containerColor = StatusSuccess), shape = RoundedCornerShape(8.dp)) {
                                Icon(Icons.Default.PlayArrow, null); Spacer(Modifier.width(4.dp)); Text("INICIAR CLASE")
                            }
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(containerColor = CosmicDarkSurface)
                )
                
                if (isServerRunning) {
                    Surface(
                        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 4.dp),
                        color = StatusSuccess.copy(alpha = 0.15f),
                        shape = RoundedCornerShape(8.dp),
                        border = BorderStroke(1.dp, StatusSuccess.copy(alpha = 0.3f))
                    ) {
                        Row(Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Podcasts, null, tint = StatusSuccess, modifier = Modifier.size(20.dp))
                            Spacer(Modifier.width(12.dp))
                            Column {
                                Text("CLASE EN VIVO: $liveSection", fontSize = 12.sp, color = Color.White, fontWeight = FontWeight.Black)
                                Text("IP del Profesor: $localIp", fontSize = 13.sp, color = StatusSuccess, fontWeight = FontWeight.ExtraBold)
                            }
                        }
                    }
                } else if (liveSection.isNotEmpty()) {
                    Surface(
                        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 4.dp),
                        color = AccentCyan.copy(alpha = 0.15f),
                        shape = RoundedCornerShape(8.dp),
                        border = BorderStroke(1.dp, AccentCyan.copy(alpha = 0.3f))
                    ) {
                        Row(Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Info, null, tint = AccentCyan, modifier = Modifier.size(16.dp))
                            Spacer(Modifier.width(8.dp))
                            Text("Aula '$liveSection' lista para iniciar.", fontSize = 11.sp, color = Color.White)
                        }
                    }
                }
            }
        },
        bottomBar = {
            NavigationBar(
                containerColor = CosmicDarkSurface,
                contentColor = Color.White
            ) {
                val navColors = NavigationBarItemDefaults.colors(
                    selectedIconColor = AccentCyan,
                    selectedTextColor = AccentCyan,
                    unselectedIconColor = Color.Gray,
                    unselectedTextColor = Color.Gray,
                    indicatorColor = AccentCyan.copy(alpha = 0.2f)
                )
                NavigationBarItem(selected = activeTab == 0, onClick = { activeTab = 0 }, icon = { Icon(Icons.AutoMirrored.Filled.List, null) }, label = { Text("Temas", fontSize = 10.sp, maxLines = 1, softWrap = false, overflow = TextOverflow.Ellipsis) }, colors = navColors)
                NavigationBarItem(selected = activeTab == 1, onClick = { activeTab = 1 }, icon = { Icon(Icons.Default.Groups, null) }, label = { Text("Aula", fontSize = 10.sp, maxLines = 1, softWrap = false, overflow = TextOverflow.Ellipsis) }, colors = navColors)
                NavigationBarItem(selected = activeTab == 2, onClick = { activeTab = 2 }, icon = { Icon(Icons.Default.HowToReg, null) }, label = { Text("Asistencia", fontSize = 10.sp, maxLines = 1, softWrap = false, overflow = TextOverflow.Ellipsis) }, colors = navColors)
                NavigationBarItem(selected = activeTab == 3, onClick = { activeTab = 3 }, icon = { Icon(Icons.Default.Assessment, null) }, label = { Text("Métricas", fontSize = 10.sp, maxLines = 1, softWrap = false, overflow = TextOverflow.Ellipsis) }, colors = navColors)
                NavigationBarItem(selected = activeTab == 4, onClick = { activeTab = 4 }, icon = { Icon(Icons.Default.Settings, null) }, label = { Text("Sistema", fontSize = 10.sp, maxLines = 1, softWrap = false, overflow = TextOverflow.Ellipsis) }, colors = navColors)
            }
        }
    ) { padding ->
        Column(Modifier.padding(padding).fillMaxSize().padding(16.dp)) {
            val pendingDoubts = syncedStudents.filter { it.hasDoubt }
            if (pendingDoubts.isNotEmpty()) {
                Card(
                    modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp).clickable {
                        selectedStudentDoubt = pendingDoubts.first()
                    },
                    colors = CardDefaults.cardColors(containerColor = AccentAmber.copy(alpha = 0.2f)),
                    border = BorderStroke(2.dp, AccentAmber),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Row(Modifier.padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
                        Text("💬", fontSize = 22.sp)
                        Spacer(Modifier.width(12.dp))
                        Column(Modifier.weight(1f)) {
                            Text(
                                text = "¡NUEVA DUDA DE ESTUDIANTE (${pendingDoubts.size})!",
                                color = AccentAmber,
                                fontWeight = FontWeight.ExtraBold,
                                fontSize = 12.sp
                            )
                            Text(
                                text = "${pendingDoubts.first().lastName}, ${pendingDoubts.first().firstName}: \"${pendingDoubts.first().doubtMessage.take(30)}...\"",
                                color = Color.White,
                                fontSize = 11.sp,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                        Button(
                            onClick = { selectedStudentDoubt = pendingDoubts.first() },
                            colors = ButtonDefaults.buttonColors(containerColor = AccentAmber),
                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp)
                        ) {
                            Text("VER", color = Color.Black, fontWeight = FontWeight.Bold, fontSize = 11.sp)
                        }
                    }
                }
            }

            when (activeTab) {
                0 -> TeacherUnitsTab(
                    units = units, liveSection = liveSection, liveActiveUnit = liveActiveUnit,
                    enabledReads = enabledReads, enabledExams = enabledExams, enabledNotebooks = enabledNotebooks, isServerRunning = isServerRunning,
                    onRefreshCourse = onRefreshCourse,
                    onOpenExplorer = { showFileExplorer = true },
                    onActivateUnit = onUpdateActiveUnit,
                    onToggleRead = { idx, state -> onUpdateTeacherUnitToggle(idx, state, false) },
                    onToggleExam = { idx, state -> onUpdateTeacherUnitToggle(idx, state, true) },
                    onToggleNotebook = { idx, state -> onUpdateTeacherNotebookToggle(idx, state) },
                    onGoToAula = { activeTab = 1 }
                )
                1 -> TeacherAulaTab(
                    isServerRunning = isServerRunning, liveSection = liveSection, liveActiveUnit = liveActiveUnit,
                    sections = sections,
                    allConnectedStudents = allConnectedStudents,
                    onAddSection = onAddSection,
                    onSelectSection = onSelectSection,
                    onDeleteSection = onDeleteSection,
                    onShareFile = { filePicker.launch("*/*") },
                    onShareZip = { zipFolderPicker.launch(null) },
                    onSendGlobalMsg = onSendGlobalMessage,
                    onStudentMessage = { studentToMessage = it },
                    onStudentAttach = { 
                        studentToAttach = it
                        privateFilePicker.launch("*/*") 
                    },
                    onStudentDoubt = { selectedStudentDoubt = it },
                    onStudentEnroll = { studentToEnroll = it }
                )
                2 -> TeacherAttendanceTab(
                    syncedStudents = syncedStudents,
                    onUpdateAttendance = onUpdateManualAttendance
                )
                3 -> TeacherMetricsTab(
                    liveSection = liveSection, sections = sections, syncedStudents = syncedStudents,
                    units = units,
                    onGenerateAcademicReport = { onGenerateAcademicReport(context, units.size) },
                    onGenerateAttendanceReport = { onGenerateAttendanceReport(context) },
                    onStudentClick = { selectedStudentHistory = it }
                )
                4 -> TeacherCloseTab(
                    currentThemeVariant = currentThemeVariant,
                    onToggleTheme = onToggleTheme,
                    onGenerateAcademicReport = { onGenerateAcademicReport(context, units.size) },
                    onGenerateAttendanceReport = { onGenerateAttendanceReport(context) },
                    onGenerateDetailedMasterReport = { onGenerateDetailedMasterReport(context, units.size) },
                    onOpenReportViewer = { showReportViewer = true },
                    onViewLogs = { showLogViewer = true },
                    onShareLogs = { com.example.data.AppLogger.shareLogFile(context) },
                    onResetApp = { showResetAppDialog = true },
                    onInjectData = onInjectTestData,
                    onGenerateAllReports = { onGenerateAllAcademicReports(context, units.size) }
                )
            }
        }
    }
    
    // Diálogo de Reset (Fuera del Scaffold)
    ResetAppDialog(
        show = showResetAppDialog,
        onDismiss = { showResetAppDialog = false },
        onConfirm = onResetApp,
        context = context
    )
}

@Preview(showBackground = true)
@Composable
fun TeacherDashboardScreenPreview() {
    MyApplicationTheme {
        TeacherDashboardContent(
            teacherProfile = null,
            sections = listOf(
                LocalSection(id = 1, name = "Sección A - Informática")
            ),
            units = emptyList(),
            isServerRunning = true,
            liveSection = "Sección A - Informática",
            liveActiveUnit = 1,
            enabledReads = "1",
            enabledExams = "",
            enabledNotebooks = "",
            generatedOtp = null,
            localIp = "192.168.1.100",
            syncedStudents = emptyList(),
            allConnectedStudents = emptyList()
        )
    }
}
