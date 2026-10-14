package com.example.ui.screens.tabs

import android.net.Uri
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.CloudDownload
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.FolderOpen
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.Wifi
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.data.model.UnitData
import com.example.data.UnitProgress
import com.example.ui.components.AnimatedLoadingView
import com.example.ui.components.FileExplorerDialog
import com.example.ui.components.M3Carousel
import com.example.ui.components.PrimaryActionButton
import com.example.ui.components.UnitCard
import com.example.ui.theme.AccentAmber
import com.example.ui.theme.AccentCyan
import com.example.ui.theme.AccentTeal
import com.example.ui.theme.CosmicDarkSurface
import com.example.ui.theme.Dimensions
import com.example.ui.theme.StatusSuccess

@Composable
fun StudentClassTab(
    profile: com.example.data.UserProfile?,
    units: List<UnitData>,
    isLocked: Boolean,
    isSyncing: Boolean,
    isStudyModeActive: Boolean,
    enabledReads: String,
    studyProgress: List<UnitProgress>,
    onUnitClick: (UnitData) -> Unit,
    onConnectClick: () -> Unit,
    onLinkFolderClick: () -> Unit
) {
    if (units.isEmpty()) {
        // Si el alumno está conectado (isLocked=true) pero no tiene materiales,
        // mostramos estado de sincronización aunque el primer sync no haya completado aún
        if (isLocked) {
            Column(
                modifier = Modifier.fillMaxSize().padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Surface(
                    color = AccentCyan.copy(alpha = 0.1f),
                    shape = RoundedCornerShape(12.dp),
                    border = BorderStroke(1.dp, AccentCyan.copy(alpha = 0.3f))
                ) {
                    Text(
                        text = "📡 SESIÓN EN VIVO ACTIVA",
                        color = AccentCyan,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Black,
                        letterSpacing = 1.sp,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                    )
                }

                Spacer(Modifier.height(16.dp))

                AnimatedLoadingView(
                    title = "Sincronizando Materiales",
                    subtitle = if (isSyncing) "Recibiendo el paquete de contenidos del profesor..." else "Conectado al servidor. Esperando inicio de sincronización...",
                    icon = Icons.Default.CloudDownload
                )

                Spacer(Modifier.height(24.dp))

                // Tarjeta de consejos de sincronización
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = CosmicDarkSurface),
                    shape = RoundedCornerShape(16.dp),
                    border = BorderStroke(1.dp, Color.Gray.copy(alpha = 0.2f))
                ) {
                    Row(
                        Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("💡", fontSize = 24.sp)
                        Spacer(Modifier.width(12.dp))
                        Column {
                            Text(
                                text = "Consejo de Conexión",
                                color = Color.White,
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp
                            )
                            Spacer(Modifier.height(2.dp))
                            Text(
                                text = "Mantén la aplicación abierta y tu dispositivo conectado a la red Wi-Fi del profesor para recibir las unidades.",
                                color = Color.Gray,
                                fontSize = 11.sp
                            )
                        }
                    }
                }
            }
        } else {
            Column(
                modifier = Modifier.fillMaxSize().padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Box(
                    modifier = Modifier
                        .size(96.dp)
                        .clip(CircleShape)
                        .background(AccentCyan.copy(alpha = 0.15f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Default.School, null, modifier = Modifier.size(56.dp), tint = AccentCyan)
                }
                Spacer(Modifier.height(24.dp))
                Text("¡Bienvenido al Aula!", style = MaterialTheme.typography.headlineMedium, color = Color.White, fontWeight = FontWeight.Black)
                Text("Para comenzar a estudiar, conéctate a una clase en vivo o vincula tus materiales.", color = Color.Gray, textAlign = TextAlign.Center, fontSize = 14.sp)
                
                Spacer(Modifier.height(40.dp))
                
                Button(
                    onClick = onConnectClick,
                    modifier = Modifier.fillMaxWidth().height(56.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = AccentCyan),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(Icons.Default.Wifi, null, tint = Color.Black)
                    Spacer(Modifier.width(12.dp))
                    Text("TOCA PARA CONECTAR AL AULA", fontWeight = FontWeight.Black, color = Color.Black)
                }
                
                Spacer(Modifier.height(16.dp))
                
                OutlinedButton(
                    onClick = onLinkFolderClick,
                    modifier = Modifier.fillMaxWidth().height(56.dp),
                    shape = RoundedCornerShape(12.dp),
                    border = BorderStroke(1.5.dp, AccentTeal)
                ) {
                    Icon(Icons.Default.FolderOpen, null, tint = AccentTeal)
                    Spacer(Modifier.width(12.dp))
                    Text("VINCULAR CARPETA LOCAL", color = AccentTeal, fontWeight = FontWeight.ExtraBold)
                }
            }
        }
    } else {
        Column(Modifier.fillMaxSize()) {
            Row(
                modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = stringResource(R.string.course_content),
                        style = MaterialTheme.typography.titleLarge,
                        color = Color.White,
                        fontWeight = FontWeight.Black
                    )
                    Spacer(Modifier.width(8.dp))
                    if (isLocked) {
                        Surface(
                            color = StatusSuccess.copy(alpha = 0.15f),
                            shape = RoundedCornerShape(Dimensions.CornerFull),
                            border = BorderStroke(1.dp, StatusSuccess.copy(alpha = 0.4f))
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(modifier = Modifier.size(6.dp).background(StatusSuccess, CircleShape))
                                Spacer(Modifier.width(4.dp))
                                Text("EN VIVO", color = StatusSuccess, fontWeight = FontWeight.Bold, fontSize = 9.sp)
                            }
                        }
                    }
                    var showExplorerDialog by remember { mutableStateOf(false) }

                    IconButton(
                        onClick = { showExplorerDialog = true },
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(Icons.Default.Folder, contentDescription = "Explorar Archivos", tint = AccentCyan, modifier = Modifier.size(20.dp))
                    }

                    IconButton(
                        onClick = onLinkFolderClick,
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(Icons.Default.FolderOpen, contentDescription = "Cambiar Carpeta", tint = AccentCyan, modifier = Modifier.size(20.dp))
                    }

                    if (showExplorerDialog) {
                        val folderUri = profile?.materialsFolderUri?.takeIf { it.isNotEmpty() }?.let { Uri.parse(it) }
                        FileExplorerDialog(
                            folderUri = folderUri,
                            onDismiss = { showExplorerDialog = false }
                        )
                    }
                }
                
                // Se elimina el botón redundante de la cabecera para evitar duplicidad y saturación visual
            }
            



            
            // Tarjeta de Progreso General del Curso (Fija / Sticky arriba)
            val completedCount = units.count { unit -> studyProgress.find { it.unitIndex == unit.index }?.isRead == true }
            val progressPercent = if (units.isNotEmpty()) (completedCount.toFloat() / units.size * 100).toInt() else 0

            Card(
                modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp),
                colors = CardDefaults.cardColors(containerColor = CosmicDarkSurface),
                shape = RoundedCornerShape(20.dp),
                border = BorderStroke(1.dp, AccentTeal.copy(alpha = 0.3f))
            ) {
                Column(Modifier.padding(16.dp)) {
                    Row(
                        Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "PROGRESO DEL CURSO",
                                color = AccentTeal,
                                fontWeight = FontWeight.ExtraBold,
                                fontSize = 11.sp,
                                letterSpacing = 1.sp
                            )
                            Spacer(Modifier.height(2.dp))
                            Text(
                                text = "$completedCount de ${units.size} unidades completadas",
                                color = Color.White,
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp
                            )
                        }
                        Surface(
                            color = AccentTeal.copy(alpha = 0.2f),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text(
                                text = "$progressPercent%",
                                color = AccentTeal,
                                fontWeight = FontWeight.Black,
                                fontSize = 14.sp,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                            )
                        }
                    }
                    Spacer(Modifier.height(12.dp))
                    LinearProgressIndicator(
                        progress = { if (units.isNotEmpty()) completedCount.toFloat() / units.size else 0f },
                        modifier = Modifier.fillMaxWidth().height(8.dp).clip(RoundedCornerShape(4.dp)),
                        color = AccentTeal,
                        trackColor = Color.DarkGray.copy(alpha = 0.5f)
                    )
                }
            }

            Spacer(Modifier.height(8.dp))
            LazyColumn(
                Modifier.fillMaxSize(),
                contentPadding = PaddingValues(bottom = 80.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(units) { unit ->
                    val unitProgress = studyProgress.find { it.unitIndex == unit.index }
                    val isRead = unitProgress?.isRead == true
                    val isEnabled = isStudyModeActive || enabledReads.split(",").contains(unit.index.toString())
                    
                    UnitCard(
                        unit = unit,
                        isEnabled = isEnabled,
                        isRead = isRead,
                        onClick = { onUnitClick(unit) }
                    )
                }
            }
        }
    }
}

@androidx.compose.ui.tooling.preview.Preview(showBackground = true, backgroundColor = 0xFF0D1117)
@Composable
fun StudentClassTabOfflineWithUnitsPreview() {
    com.example.ui.theme.MyApplicationTheme {
        Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
            StudentClassTab(
                profile = null,
                units = listOf(
                    UnitData(
                        index = 1,
                        title = "Unidad 1: Introducción a la Programación",
                        description = "Descripción unidad 1",
                        summary = "Fundamentos y sintaxis básica",
                        audioNarratorText = "",
                        topicText = "",
                        challengeTitle = "",
                        challengeInstructions = "",
                        challengeOptions = emptyList(),
                        challengeAnswer = ""
                    ),
                    UnitData(
                        index = 2,
                        title = "Unidad 2: Estructuras de Control",
                        description = "Descripción unidad 2",
                        summary = "Condicionales y bucles en Kotlin",
                        audioNarratorText = "",
                        topicText = "",
                        challengeTitle = "",
                        challengeInstructions = "",
                        challengeOptions = emptyList(),
                        challengeAnswer = ""
                    ),
                    UnitData(
                        index = 3,
                        title = "Unidad 3: Programación Orientada a Objetos",
                        description = "Descripción unidad 3",
                        summary = "Clases, objetos y herencia",
                        audioNarratorText = "",
                        topicText = "",
                        challengeTitle = "",
                        challengeInstructions = "",
                        challengeOptions = emptyList(),
                        challengeAnswer = ""
                    )
                ),
                isLocked = false,
                isSyncing = false,
                isStudyModeActive = true,
                enabledReads = "1,2,3",
                studyProgress = listOf(
                    UnitProgress(unitIndex = 1, isRead = true)
                ),
                onUnitClick = {},
                onConnectClick = {},
                onLinkFolderClick = {}
            )
        }
    }
}

@androidx.compose.ui.tooling.preview.Preview(showBackground = true, backgroundColor = 0xFF0D1117)
@Composable
fun StudentClassTabOfflinePreview() {
    com.example.ui.theme.MyApplicationTheme {
        Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
            StudentClassTab(
                profile = null,
                units = emptyList(),
                isLocked = false,
                isSyncing = false,
                isStudyModeActive = false,
                enabledReads = "",
                studyProgress = emptyList(),
                onUnitClick = {},
                onConnectClick = {},
                onLinkFolderClick = {}
            )
        }
    }
}

