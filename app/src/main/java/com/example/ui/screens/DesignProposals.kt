package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.*

@Composable
fun MockDashboard(
    primaryColor: Color,
    surfaceColor: Color,
    bgColor: Color,
    accentColor: Color,
    isDark: Boolean
) {
    Box(Modifier.fillMaxSize().background(bgColor)) {
        Column(Modifier.padding(16.dp)) {
            // Header
            Row(
                Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text("Hola, Estudiante", color = if (isDark) Color.White else Color.Black, fontSize = 24.sp, fontWeight = FontWeight.Bold)
                    Text("Unidad 3: Introducción a la IA", color = if (isDark) Color.White.copy(alpha = 0.7f) else Color.Black.copy(alpha = 0.6f), fontSize = 14.sp)
                }
                Icon(Icons.Default.AccountCircle, null, tint = accentColor, modifier = Modifier.size(40.dp))
            }

            Spacer(Modifier.height(24.dp))

            // Stats Row
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                Card(Modifier.weight(1f), colors = CardDefaults.cardColors(containerColor = surfaceColor)) {
                    Column(Modifier.padding(16.dp)) {
                        Text("Asistencia", color = accentColor, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        Text("92%", color = if (isDark) Color.White else Color.Black, fontSize = 20.sp, fontWeight = FontWeight.Bold)
                    }
                }
                Card(Modifier.weight(1f), colors = CardDefaults.cardColors(containerColor = surfaceColor)) {
                    Column(Modifier.padding(16.dp)) {
                        Text("Promedio", color = accentColor, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        Text("18.5", color = if (isDark) Color.White else Color.Black, fontSize = 20.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }

            Spacer(Modifier.height(24.dp))

            // Main Content Area
            Text("Lecciones de Hoy", color = if (isDark) Color.White else Color.Black, fontWeight = FontWeight.Bold, modifier = Modifier.padding(bottom = 12.dp))
            
            repeat(2) { index ->
                Card(
                    Modifier.fillMaxWidth().padding(bottom = 12.dp),
                    colors = CardDefaults.cardColors(containerColor = surfaceColor),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Row(Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                        Box(Modifier.size(40.dp).background(accentColor.copy(alpha = 0.2f), RoundedCornerShape(8.dp)), contentAlignment = Alignment.Center) {
                            Icon(if (index == 0) Icons.Default.MenuBook else Icons.Default.Headset, null, tint = accentColor)
                        }
                        Spacer(Modifier.width(16.dp))
                        Column {
                            Text(if (index == 0) "Lectura: Fundamentos" else "Audio: Historia de la IA", color = if (isDark) Color.White else Color.Black, fontWeight = FontWeight.Bold)
                            Text("15 min • Completado", color = if (isDark) Color.White.copy(alpha = 0.5f) else Color.Black.copy(alpha = 0.4f), fontSize = 12.sp)
                        }
                    }
                }
            }
        }

        // FAB
        ExtendedFloatingActionButton(
            onClick = {},
            modifier = Modifier.align(Alignment.BottomEnd).padding(24.dp),
            containerColor = accentColor,
            contentColor = if (accentColor == AccentAmber) Color.Black else Color.White
        ) {
            Icon(Icons.Default.QuestionAnswer, null)
            Spacer(Modifier.width(8.dp))
            Text("TENGO UNA DUDA")
        }
    }
}

@Preview(showBackground = true, name = "Cosmic Glow (Proposal 1)")
@Composable
fun PreviewCosmicGlow() {
    MockDashboard(
        primaryColor = AccentCyan,
        surfaceColor = CosmicDarkSurface,
        bgColor = CosmicDarkBg,
        accentColor = AccentCyan,
        isDark = true
    )
}

@Preview(showBackground = true, name = "Arctic Academic (Proposal 2)")
@Composable
fun PreviewArcticAcademic() {
    MockDashboard(
        primaryColor = LightSecondary,
        surfaceColor = Color.White,
        bgColor = LightBg,
        accentColor = LightSecondary,
        isDark = false
    )
}

@Preview(showBackground = true, name = "Sunset Learning (Proposal 3)")
@Composable
fun PreviewSunsetLearning() {
    MockDashboard(
        primaryColor = AccentAmber,
        surfaceColor = CosmicDarkSurface,
        bgColor = CosmicDarkBg,
        accentColor = AccentAmber,
        isDark = true
    )
}
