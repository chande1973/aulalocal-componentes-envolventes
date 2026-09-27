package com.example.ui.screens.tabs

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CloudDownload
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.data.model.UnitData
import com.example.data.UnitProgress
import com.example.ui.components.UnitCard
import com.example.ui.theme.AccentCyan
import com.example.ui.theme.AccentTeal
import com.example.ui.theme.CosmicDarkSurface

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
                // Contenedor con efecto de brillo
                Box(
                    modifier = Modifier
                        .size(100.dp)
                        .background(AccentCyan.copy(alpha = 0.15f), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.CloudDownload,
                        contentDescription = null,
                        modifier = Modifier.size(48.dp),
                        tint = AccentCyan
                    )
                }
                
                Spacer(Modifier.height(24.dp))
                
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

                Text(
                    text = "Sincronizando Materiales", 
                    style = MaterialTheme.typography.headlineMedium, 
                    color = Color.White, 
                    fontWeight = FontWeight.Black,
                    textAlign = TextAlign.Center
                )
                
                Spacer(Modifier.height(8.dp))
                
                Text(
                    text = if (isSyncing) "Recibiendo el paquete de contenidos del profesor..." else "Conectado al servidor. Esperando inicio de sincronización...", 
                    color = Color.Gray, 
                    fontSize = 14.sp,
                    textAlign = TextAlign.Center
                )
                
                Spacer(Modifier.height(32.dp))

                // Barra de progreso lineal moderna en lugar de solo un círculo
                LinearProgressIndicator(
                    modifier = Modifier.fillMaxWidth(0.8f).height(8.dp).clip(RoundedCornerShape(4.dp)),
                    color = AccentCyan,
                    trackColor = Color.DarkGray.copy(alpha = 0.5f)
                )

                Spacer(Modifier.height(32.dp))

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
                Icon(Icons.Default.School, null, modifier = Modifier.size(80.dp), tint = Color.DarkGray.copy(alpha = 0.5f))
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
                    Icon(Icons.Default.Wifi, null)
                    Spacer(Modifier.width(12.dp))
                    Text("BUSCAR CLASE EN VIVO", fontWeight = FontWeight.Black)
                }
                
                Spacer(Modifier.height(16.dp))
                
                OutlinedButton(
                    onClick = onLinkFolderClick,
                    modifier = Modifier.fillMaxWidth().height(56.dp),
                    shape = RoundedCornerShape(12.dp),
                    border = BorderStroke(1.dp, Color.Gray.copy(alpha = 0.5f))
                ) {
                    Icon(Icons.Default.FolderOpen, null, tint = Color.Gray)
                    Spacer(Modifier.width(12.dp))
                    Text("VINCULAR CARPETA LOCAL", color = Color.Gray, fontWeight = FontWeight.Bold)
                }
            }
        }
    } else {
        Column(Modifier.fillMaxSize()) {
            if (profile != null) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(44.dp)
                            .clip(CircleShape)
                            .background(AccentCyan.copy(alpha = 0.1f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(profile.avatar.ifEmpty { "👤" }, fontSize = 22.sp)
                    }
                    Spacer(Modifier.width(12.dp))
                    Column {
                        Text(
                            text = "Hola, ${profile.firstName}",
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp
                        )
                        Text(
                            text = if (profile.activeSection.isNotEmpty()) "Sección ${profile.activeSection}" else "ID: ${profile.cardId}",
                            color = Color.Gray,
                            fontSize = 12.sp
                        )
                    }
                }
            }

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
                    IconButton(
                        onClick = onLinkFolderClick,
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(Icons.Default.FolderOpen, contentDescription = "Cambiar Carpeta", tint = AccentCyan, modifier = Modifier.size(20.dp))
                    }
                }
                
                if (isStudyModeActive) {
                    Surface(
                        onClick = onConnectClick,
                        color = AccentCyan.copy(alpha = 0.1f),
                        shape = RoundedCornerShape(8.dp),
                        border = BorderStroke(1.dp, AccentCyan.copy(alpha = 0.5f))
                    ) {
                        Row(Modifier.padding(horizontal = 8.dp, vertical = 4.dp), verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Wifi, null, modifier = Modifier.size(14.dp), tint = AccentCyan)
                            Spacer(Modifier.width(6.dp))
                            Text("CONECTAR", fontSize = 10.sp, fontWeight = FontWeight.Black, color = AccentCyan)
                        }
                    }
                }
            }
            
            // NUEVO: Tarjeta CTA cuando no hay clase activa pero sí materiales
            if (!isLocked) {
                Card(
                    modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp),
                    colors = CardDefaults.cardColors(containerColor = AccentCyan.copy(alpha = 0.1f)),
                    border = BorderStroke(2.dp, AccentCyan),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Column(Modifier.padding(20.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier.size(48.dp).background(AccentCyan.copy(alpha = 0.2f), CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(Icons.Default.Wifi, null, tint = AccentCyan, modifier = Modifier.size(24.dp))
                            }
                            Spacer(Modifier.width(16.dp))
                            Column {
                                Text(
                                    text = "¿Nueva clase disponible?",
                                    fontWeight = FontWeight.ExtraBold,
                                    color = Color.White,
                                    fontSize = 16.sp
                                )
                                Text(
                                    text = "Toca para buscar y unirte a la clase en vivo del profesor",
                                    color = AccentCyan,
                                    fontSize = 12.sp
                                )
                            }
                        }
                        Spacer(Modifier.height(16.dp))
                        Button(
                            onClick = onConnectClick,
                            modifier = Modifier.fillMaxWidth().height(48.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = AccentCyan),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Text(
                                text = "🔍 BUSCAR CLASE EN VIVO Y ENTRAR",
                                fontWeight = FontWeight.Black,
                                color = Color.Black,
                                fontSize = 14.sp
                            )
                        }
                    }
                }
            }
            
            Spacer(Modifier.height(16.dp))
            LazyColumn(
                Modifier.fillMaxSize(),
                contentPadding = PaddingValues(bottom = 80.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                item {
                    // Tarjeta de Progreso General del Curso
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
                }

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
