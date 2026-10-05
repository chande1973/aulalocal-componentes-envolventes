package com.example.ui.screens

import android.content.Intent
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.data.SyncedStudent
import com.example.ui.MainViewModel
import com.example.ui.components.ReportViewerDialog
import com.example.ui.components.StudentDetailAndRecuperativoDialog
import com.example.ui.screens.tabs.*
import com.example.ui.theme.*
import com.example.ui.viewmodels.TeacherViewModel
import kotlinx.coroutines.flow.collectLatest

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TeacherDashboardScreen(mainViewModel: MainViewModel, teacherViewModel: TeacherViewModel) {
    val context = LocalContext.current
    // Diferir colecciones pesadas para evitar ANR en arranque
    val sections by teacherViewModel.sections.collectAsState()
    val units by mainViewModel.unitsFlow.collectAsState()
    val isServerRunning by teacherViewModel.isServerRunningState.collectAsState()
    val liveSection by teacherViewModel.liveSelectedSection.collectAsState()
    val liveActiveUnit by teacherViewModel.liveActiveUnitIndex.collectAsState()
    val enabledReads by teacherViewModel.enabledReadUnits.collectAsState()
    val enabledExams by teacherViewModel.enabledExamUnits.collectAsState()
    val enabledNotebooks by teacherViewModel.enabledNotebookUnits.collectAsState()
    val generatedOtp by teacherViewModel.generatedOtpDisplay.collectAsState()
    
    // Colecciones pesadas diferidas con valores por defecto
    var syncedStudents by remember { mutableStateOf<List<SyncedStudent>>(emptyList()) }
    var allConnectedStudents by remember { mutableStateOf<List<SyncedStudent>>(emptyList()) }
    
    LaunchedEffect(teacherViewModel) {
        teacherViewModel.syncedStudents.collect { syncedStudents = it }
    }
    LaunchedEffect(teacherViewModel) {
        teacherViewModel.allConnectedStudents.collect { allConnectedStudents = it }
    }
    
    var activeTab by remember { mutableIntStateOf(0) }

    var showFinishClassDialog by remember { mutableStateOf(false) }
    var showResetAppDialog by remember { mutableStateOf(false) }
    var showLogViewer by remember { mutableStateOf(false) }
    var showReportViewer by remember { mutableStateOf(false) }
    var selectedStudentDoubt by remember { mutableStateOf<SyncedStudent?>(null) }
    var selectedStudentHistory by remember { mutableStateOf<SyncedStudent?>(null) }
    var studentToMessage by remember { mutableStateOf<SyncedStudent?>(null) }
    var studentToAttach by remember { mutableStateOf<SyncedStudent?>(null) }
    var studentToEnroll by remember { mutableStateOf<SyncedStudent?>(null) }
    var privateMsgText by remember { mutableStateOf("") }

    // Pickers
    val filePicker = androidx.activity.compose.rememberLauncherForActivityResult(
        androidx.activity.result.contract.ActivityResultContracts.GetContent()
    ) { uri -> uri?.let { teacherViewModel.shareFile(it) } }

    val privateFilePicker = androidx.activity.compose.rememberLauncherForActivityResult(
        androidx.activity.result.contract.ActivityResultContracts.GetContent()
    ) { uri ->
        uri?.let { uriVal ->
            studentToAttach?.let { student ->
                teacherViewModel.sharePrivateFile(student.cardId, uriVal)
                studentToAttach = null
            }
        }
    }

    val zipFolderPicker = rememberLauncherForActivityResult(
        ActivityResultContracts.OpenDocumentTree()
    ) { uri ->
        uri?.let {
            context.contentResolver.takePersistableUriPermission(it, Intent.FLAG_GRANT_READ_URI_PERMISSION)
            teacherViewModel.shareFullCoursePack(it)
        }
    }

    var showReportSuccessDialog by remember { mutableStateOf<String?>(null) }

    // Observe UI Events
    LaunchedEffect(teacherViewModel) {
        teacherViewModel.uiEvents.collectLatest { event ->
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
        val parts = generatedOtp!!.split("|")
        val pin = parts.getOrNull(0) ?: ""
        val studentName = parts.getOrNull(1) ?: "Alumno"
        AlertDialog(
            onDismissRequest = { teacherViewModel.dismissOtpDisplay() },
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
                Button(onClick = { teacherViewModel.dismissOtpDisplay() }, colors = ButtonDefaults.buttonColors(containerColor = AccentAmber)) {
                    Text("ENTENDIDO", color = Color.Black, fontWeight = FontWeight.Bold)
                }
            }
        )
    }

    // Diálogos
    if (showLogViewer) LogViewerDialog(onDismiss = { showLogViewer = false })
    if (showReportViewer) ReportViewerDialog(onDismiss = { showReportViewer = false })
    
    if (selectedStudentHistory != null) {
        val student = selectedStudentHistory!!
        StudentDetailAndRecuperativoDialog(
            student = student,
            units = units,
            historyFlow = teacherViewModel.getStudentAttendanceHistory(student.cardId),
            onEnableRecuperativo = { cardId, uIdx -> teacherViewModel.enableRecuperativoForStudent(cardId, uIdx) },
            onEnableNotebookRecuperativo = { cardId, uIdx -> teacherViewModel.enableNotebookRecuperativo(cardId, uIdx) },
            onAuthorizeProfileEdit = { cardId -> teacherViewModel.authorizeStudentProfileEdit(cardId) },
            onClearDoubt = { cardId -> teacherViewModel.clearStudentDoubt(cardId) },
            onDismiss = { selectedStudentHistory = null }
        )
    }

    if (showFinishClassDialog) {
        AlertDialog(
            onDismissRequest = { showFinishClassDialog = false },
            title = { Text("Finalizar Clase") },
            text = { Text("¿Deseas cerrar la clase actual? Se guardará la asistencia de todos los alumnos.") },
            confirmButton = {
                Button(onClick = { teacherViewModel.incrementTotalClasses(); showFinishClassDialog = false }, colors = ButtonDefaults.buttonColors(containerColor = StatusError)) {
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
                Button(onClick = { teacherViewModel.clearStudentDoubt(selectedStudentDoubt!!.cardId); selectedStudentDoubt = null }) {
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
                        teacherViewModel.sendPrivateMessage(studentToMessage!!.cardId, privateMsgText)
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
                            Button(onClick = { teacherViewModel.startClassServer(liveSection) }, enabled = liveSection.isNotEmpty(), colors = ButtonDefaults.buttonColors(containerColor = StatusSuccess), shape = RoundedCornerShape(8.dp)) {
                                Icon(Icons.Default.PlayArrow, null); Spacer(Modifier.width(4.dp)); Text("INICIAR CLASE")
                            }
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(containerColor = CosmicDarkSurface)
                )
                
                val localIp by mainViewModel.localIpAddress.collectAsState()
                if (isServerRunning) {
                    Surface(
                        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 4.dp),
                        color = StatusSuccess.copy(alpha = 0.15f),
                        shape = RoundedCornerShape(8.dp),
                        border = androidx.compose.foundation.BorderStroke(1.dp, StatusSuccess.copy(alpha = 0.3f))
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
                        border = androidx.compose.foundation.BorderStroke(1.dp, AccentCyan.copy(alpha = 0.3f))
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
            NavigationBar {
                NavigationBarItem(selected = activeTab == 0, onClick = { activeTab = 0 }, icon = { Icon(Icons.AutoMirrored.Filled.List, null) }, label = { Text("Unidades") })
                NavigationBarItem(selected = activeTab == 1, onClick = { activeTab = 1 }, icon = { Icon(Icons.Default.Groups, null) }, label = { Text("Aula") })
                NavigationBarItem(selected = activeTab == 2, onClick = { activeTab = 2 }, icon = { Icon(Icons.Default.HowToReg, null) }, label = { Text("Asist") })
                NavigationBarItem(selected = activeTab == 3, onClick = { activeTab = 3 }, icon = { Icon(Icons.Default.Assessment, null) }, label = { Text("Métricas") })
                NavigationBarItem(selected = activeTab == 4, onClick = { activeTab = 4 }, icon = { Icon(Icons.Default.Settings, null) }, label = { Text("Sist") })
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
                    onRefreshCourse = { mainViewModel.refreshCurrentCourse() },
                    onActivateUnit = { teacherViewModel.updateActiveUnit(it) },
                    onToggleRead = { idx, state -> teacherViewModel.updateTeacherUnitToggle(idx, state, false) },
                    onToggleExam = { idx, state -> teacherViewModel.updateTeacherUnitToggle(idx, state, true) },
                    onToggleNotebook = { idx, state -> teacherViewModel.updateTeacherNotebookToggle(idx, state) },
                    onGoToAula = { activeTab = 1 }
                )
                1 -> TeacherAulaTab(
                    isServerRunning = isServerRunning, liveSection = liveSection, liveActiveUnit = liveActiveUnit,
                    sections = sections,
                    allConnectedStudents = allConnectedStudents,
                    onAddSection = { name, weeks -> teacherViewModel.addSection(name, weeks, {}, {}) },
                    onSelectSection = { teacherViewModel.selectSection(it) },
                    onDeleteSection = { teacherViewModel.deleteSection(it) },
                    onShareFile = { filePicker.launch("*/*") },
                    onShareZip = { zipFolderPicker.launch(null) },
                    onSendGlobalMsg = { teacherViewModel.sendGlobalMessage(it) },
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
                    onUpdateAttendance = { id, stat -> teacherViewModel.updateManualAttendance(id, stat) }
                )
                3 -> TeacherMetricsTab(
                    liveSection = liveSection, sections = sections, syncedStudents = syncedStudents,
                    units = units,
                    onGenerateAcademicReport = { teacherViewModel.generateAcademicReport(context, units.size) },
                    onGenerateAttendanceReport = { teacherViewModel.generateAttendanceReport(context) },
                    onStudentClick = { selectedStudentHistory = it }
                )
                4 -> TeacherCloseTab(
                    onGenerateAcademicReport = { teacherViewModel.generateAcademicReport(context, units.size) },
                    onGenerateAttendanceReport = { teacherViewModel.generateAttendanceReport(context) },
                    onGenerateDetailedMasterReport = { teacherViewModel.generateDetailedMasterReport(context, units.size) },
                    onOpenReportViewer = { showReportViewer = true },
                    onViewLogs = { showLogViewer = true },
                    onShareLogs = { com.example.data.AppLogger.shareLogFile(context) },
                    onResetApp = { showResetAppDialog = true },
                    onInjectData = { weeks -> teacherViewModel.injectTestData(weeks) },
                    onGenerateAllReports = { teacherViewModel.generateAllAcademicReports(context, units.size) }
                )
            }
        }
    }
    
    // Diálogo de Reset (Fuera del Scaffold)
    if (showResetAppDialog) {
        AlertDialog(
            onDismissRequest = { showResetAppDialog = false },
            title = { Text("Borrado de Fábrica") },
            text = { Text("Se eliminarán todos los perfiles, secciones y registros. Esta acción no se puede deshacer.") },
            confirmButton = {
                Button(onClick = { mainViewModel.resetApp(context); showResetAppDialog = false }, colors = ButtonDefaults.buttonColors(containerColor = StatusError)) {
                    Text("BORRAR TODO")
                }
            },
            dismissButton = { TextButton(onClick = { showResetAppDialog = false }) { Text("CANCELAR") } }
        )
    }
}
