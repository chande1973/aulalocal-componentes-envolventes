package com.example.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.AccentCyan
import com.example.ui.theme.StatusSuccess

@Composable
fun ReadingProgressTopBar(
    isReadLocal: Boolean,
    scrollProgress: Float,
    fontSizeSp: Int,
    onDecreaseFontSize: () -> Unit,
    onIncreaseFontSize: () -> Unit
) {
    CosmicCard {
        Column(Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = if (isReadLocal) Icons.Default.CheckCircle else Icons.Default.MenuBook,
                        contentDescription = null,
                        tint = if (isReadLocal) StatusSuccess else AccentCyan,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(Modifier.width(8.dp))
                    Text(
                        text = if (isReadLocal) "Lectura Completada" else "Avance de Lectura",
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        fontSize = 12.sp
                    )
                }

                // Controles de tamaño de letra (A- / A+)
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Surface(
                        onClick = onDecreaseFontSize,
                        color = Color.DarkGray.copy(alpha = 0.4f),
                        shape = CircleShape,
                        modifier = Modifier.size(28.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(Icons.Default.Remove, "Reducir letra", tint = Color.White, modifier = Modifier.size(14.dp))
                        }
                    }
                    Text(
                        text = "${fontSizeSp}sp",
                        fontSize = 10.sp,
                        color = Color.Gray,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 6.dp)
                    )
                    Surface(
                        onClick = onIncreaseFontSize,
                        color = Color.DarkGray.copy(alpha = 0.4f),
                        shape = CircleShape,
                        modifier = Modifier.size(28.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(Icons.Default.Add, "Aumentar letra", tint = Color.White, modifier = Modifier.size(14.dp))
                        }
                    }
                }
            }

            Spacer(Modifier.height(8.dp))

            LinearProgressIndicator(
                progress = { if (isReadLocal) 1f else scrollProgress },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(6.dp)
                    .clip(RoundedCornerShape(3.dp)),
                color = if (isReadLocal) StatusSuccess else AccentCyan,
                trackColor = Color.DarkGray.copy(alpha = 0.5f)
            )
        }
    }
}
