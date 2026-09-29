package com.example.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.SyncedStudent
import com.example.data.model.UnitData
import com.example.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TeacherUnitCard(
    unit: UnitData,
    isActive: Boolean,
    isExam: Boolean,
    onActivate: () -> Unit,
    onToggleExam: (Boolean) -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
            .clickable { onActivate() },
        colors = CardDefaults.cardColors(
            containerColor = if (isActive) AccentTeal.copy(alpha = 0.15f) else CosmicDarkSurface
        ),
        border = if (isActive) BorderStroke(2.dp, AccentTeal) else BorderStroke(1.dp, Color.Gray.copy(alpha = 0.2f)),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(Modifier.padding(14.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(
                    imageVector = if (isActive) Icons.Default.Sensors else Icons.AutoMirrored.Filled.MenuBook,
                    contentDescription = null,
                    tint = if (isActive) StatusSuccess else Color.Gray,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(Modifier.width(12.dp))
                Column(Modifier.weight(1f)) {
                    Text(
                        text = "Unidad ${unit.index}: ${unit.title}",
                        color = TextPrimary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp
                    )
                    Text(
                        text = unit.description,
                        color = Color.Gray,
                        fontSize = 11.sp,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }

            Spacer(Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Button(
                    onClick = onActivate,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (isActive) StatusSuccess else AccentTeal
                    ),
                    shape = RoundedCornerShape(8.dp),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 2.dp),
                    modifier = Modifier.height(36.dp)
                ) {
                    Icon(
                        imageVector = if (isActive) Icons.Default.Sensors else Icons.Default.PlayArrow,
                        contentDescription = null,
                        tint = if (isActive) Color.White else Color.Black,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(Modifier.width(6.dp))
                    Text(
                        text = if (isActive) "IMPARTIENDO EN VIVO" else "IMPARTIR CLASE",
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 11.sp,
                        color = if (isActive) Color.White else Color.Black
                    )
                }

                FilterChip(
                    selected = isExam,
                    onClick = { onToggleExam(!isExam) },
                    label = {
                        Text(
                            text = if (isExam) "📝 EXAMEN ACTIVO" else "📝 EXAMEN",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold
                        )
                    },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = AccentAmber,
                        selectedLabelColor = Color.Black
                    )
                )
            }
        }
    }
}

@Composable
fun StudentRowTeacher(
    student: SyncedStudent,
    isOyente: Boolean,
    onMessage: (SyncedStudent) -> Unit,
    onAttach: (SyncedStudent) -> Unit,
    onDoubt: (SyncedStudent) -> Unit,
    onEnroll: (SyncedStudent) -> Unit
) {
    val isOnline = (System.currentTimeMillis() - student.lastSeen) < 15000
    
    Card(
        Modifier
            .fillMaxWidth()
            .padding(vertical = Dimensions.PaddingExtraSmall)
            .clickable { if (student.hasDoubt) onDoubt(student) else onMessage(student) },
        colors = CardDefaults.cardColors(containerColor = if (isOnline) CosmicDarkSurface else Color.DarkGray.copy(alpha = 0.2f)),
        shape = RoundedCornerShape(Dimensions.CornerMedium),
        border = if (isOyente) BorderStroke(1.dp, StatusError.copy(alpha = 0.5f)) else null
    ) {
        Row(Modifier.padding(Dimensions.PaddingMedium), verticalAlignment = Alignment.CenterVertically) {
            Box(
                Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(if (isOnline) (if (isOyente) StatusError else AccentTeal) else Color.Gray),
                contentAlignment = Alignment.Center
            ) {
                Text(student.avatar, fontSize = 18.sp)
            }
            Spacer(Modifier.width(Dimensions.PaddingMedium))
            Column(Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(text = "${student.lastName}, ${student.firstName}", color = TextPrimary, fontWeight = FontWeight.Bold, maxLines = 1, overflow = TextOverflow.Ellipsis)
                    if (isOyente) {
                        Spacer(Modifier.width(Dimensions.PaddingSmall))
                        Surface(color = StatusError, shape = RoundedCornerShape(4.dp)) {
                            Text("OYENTE", color = Color.White, fontSize = 8.sp, modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp), fontWeight = FontWeight.ExtraBold)
                        }
                    }
                }
                Text(if (isOyente) "Sección: ${student.sectionName}" else student.cardId, fontSize = 10.sp, color = if (isOyente) StatusError.copy(alpha = 0.7f) else Color.Gray)
                if (student.isProfileUnlockRequested) {
                    Text("🔓 SOLICITA DESBLOQUEAR PERFIL", color = StatusWarning, fontWeight = FontWeight.ExtraBold, fontSize = 9.sp)
                } else if (student.hasDoubt) {
                    Text("💬 MENSAJE: ${student.doubtMessage.take(25)}", color = AccentAmber, fontWeight = FontWeight.Bold, fontSize = 9.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
                }
            }

            if (isOnline) {
                Row {
                    if (isOyente) {
                        IconButton(onClick = { onEnroll(student) }) {
                            Icon(Icons.Default.PersonAdd, null, tint = StatusSuccess, modifier = Modifier.size(Dimensions.IconSmall))
                        }
                    }
                    IconButton(onClick = { onAttach(student) }) {
                        Icon(Icons.Default.AttachFile, null, tint = AccentAmber, modifier = Modifier.size(Dimensions.IconSmall))
                    }
                    IconButton(onClick = { onMessage(student) }) {
                        Icon(Icons.Default.ChatBubbleOutline, null, tint = AccentCyan, modifier = Modifier.size(Dimensions.IconSmall))
                    }
                }
            }
            Box(Modifier.size(8.dp).clip(CircleShape).background(if (isOnline) StatusSuccess else Color.Gray))
        }
    }
}
