package com.example.ui.theme

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

// ============================================================
// COLORES PROPUESTA "NEO ACADEMIA"
// ============================================================
private val NeoBg = Color(0xFF0B1120)
private val NeoSurface = Color(0xFF162032)
private val NeoCard = Color(0xFF1E2A3A)
private val NeoPrimary = Color(0xFF2DD4BF)
private val NeoSecondary = Color(0xFF38BDF8)
private val NeoSuccess = Color(0xFF4ADE80)
private val NeoError = Color(0xFFF87171)
private val NeoWarning = Color(0xFFFBBF24)
private val NeoTextPrimary = Color(0xFFF1F5F9)
private val NeoTextMuted = Color(0xFF94A3B8)

// ============================================================
// COMPONENTES DE DEMO
// ============================================================

@Composable
private fun DemoCard(title: String, subtitle: String, colorScheme: String) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = colorScheme,
            color = AccentTeal,
            fontSize = 11.sp,
            fontWeight = FontWeight.ExtraBold,
            modifier = Modifier.padding(bottom = 8.dp)
        )
        // TopAppBar
        Surface(
            color = CosmicDarkSurface,
            shape = RoundedCornerShape(topStart = 12.dp, topEnd = 12.dp)
        ) {
            Row(
                Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(title, color = Color.White, fontWeight = FontWeight.ExtraBold, fontSize = 16.sp)
                Spacer(Modifier.weight(1f))
                // Status dot
                Box(
                    Modifier
                        .size(10.dp)
                        .clip(CircleShape)
                        .background(AccentTeal)
                )
            }
        }
        
        // Content
        Column(
            modifier = Modifier
                .background(CosmicDarkBg)
                .padding(12.dp)
        ) {
            // Banner
            Card(
                colors = CardDefaults.cardColors(containerColor = CosmicDarkCard),
                shape = RoundedCornerShape(8.dp)
            ) {
                Row(Modifier.padding(10.dp), verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Campaign, null, tint = AccentAmber, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(8.dp))
                    Text(subtitle, color = Color.White, fontSize = 11.sp)
                }
            }
            
            Spacer(Modifier.height(8.dp))
            
            // Unit cards
            repeat(2) { i ->
                Card(
                    Modifier.fillMaxWidth().padding(vertical = 3.dp),
                    colors = CardDefaults.cardColors(containerColor = CosmicDarkSurface),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Row(Modifier.padding(10.dp), verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            if (i == 0) Icons.AutoMirrored.Filled.MenuBook else Icons.Default.Lock,
                            null,
                            tint = if (i == 0) AccentCyan else Color.Gray,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(Modifier.width(10.dp))
                        Text(
                            if (i == 0) "Unidad 1: Introducción" else "Unidad 2: Bloqueada",
                            fontSize = 12.sp,
                            color = if (i == 0) Color.White else Color.Gray
                        )
                    }
                }
            }
            
            Spacer(Modifier.height(8.dp))
            
            // Progress bars
            Column {
                Text("Promedio: 14.50", color = Color.White, fontSize = 11.sp)
                LinearProgressIndicator(
                    progress = { 0.72f },
                    modifier = Modifier.fillMaxWidth().height(6.dp).clip(CircleShape),
                    color = AccentCyan,
                    trackColor = CosmicDarkCard
                )
                Spacer(Modifier.height(6.dp))
                Text("Asistencia: 75%", color = Color.White, fontSize = 11.sp)
                LinearProgressIndicator(
                    progress = { 0.75f },
                    modifier = Modifier.fillMaxWidth().height(6.dp).clip(CircleShape),
                    color = StatusSuccess,
                    trackColor = CosmicDarkCard
                )
            }
            
            Spacer(Modifier.height(8.dp))
            
            // Button
            Button(
                onClick = {},
                modifier = Modifier.fillMaxWidth().height(40.dp),
                colors = ButtonDefaults.buttonColors(containerColor = AccentAmber),
                shape = RoundedCornerShape(8.dp)
            ) {
                Text("PRESENTE", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color.Black)
            }
        }
        
        // Bottom Nav
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(CosmicDarkSurface)
                .padding(vertical = 6.dp),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            listOf("Clase" to Icons.AutoMirrored.Filled.MenuBook, "Buzón" to Icons.Default.Mail, "Perfil" to Icons.Default.Person).forEach { (label, icon) ->
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(icon, null, tint = if (label == "Clase") AccentCyan else Color.Gray, modifier = Modifier.size(18.dp))
                    Text(label, fontSize = 9.sp, color = if (label == "Clase") AccentCyan else Color.Gray)
                }
            }
        }
    }
}

@Composable
private fun NeoDemoCard(title: String, subtitle: String, colorScheme: String) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = colorScheme,
            color = NeoPrimary,
            fontSize = 11.sp,
            fontWeight = FontWeight.ExtraBold,
            modifier = Modifier.padding(bottom = 8.dp)
        )
        // TopAppBar
        Surface(
            color = NeoSurface,
            shape = RoundedCornerShape(topStart = 14.dp, topEnd = 14.dp)
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(title, color = NeoTextPrimary, fontWeight = FontWeight.ExtraBold, fontSize = 16.sp)
                Spacer(Modifier.weight(1f))
                Box(
                    Modifier
                        .size(10.dp)
                        .clip(CircleShape)
                        .background(NeoPrimary)
                )
            }
        }

        // Content
        Column(
            modifier = Modifier
                .background(NeoBg)
                .padding(12.dp)
        ) {
            // Banner
            Card(
                colors = CardDefaults.cardColors(containerColor = NeoCard),
                shape = RoundedCornerShape(10.dp)
            ) {
                Row(Modifier.padding(10.dp), verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Campaign, contentDescription = null, tint = NeoWarning, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(8.dp))
                    Text(subtitle, color = NeoTextPrimary, fontSize = 11.sp)
                }
            }

            Spacer(Modifier.height(8.dp))

            // Unit cards
            repeat(2) { i ->
                Card(
                    Modifier.fillMaxWidth().padding(vertical = 3.dp),
                    colors = CardDefaults.cardColors(containerColor = NeoSurface),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Row(Modifier.padding(10.dp), verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            if (i == 0) Icons.AutoMirrored.Filled.MenuBook else Icons.Default.Lock,
                            null,
                            tint = if (i == 0) NeoSecondary else Color.Gray,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(Modifier.width(10.dp))
                        Text(
                            if (i == 0) "Unidad 1: Introducción" else "Unidad 2: Bloqueada",
                            fontSize = 12.sp,
                            color = if (i == 0) NeoTextPrimary else Color.Gray
                        )
                    }
                }
            }

            Spacer(Modifier.height(8.dp))

            // Progress bars
            Column {
                Text("Promedio: 8.50", color = NeoTextPrimary, fontSize = 11.sp)
                LinearProgressIndicator(
                    progress = { 0.72f },
                    modifier = Modifier.fillMaxWidth().height(6.dp).clip(CircleShape),
                    color = NeoSecondary,
                    trackColor = NeoCard
                )
                Spacer(Modifier.height(6.dp))
                Text("Asistencia: 75%", color = NeoTextPrimary, fontSize = 11.sp)
                LinearProgressIndicator(
                    progress = { 0.75f },
                    modifier = Modifier.fillMaxWidth().height(6.dp).clip(CircleShape),
                    color = NeoSuccess,
                    trackColor = NeoCard
                )
            }

            Spacer(Modifier.height(8.dp))

            // Button
            Button(
                onClick = {},
                modifier = Modifier.fillMaxWidth().height(40.dp),
                colors = ButtonDefaults.buttonColors(containerColor = NeoWarning),
                shape = RoundedCornerShape(10.dp)
            ) {
                Text("PRESENTE", color = Color.Black, fontSize = 11.sp, fontWeight = FontWeight.Bold)
            }
        }
        
        // Bottom NavBar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(NeoSurface)
                .clip(RoundedCornerShape(bottomStart = 14.dp, bottomEnd = 14.dp))
                .padding(vertical = 6.dp),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            listOf("Clase" to Icons.AutoMirrored.Filled.MenuBook, "Buzón" to Icons.Default.Mail, "Perfil" to Icons.Default.Person).forEach { (label, icon) ->
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(icon, null, tint = if (label == "Clase") NeoPrimary else Color.Gray, modifier = Modifier.size(18.dp))
                    Text(label, fontSize = 9.sp, color = if (label == "Clase") NeoPrimary else Color.Gray)
                }
            }
        }
    }
}

// ============================================================
// PREVIEWS
// ============================================================

@Preview(name = "🎨 ACTUAL - Dashboard", showBackground = true, widthDp = 360, heightDp = 800)
@Composable
fun Preview_ActualDashboard() {
    MaterialTheme {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color(0xFF0B1120))
                .verticalScroll(rememberScrollState())
                .padding(12.dp)
        ) {
            DemoCard(
                title = "Aula Local",
                subtitle = "Profesor: Bienvenidos a nueva lección",
                colorScheme = "🎨 TEMA ACTUAL"
            )
        }
    }
}

@Preview(name = "✨ PROPUESTA - Neo Academia", showBackground = true, widthDp = 360, heightDp = 800)
@Composable
private fun Preview_NeoDashboard() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color(0xFF0B1120))
            .padding(12.dp)
    ) {
        NeoDemoCard(
            title = "Aula Local",
            subtitle = "Profesor: Bienvenidos a clase",
            colorScheme = "✨ PROPUESTA: Neo Academia"
        )
    }
}