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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

// --- Propuesta de Colores "Aurora Premium" ---
val AuroraBg = Color(0xFF0F172A)
val AuroraCard = Color(0xFF1E293B)
val AuroraPrimary = Color(0xFF8B5CF6) // Violeta vibrante
val AuroraSecondary = Color(0xFF06B6D4) // Cian suave
val AuroraAccent = Color(0xFF10B981) // Esmeralda

@Composable
fun PremiumDashboardProposal() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(AuroraBg)
            .padding(16.dp)
    ) {
        // Cabecera con Gradiente
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(180.dp)
                .background(
                    brush = Brush.verticalGradient(
                        colors = listOf(AuroraPrimary.copy(alpha = 0.3f), Color.Transparent)
                    ),
                    shape = RoundedCornerShape(24.dp)
                )
                .padding(24.dp)
        ) {
            Column {
                Text(
                    text = "¡Hola, Estudiante!",
                    color = Color.White,
                    fontSize = 28.sp,
                    fontWeight = FontWeight.ExtraBold
                )
                Text(
                    text = "Tienes 3 unidades nuevas disponibles",
                    color = Color.White.copy(alpha = 0.7f),
                    fontSize = 14.sp
                )
                Spacer(Modifier.height(16.dp))
                LinearProgressIndicator(
                    progress = { 0.65f },
                    modifier = Modifier.fillMaxWidth().height(8.dp),
                    color = AuroraSecondary,
                    trackColor = Color.White.copy(alpha = 0.1f),
                    strokeCap = androidx.compose.ui.graphics.StrokeCap.Round
                )
                Text(
                    text = "65% de tu curso completado",
                    color = AuroraSecondary,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(top = 8.dp)
                )
            }
        }

        Spacer(Modifier.height(24.dp))

        Text(
            text = "Continúa aprendiendo",
            color = Color.White,
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        // Tarjetas de Unidades Modernas
        PremiumUnitCard(
            title = "Unidad 1: Introducción al Aula",
            description = "Conceptos básicos y navegación",
            icon = Icons.Default.Book,
            color = AuroraPrimary,
            progress = 1f
        )
        
        PremiumUnitCard(
            title = "Unidad 2: Herramientas Digitales",
            description = "Uso de la plataforma y recursos",
            icon = Icons.Default.Laptop,
            color = AuroraSecondary,
            progress = 0.4f
        )

        PremiumUnitCard(
            title = "Unidad 3: Evaluación Final",
            description = "Prueba tus conocimientos",
            icon = Icons.Default.List,
            color = AuroraAccent,
            progress = 0f,
            isLocked = true
        )
    }
}

@Composable
fun PremiumUnitCard(
    title: String,
    description: String,
    icon: ImageVector,
    color: Color,
    progress: Float,
    isLocked: Boolean = false
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 12.dp),
        colors = CardDefaults.cardColors(containerColor = AuroraCard),
        shape = RoundedCornerShape(20.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Icono con fondo suave
            Surface(
                modifier = Modifier.size(48.dp),
                color = color.copy(alpha = 0.15f),
                shape = RoundedCornerShape(12.dp)
            ) {
                Icon(
                    imageVector = if (isLocked) Icons.Default.Lock else icon,
                    contentDescription = null,
                    tint = color,
                    modifier = Modifier.padding(12.dp)
                )
            }

            Spacer(Modifier.width(16.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp
                )
                Text(
                    text = description,
                    color = Color.White.copy(alpha = 0.5f),
                    fontSize = 12.sp
                )
            }

            if (progress > 0f) {
                CircularProgressIndicator(
                    progress = { progress },
                    modifier = Modifier.size(24.dp),
                    color = color,
                    strokeWidth = 3.dp,
                    trackColor = color.copy(alpha = 0.1f)
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun PreviewPremiumProposal() {
    PremiumDashboardProposal()
}

// --- Propuesta de Colores "Soft Pastel" ---
val PastelBg = Color(0xFFF8FAFC)
val PastelPrimary = Color(0xFF3B82F6)
val PastelSecondary = Color(0xFF8B5CF6)
val PastelAccent = Color(0xFF10B981)

@Composable
fun LightPremiumProposal() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(PastelBg)
            .padding(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(vertical = 16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text("Mis Clases", fontSize = 24.sp, fontWeight = FontWeight.Black, color = Color(0xFF1E293B))
                Text("4 de agosto, 2026", fontSize = 12.sp, color = Color.Gray)
            }
            Surface(
                modifier = Modifier.size(40.dp),
                shape = RoundedCornerShape(12.dp),
                color = PastelPrimary.copy(alpha = 0.1f)
            ) {
                Icon(Icons.Default.Person, null, tint = PastelPrimary, modifier = Modifier.padding(8.dp))
            }
        }

        Spacer(Modifier.height(16.dp))

        // Card de Progreso Circular
        Card(
            modifier = Modifier.fillMaxWidth().height(120.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            shape = RoundedCornerShape(24.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
            border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFE2E8F0))
        ) {
            Row(Modifier.padding(20.dp), verticalAlignment = Alignment.CenterVertically) {
                Box(contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(
                        progress = { 0.75f },
                        modifier = Modifier.size(60.dp),
                        color = PastelPrimary,
                        strokeWidth = 6.dp,
                        trackColor = PastelPrimary.copy(alpha = 0.1f)
                    )
                    Text("75%", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                }
                Spacer(Modifier.width(20.dp))
                Column {
                    Text("Nivel Intermedio", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = Color(0xFF1E293B))
                    Text("¡Vas por buen camino!", fontSize = 12.sp, color = Color.Gray)
                }
            }
        }

        Spacer(Modifier.height(24.dp))

        Text("Unidades Sugeridas", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = Color(0xFF1E293B))
        Spacer(Modifier.height(12.dp))

        LightUnitItem("Matemáticas Básicas", "25 min", Icons.Default.Calculate, PastelPrimary)
        LightUnitItem("Historia Universal", "40 min", Icons.Default.History, PastelSecondary)
    }
}

@Composable
fun LightUnitItem(title: String, time: String, icon: ImageVector, color: Color) {
    Surface(
        modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp),
        color = Color.White,
        shape = RoundedCornerShape(16.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFE2E8F0))
    ) {
        Row(Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            Icon(icon, null, tint = color, modifier = Modifier.size(24.dp))
            Spacer(Modifier.width(16.dp))
            Column(Modifier.weight(1f)) {
                Text(title, fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
                Text(time, fontSize = 11.sp, color = Color.Gray)
            }
            IconButton(onClick = {}) {
                Icon(Icons.Default.PlayArrow, null, tint = color)
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun PreviewLightProposal() {
    LightPremiumProposal()
}
