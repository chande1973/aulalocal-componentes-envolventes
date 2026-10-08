package com.example.ui.screens

import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.layout.imePadding

import com.example.R
import com.example.data.UserProfile
import com.example.data.model.LoadResult
import com.example.ui.MainViewModel
import com.example.ui.avatarOptionsList
import com.example.ui.components.PrimaryActionButton
import com.example.ui.theme.AccentAmber
import com.example.ui.theme.AccentCyan
import com.example.ui.theme.AccentTeal
import com.example.ui.theme.CosmicDarkBg
import com.example.ui.theme.CosmicDarkSurface
import com.example.ui.viewmodels.StudentViewModel
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SetupAssistantScreen(
    mainViewModel: MainViewModel,
    studentViewModel: StudentViewModel
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    
    var selectedRole by rememberSaveable { mutableStateOf<String?>(null) }
    var setupStep by rememberSaveable { mutableIntStateOf(0) }
    
    // Obtenemos el resultado de validación del ViewModel
    val wizardResult by mainViewModel.wizardValidationResult.collectAsState()
    val loadingProgress by mainViewModel.loadingProgress.collectAsState()
    
    var isValidating by rememberSaveable { mutableStateOf(false) }
    var selectedFolderUriString by rememberSaveable { mutableStateOf<String?>(null) }
    val selectedFolderUri = selectedFolderUriString?.let { Uri.parse(it) }

    // Formularios
    var firstName by rememberSaveable { mutableStateOf("") }
    var lastName by rememberSaveable { mutableStateOf("") }
    var cardId by rememberSaveable { mutableStateOf("") }
    var studentSection by rememberSaveable { mutableStateOf("") }
    var selectedAvatarEmoji by rememberSaveable { mutableStateOf(avatarOptionsList[0].emoji) }
    val selectedAvatar = avatarOptionsList.find { it.emoji == selectedAvatarEmoji } ?: avatarOptionsList[0]
    var showLogViewer by rememberSaveable { mutableStateOf(false) }

    val isFirstNameError = firstName.isNotEmpty() && firstName.length < 3
    val isLastNameError = lastName.isNotEmpty() && lastName.length < 3
    val isCardIdError = cardId.isNotEmpty() && cardId.length < 5
    val isSectionError = selectedRole == "student" && studentSection.isNotEmpty() && studentSection.length < 2

    val detectedIp by studentViewModel.detectedTeacherIp.collectAsState()
    val detectedSection by studentViewModel.detectedSectionName.collectAsState()

    val folderLauncher = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocumentTree()) { uri ->
        uri?.let {
            context.contentResolver.takePersistableUriPermission(it, Intent.FLAG_GRANT_READ_URI_PERMISSION)
            selectedFolderUriString = it.toString()
            isValidating = true
            scope.launch {
                mainViewModel.validateAndCacheCoursePack(it)
                isValidating = false
            }
        }
    }

    if (showLogViewer) {
        LogViewerDialog(onDismiss = { showLogViewer = false })
    }

    LaunchedEffect(wizardResult, selectedRole) {
        if (selectedRole == "student") {
            val res = wizardResult
            if (res is LoadResult.Success || res is LoadResult.PartialSuccess) {
                selectedFolderUri?.let { uri ->
                    mainViewModel.confirmSetupAndSaveProfile(
                        uri,
                        UserProfile(
                            role = "student",
                            firstName = firstName.trim(),
                            lastName = lastName.trim(),
                            cardId = cardId.trim(),
                            avatar = selectedAvatar.emoji,
                            activeSection = studentSection.trim(),
                            email = "",
                            isLocked = false
                        )
                    )
                    Toast.makeText(context, "✅ ¡Configuración completada con éxito!", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    Scaffold(topBar = { 
        TopAppBar(
            title = { Text(stringResource(R.string.setup_title), fontWeight = FontWeight.Bold, maxLines = 1, overflow = TextOverflow.Ellipsis) }, 
            colors = TopAppBarDefaults.topAppBarColors(containerColor = CosmicDarkSurface),
            actions = {
                IconButton(onClick = { showLogViewer = true }) {
                    Icon(Icons.Default.BugReport, stringResource(R.string.action_logs), tint = AccentCyan)
                }
            }
        ) 
    }) { padding ->
        LazyColumn(
            modifier = Modifier
                .padding(padding)
                .navigationBarsPadding()
                .fillMaxSize()
                .background(CosmicDarkBg)
                .padding(horizontal = 24.dp)
                .imePadding(),
            horizontalAlignment = Alignment.CenterHorizontally,
            contentPadding = PaddingValues(top = 24.dp, bottom = 80.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                if (setupStep == 0) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(Icons.Default.School, null, tint = AccentTeal, modifier = Modifier.size(100.dp))
                        Text(stringResource(R.string.setup_welcome), style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold, color = Color.White)
                        Spacer(Modifier.height(32.dp))
                        
                        Text(stringResource(R.string.setup_step_1), fontSize = 12.sp, color = AccentCyan.copy(alpha = 0.7f), modifier = Modifier.fillMaxWidth())
                        Spacer(Modifier.height(8.dp))
                        Text(stringResource(R.string.setup_choose_role), color = Color.LightGray, modifier = Modifier.fillMaxWidth())
                        Spacer(Modifier.height(16.dp))
                        
                        RoleCard(stringResource(R.string.role_teacher), stringResource(R.string.role_teacher_desc), selectedRole == "teacher", AccentTeal) { selectedRole = "teacher" }
                        Spacer(Modifier.height(16.dp))
                        RoleCard(stringResource(R.string.role_student), stringResource(R.string.role_student_desc), selectedRole == "student", AccentCyan) { selectedRole = "student" }
                        
                        Spacer(Modifier.height(40.dp))

                        PrimaryActionButton(
                            text = stringResource(R.string.setup_btn_continue_step_2),
                            onClick = { setupStep = 1 },
                            enabled = selectedRole != null,
                            containerColor = AccentTeal,
                            icon = Icons.AutoMirrored.Filled.ArrowForward
                        )
                    }
                } else if (setupStep == 1) {
                    Column {
                        Text(stringResource(R.string.setup_step_2), fontSize = 12.sp, color = AccentCyan.copy(alpha = 0.7f), modifier = Modifier.fillMaxWidth())
                        Spacer(Modifier.height(8.dp))
                        Text(stringResource(R.string.setup_profile_info), style = MaterialTheme.typography.headlineSmall, color = Color.White, modifier = Modifier.fillMaxWidth())
                        Spacer(Modifier.height(24.dp))
                        
                        OutlinedTextField(
                            value = firstName, 
                            onValueChange = { firstName = it }, 
                            label = { Text(stringResource(R.string.label_first_name)) }, 
                            isError = isFirstNameError,
                            supportingText = { if (isFirstNameError) Text(stringResource(R.string.error_min_3_chars)) },
                            modifier = Modifier.fillMaxWidth()
                        )
                        Spacer(Modifier.height(16.dp))
                        
                        OutlinedTextField(
                            value = lastName, 
                            onValueChange = { lastName = it }, 
                            label = { Text(stringResource(R.string.label_last_name)) }, 
                            isError = isLastNameError,
                            supportingText = { if (isLastNameError) Text(stringResource(R.string.error_min_3_chars)) },
                            modifier = Modifier.fillMaxWidth()
                        )
                        Spacer(Modifier.height(16.dp))
                        
                        OutlinedTextField(
                            value = cardId, 
                            onValueChange = { cardId = it }, 
                            label = { Text(stringResource(R.string.label_card_id)) }, 
                            isError = isCardIdError,
                            supportingText = { if (isCardIdError) Text(stringResource(R.string.error_min_5_chars)) },
                            modifier = Modifier.fillMaxWidth()
                        )
                        Spacer(Modifier.height(16.dp))

                        if (selectedRole == "student") {
                            OutlinedTextField(
                                value = studentSection, 
                                onValueChange = { studentSection = it }, 
                                label = { Text(stringResource(R.string.label_section)) }, 
                                isError = isSectionError,
                                supportingText = { if (isSectionError) Text(stringResource(R.string.example_section)) },
                                modifier = Modifier.fillMaxWidth()
                            )
                            Spacer(Modifier.height(16.dp))
                        }
                        
                        Text(stringResource(R.string.setup_choose_avatar), color = Color.White)
                        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
                            avatarOptionsList.forEach { av ->
                                Box(Modifier.size(50.dp).clip(CircleShape).background(if(selectedAvatarEmoji == av.emoji) av.color else Color.DarkGray).clickable { selectedAvatarEmoji = av.emoji }, contentAlignment = Alignment.Center) {
                                    Text(av.emoji, fontSize = 24.sp)
                                }
                            }
                        }

                        Spacer(Modifier.height(40.dp))
                        
                        val isFormValid = firstName.trim().length >= 3 && 
                                         lastName.trim().length >= 3 && 
                                         cardId.trim().length >= 5 &&
                                         (selectedRole == "teacher" || studentSection.trim().isNotEmpty())

                        if (selectedRole != null) {
                            PrimaryActionButton(
                                text = if (isFormValid) {
                                    if (selectedRole == "teacher") "SIGUIENTE: MATERIALES" else "SIGUIENTE: CONEXIÓN Y MATERIALES"
                                } else {
                                    "Completa tus datos para continuar"
                                },
                                onClick = { setupStep = 2 },
                                enabled = isFormValid,
                                containerColor = if (selectedRole == "teacher") AccentTeal else AccentCyan,
                                contentColor = if (selectedRole == "student" && isFormValid) Color.Black else Color.White
                            )
                        }
                    }
                } else {
                    // PASO 3: CARGA DE MATERIALES (DIFERENCIADO)
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("Último Paso: Carga de Contenido", fontSize = 12.sp, color = AccentCyan.copy(alpha = 0.7f), modifier = Modifier.fillMaxWidth())
                        Spacer(Modifier.height(8.dp))
                        Text(if(selectedRole == "teacher") "Prepara tu Curso" else "Únete a la Clase", style = MaterialTheme.typography.headlineSmall, color = Color.White, modifier = Modifier.fillMaxWidth())
                        Spacer(Modifier.height(24.dp))

                        if (selectedRole == "student") {
                            // PRIORIDAD PARA EL ALUMNO: RED
                            val isDiscovering by studentViewModel.isDiscovering.collectAsState()
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                colors = CardDefaults.cardColors(containerColor = CosmicDarkSurface),
                                border = if (detectedIp != null) BorderStroke(1.dp, Color.Green) else null
                            ) {
                                Column(Modifier.padding(16.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                                    Icon(if(detectedIp != null) Icons.Default.Podcasts else Icons.Default.Wifi, null, tint = if(detectedIp != null) Color.Green else AccentCyan)
                                    Spacer(Modifier.height(16.dp))
                                    Text("Busca el servidor de tu profesor en la red WiFi actual para recibir el curso.", textAlign = TextAlign.Center, fontSize = 12.sp, color = Color.LightGray)
                                    Spacer(Modifier.height(16.dp))
                                    Button(
                                        onClick = { studentViewModel.discoverActiveTeacher() },
                                        enabled = !isDiscovering,
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        if (isDiscovering) CircularProgressIndicator(Modifier.size(20.dp), color = Color.White)
                                        else Text(stringResource(R.string.setup_btn_discover))
                                    }
                                    if (detectedIp != null) {
                                        Text("✅ Profesor encontrado en: $detectedSection", color = Color.Green, fontSize = 11.sp, modifier = Modifier.padding(top = 8.dp))
                                    }
                                }
                            }
                            Spacer(Modifier.height(24.dp))
                            Text("O también puedes:", fontSize = 10.sp, color = Color.Gray)
                        }

                        // CARGA MANUAL (Obligatoria para profesor, secundaria para alumno)
                        val res = wizardResult
                        val isAnySuccess = res is LoadResult.Success || res is LoadResult.PartialSuccess
                        
                        Card(
                            modifier = Modifier.fillMaxWidth(), 
                            colors = CardDefaults.cardColors(containerColor = CosmicDarkSurface),
                            border = if (isAnySuccess) BorderStroke(1.dp, AccentTeal) else if (res is LoadResult.Failure) BorderStroke(1.dp, Color.Red) else null
                        ) {
                            Column(Modifier.padding(16.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(stringResource(R.string.course_content), fontWeight = FontWeight.Bold, color = Color.White)
                                Text(stringResource(R.string.setup_select_folder_desc), fontSize = 10.sp, color = Color.Gray, textAlign = TextAlign.Center)
                                Spacer(Modifier.height(12.dp))

                                if (loadingProgress.isLoading) {
                                    Card(
                                        modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp),
                                        colors = CardDefaults.cardColors(containerColor = AccentAmber.copy(alpha = 0.15f)),
                                        border = BorderStroke(1.dp, AccentAmber.copy(alpha = 0.6f)),
                                        shape = RoundedCornerShape(12.dp)
                                    ) {
                                        Column(Modifier.padding(12.dp)) {
                                            Row(verticalAlignment = Alignment.CenterVertically) {
                                                Icon(Icons.Default.Inventory, null, tint = AccentAmber, modifier = Modifier.size(18.dp))
                                                Spacer(Modifier.width(10.dp))
                                                Column(Modifier.weight(1f)) {
                                                    Text(
                                                        text = loadingProgress.step.ifEmpty { "ANALIZANDO MATERIALES" }.uppercase(),
                                                        color = AccentAmber,
                                                        fontWeight = FontWeight.ExtraBold,
                                                        fontSize = 11.sp,
                                                        letterSpacing = 1.sp
                                                    )
                                                    Text(
                                                        text = loadingProgress.step.ifEmpty { "Procesando contenido..." },
                                                        color = Color.White,
                                                        fontSize = 11.sp,
                                                        fontWeight = FontWeight.Medium
                                                    )
                                                }
                                                if (loadingProgress.total > 0) {
                                                    val pct = (loadingProgress.current.toFloat() / loadingProgress.total * 100).toInt().coerceIn(0, 100)
                                                    Text(
                                                        text = "$pct%",
                                                        color = AccentAmber,
                                                        fontWeight = FontWeight.ExtraBold,
                                                        fontSize = 14.sp
                                                    )
                                                }
                                            }
                                            if (loadingProgress.currentFile.isNotEmpty()) {
                                                Spacer(Modifier.height(4.dp))
                                                Text(
                                                    text = "📄 Procesando: ${loadingProgress.currentFile}",
                                                    color = Color.LightGray,
                                                    fontSize = 10.sp,
                                                    maxLines = 1,
                                                    overflow = TextOverflow.Ellipsis
                                                )
                                            }
                                            Spacer(Modifier.height(8.dp))
                                            if (loadingProgress.total > 0) {
                                                val pct = (loadingProgress.current.toFloat() / loadingProgress.total).coerceIn(0f, 1f)
                                                LinearProgressIndicator(
                                                    progress = { pct },
                                                    modifier = Modifier.fillMaxWidth().height(6.dp).clip(RoundedCornerShape(3.dp)),
                                                    color = AccentAmber,
                                                    trackColor = Color.DarkGray.copy(alpha = 0.5f)
                                                )
                                            } else {
                                                LinearProgressIndicator(
                                                    modifier = Modifier.fillMaxWidth().height(6.dp).clip(RoundedCornerShape(3.dp)),
                                                    color = AccentAmber,
                                                    trackColor = Color.DarkGray.copy(alpha = 0.5f)
                                                )
                                            }
                                        }
                                    }
                                } else {
                                    when (res) {
                                    is LoadResult.Success -> {
                                        Icon(Icons.Default.CheckCircle, null, tint = AccentTeal)
                                        Text("Materiales listos (v${res.data.index.courseMeta.version})", color = AccentTeal, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                        Spacer(Modifier.height(8.dp))
                                    }
                                    is LoadResult.PartialSuccess -> {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Icon(Icons.Default.Warning, null, tint = AccentAmber, modifier = Modifier.size(18.dp))
                                            Spacer(Modifier.width(6.dp))
                                            Text("Materiales cargados con advertencias", color = AccentAmber, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                        }
                                        Spacer(Modifier.height(8.dp))
                                        Column(Modifier.fillMaxWidth().background(AccentAmber.copy(alpha = 0.1f), RoundedCornerShape(8.dp)).padding(8.dp)) {
                                            res.warnings.take(5).forEach { warn ->
                                                Text("• ${warn.message}", color = Color.LightGray, fontSize = 10.sp)
                                            }
                                        }
                                        Spacer(Modifier.height(12.dp))
                                    }
                                    is LoadResult.Failure -> {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Icon(Icons.Default.Error, null, tint = Color.Red, modifier = Modifier.size(18.dp))
                                            Spacer(Modifier.width(6.dp))
                                            Text("Errores encontrados en el CSV / Carpeta", color = Color.Red, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                        }
                                        Spacer(Modifier.height(8.dp))
                                        Column(Modifier.fillMaxWidth().background(Color.Red.copy(alpha = 0.1f), RoundedCornerShape(8.dp)).padding(8.dp)) {
                                            res.errors.take(5).forEach { err ->
                                                Text("• ${err.message}", color = Color.LightGray, fontSize = 10.sp)
                                            }
                                        }
                                        Spacer(Modifier.height(12.dp))
                                    }
                                    null -> {}
                                    }
                                }

                                Button(
                                    onClick = { folderLauncher.launch(null) },
                                    modifier = Modifier.fillMaxWidth(),
                                    colors = ButtonDefaults.buttonColors(containerColor = if(isAnySuccess) Color.DarkGray else MaterialTheme.colorScheme.primary)
                                ) {
                                    Text(if(isAnySuccess) "Cambiar Carpeta" else "Vincular Carpeta Manual")
                                }
                            }
                        }
                    }
                }
            }

            item {
                if (setupStep == 2) {
                    Spacer(Modifier.height(40.dp))
                    val isAnySuccess = wizardResult is LoadResult.Success || wizardResult is LoadResult.PartialSuccess
                    val isStep3Valid = if(selectedRole == "teacher") isAnySuccess else true

                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        if (selectedRole == "teacher" && !isStep3Valid) {
                            Text(
                                text = "El profesor debe vincular la carpeta de materiales obligatoriamente",
                                color = Color.Red.copy(alpha = 0.7f),
                                fontSize = 11.sp,
                                modifier = Modifier.padding(bottom = 8.dp)
                            )
                        }
                        
                        PrimaryActionButton(
                            text = stringResource(R.string.setup_btn_finish),
                            onClick = { 
                                scope.launch { 
                                    mainViewModel.confirmSetupAndSaveProfile(
                                        selectedFolderUri ?: Uri.EMPTY, 
                                        UserProfile(
                                            role = selectedRole!!, 
                                            firstName = firstName.trim(), 
                                            lastName = lastName.trim(), 
                                            cardId = cardId.trim(),
                                            avatar = selectedAvatar.emoji,
                                            activeSection = studentSection.trim(), 
                                            email = "",
                                            isLocked = false 
                                        )
                                    ) 
                                } 
                            },
                            enabled = isStep3Valid,
                            containerColor = AccentTeal
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun RoleCard(title: String, desc: String, selected: Boolean, color: Color, onClick: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth().clickable { onClick() }.border(if(selected) 2.dp else 0.dp, color, RoundedCornerShape(12.dp)),
        colors = CardDefaults.cardColors(containerColor = CosmicDarkSurface)
    ) {
        Row(Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            RadioButton(selected, onClick)
            Column {
                Text(title, fontWeight = FontWeight.Bold, color = Color.White)
                Text(desc, fontSize = 12.sp, color = Color.Gray)
            }
        }
    }
}
