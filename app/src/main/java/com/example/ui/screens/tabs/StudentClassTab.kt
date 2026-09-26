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
                modifier = Modifier.fillMaxSize(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Box(
                    modifier = Modifier.size(120.dp).background(AccentCyan.copy(alpha = 0.1f), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Default.CloudDownload, null, modifier = Modifier.size(60.dp), tint = AccentCyan)
                }
                Spacer(Modifier.height(32.dp))
                Text(
                    text = stringResource(R.string.msg_connected_waiting_data), 
                    style = MaterialTheme.typography.headlineSmall, 
                    color = Color.White, 
                    fontWeight = FontWeight.Black,
                    textAlign = TextAlign.Center
                )
                Spacer(Modifier.height(8.dp))
                Text(
                    text = if (isSyncing) "Sincronizando materiales con el profesor..." else "Conectado. Esperando primera sincronización...", 
                    color = Color.Gray, 
                    fontSize = 14.sp
                )
                Spacer(Modifier.height(48.dp))
                CircularProgressIndicator(color = AccentCyan, strokeWidth = 3.dp)
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
            
            Spacer(Modifier.height(16.dp))
            LazyColumn(
                Modifier.fillMaxSize(),
                contentPadding = PaddingValues(bottom = 80.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp)
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
