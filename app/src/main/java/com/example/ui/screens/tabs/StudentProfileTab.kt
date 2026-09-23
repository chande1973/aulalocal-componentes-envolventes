package com.example.ui.screens.tabs

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.UserProfile
import com.example.ui.avatarOptionsList
import com.example.ui.components.StudentProfileHeader
import com.example.ui.theme.*

@Composable
fun StudentProfileTab(
    profile: UserProfile?,
    isUnlocked: Boolean = false,
    onRequestUnlock: () -> Unit = {},
    onPinUnlockSuccess: () -> Unit = {},
    onSaveProfile: (UserProfile) -> Unit = {}
) {
    var firstName by remember(profile) { mutableStateOf(profile?.firstName ?: "") }
    var lastName by remember(profile) { mutableStateOf(profile?.lastName ?: "") }
    var cardId by remember(profile) { mutableStateOf(profile?.cardId ?: "") }
    var activeSection by remember(profile) { mutableStateOf(profile?.activeSection ?: "") }
    var selectedAvatarEmoji by remember(profile) { mutableStateOf(profile?.avatar?.ifEmpty { avatarOptionsList[0].emoji } ?: avatarOptionsList[0].emoji) }

    var showPinDialog by remember { mutableStateOf(false) }
    var teacherPinInput by remember { mutableStateOf("") }
    var isPinError by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {
        StudentProfileHeader(profile = profile)

        Spacer(Modifier.height(16.dp))

        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = CosmicDarkSurface),
            shape = RoundedCornerShape(16.dp),
            border = BorderStroke(1.dp, Color.Gray.copy(alpha = 0.2f))
        ) {
            Column(Modifier.padding(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Datos Personales", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = Color.White)
                    Surface(
                        color = if (isUnlocked) StatusSuccess.copy(alpha = 0.1f) else StatusError.copy(alpha = 0.1f),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Row(Modifier.padding(horizontal = 8.dp, vertical = 4.dp), verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = if (isUnlocked) Icons.Default.LockOpen else Icons.Default.Lock,
                                contentDescription = null,
                                tint = if (isUnlocked) StatusSuccess else StatusError,
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(Modifier.width(4.dp))
                            Text(
                                text = if (isUnlocked) "DESBLOQUEADO" else "BLOQUEADO",
                                color = if (isUnlocked) StatusSuccess else StatusError,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }

                OutlinedTextField(
                    value = firstName,
                    onValueChange = { firstName = it },
                    label = { Text("Nombre") },
                    enabled = isUnlocked,
                    modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp)
                )

                OutlinedTextField(
                    value = lastName,
                    onValueChange = { lastName = it },
                    label = { Text("Apellido") },
                    enabled = isUnlocked,
                    modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp)
                )

                OutlinedTextField(
                    value = cardId,
                    onValueChange = { cardId = it },
                    label = { Text("Cédula / Identificación") },
                    enabled = isUnlocked,
                    modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp)
                )

                OutlinedTextField(
                    value = activeSection,
                    onValueChange = { activeSection = it },
                    label = { Text("Sección / Aula") },
                    enabled = isUnlocked,
                    modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp)
                )

                if (isUnlocked) {
                    Text("Selecciona Avatar:", fontSize = 12.sp, color = Color.Gray, modifier = Modifier.padding(bottom = 8.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(bottom = 24.dp),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        avatarOptionsList.forEach { av ->
                            Box(
                                modifier = Modifier
                                    .size(48.dp)
                                    .clip(CircleShape)
                                    .background(if (selectedAvatarEmoji == av.emoji) av.color else Color.DarkGray)
                                    .clickable { selectedAvatarEmoji = av.emoji },
                                contentAlignment = Alignment.Center
                            ) {
                                Text(av.emoji, fontSize = 22.sp)
                            }
                        }
                    }

                    Button(
                        onClick = {
                            if (profile != null) {
                                val updated = profile.copy(
                                    firstName = firstName.trim(),
                                    lastName = lastName.trim(),
                                    cardId = cardId.trim(),
                                    activeSection = activeSection.trim(),
                                    avatar = selectedAvatarEmoji
                                )
                                onSaveProfile(updated)
                            }
                        },
                        modifier = Modifier.fillMaxWidth().height(52.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = StatusSuccess),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(Icons.Default.Save, null)
                        Spacer(Modifier.width(8.dp))
                        Text("GUARDAR CAMBIOS", fontWeight = FontWeight.ExtraBold)
                    }
                } else {
                    Button(
                        onClick = { showPinDialog = true },
                        modifier = Modifier.fillMaxWidth().height(52.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = AccentAmber),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(Icons.Default.Lock, null, tint = Color.Black)
                        Spacer(Modifier.width(8.dp))
                        Text("EDITAR DATOS (REQUIERE AUTORIZACIÓN)", fontWeight = FontWeight.Bold, color = Color.Black, fontSize = 12.sp)
                    }
                }
            }
        }
    }

    if (showPinDialog) {
        AlertDialog(
            onDismissRequest = { 
                showPinDialog = false
                teacherPinInput = ""
                isPinError = false
            },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Lock, null, tint = AccentAmber)
                    Spacer(Modifier.width(8.dp))
                    Text("Autorización del Profesor", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                }
            },
            text = {
                Column {
                    Text(
                        text = "Para editar tus datos, el profesor debe autorizar desde su panel o ingresar su PIN Maestro.",
                        fontSize = 12.sp,
                        color = Color.LightGray,
                        modifier = Modifier.padding(bottom = 16.dp)
                    )

                    OutlinedButton(
                        onClick = {
                            onRequestUnlock()
                            showPinDialog = false
                        },
                        modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp),
                        border = BorderStroke(1.dp, AccentCyan)
                    ) {
                        Icon(Icons.Default.Podcasts, null, tint = AccentCyan)
                        Spacer(Modifier.width(8.dp))
                        Text("SOLICITAR DESBLOQUEO POR WI-FI", fontSize = 11.sp, color = AccentCyan, fontWeight = FontWeight.Bold)
                    }

                    HorizontalDivider(Modifier.padding(vertical = 8.dp), color = Color.Gray.copy(alpha = 0.3f))

                    Text("O ingresa PIN Maestro del Docente:", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color.White, modifier = Modifier.padding(vertical = 8.dp))

                    OutlinedTextField(
                        value = teacherPinInput,
                        onValueChange = { 
                            teacherPinInput = it
                            isPinError = false
                        },
                        label = { Text("PIN (Default: 1234)") },
                        isError = isPinError,
                        supportingText = { if (isPinError) Text("PIN incorrecto. Intenta con '1234'", color = StatusError) },
                        visualTransformation = PasswordVisualTransformation(),
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (teacherPinInput.trim() == "1234") {
                            onPinUnlockSuccess()
                            showPinDialog = false
                            teacherPinInput = ""
                            isPinError = false
                        } else {
                            isPinError = true
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = AccentAmber)
                ) {
                    Text("DESBLOQUEAR", color = Color.Black, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { 
                    showPinDialog = false
                    teacherPinInput = ""
                    isPinError = false
                }) {
                    Text("CANCELAR")
                }
            }
        )
    }
}
