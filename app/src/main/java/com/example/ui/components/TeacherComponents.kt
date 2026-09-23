package com.example.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
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

@Composable
fun TeacherUnitCard(
    unit: UnitData,
    isActive: Boolean,
    isRead: Boolean,
    isExam: Boolean,
    onActivate: () -> Unit,
    onToggleRead: (Boolean) -> Unit,
    onToggleExam: (Boolean) -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = Dimensions.PaddingExtraSmall)
            .clickable { onActivate() },
        colors = CardDefaults.cardColors(
            containerColor = if (isActive) AccentTeal.copy(alpha = 0.2f) else CosmicDarkSurface
        ),
        border = if (isActive) BorderStroke(2.dp, AccentTeal) else null,
        shape = RoundedCornerShape(Dimensions.CornerMedium)
    ) {
        Row(Modifier.padding(Dimensions.PaddingMedium), verticalAlignment = Alignment.CenterVertically) {
            Icon(
                imageVector = if (isActive) Icons.Default.PlayCircle else Icons.Default.Circle,
                contentDescription = null,
                tint = if (isActive) AccentTeal else Color.Gray,
                modifier = Modifier.size(Dimensions.IconMedium)
            )
            Spacer(Modifier.width(Dimensions.PaddingMedium))
            Column(Modifier.weight(1f)) {
                Text(unit.title, color = TextPrimary, maxLines = 1, overflow = TextOverflow.Ellipsis, fontWeight = FontWeight.Medium)
                Text(unit.description, color = Color.Gray, fontSize = 10.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
            }
            
            // Switches
            TeacherToggle(label = "Leer", checked = isRead, onCheckedChange = onToggleRead, activeColor = AccentTeal)
            Spacer(Modifier.width(Dimensions.PaddingSmall))
            TeacherToggle(label = "Exam", checked = isExam, onCheckedChange = onToggleExam, activeColor = AccentAmber)
        }
    }
}

@Composable
fun TeacherToggle(label: String, checked: Boolean, onCheckedChange: (Boolean) -> Unit, activeColor: Color) {
    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.width(45.dp)) {
        Text(label, fontSize = 8.sp, color = Color.Gray)
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            colors = SwitchDefaults.colors(checkedThumbColor = activeColor),
            modifier = Modifier.scale(0.7f)
        )
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
                if (student.hasDoubt) {
                    Text("TIENE UNA DUDA", color = AccentAmber, fontWeight = FontWeight.Bold, fontSize = 9.sp)
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
