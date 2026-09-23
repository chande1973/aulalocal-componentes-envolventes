package com.example.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R

@Preview(showBackground = true, widthDp = 380, heightDp = 800)
@Composable
fun AppIconProposalsDisplay() {
    Surface(
        modifier = Modifier.fillMaxSize(),
        color = Color(0xFF0F172A)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceEvenly
        ) {
            Text(
                text = "Propuestas de Icono para Aula Local",
                color = Color.White,
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp
            )

            // Opción 1: Escudo Académico Conectado
            IconProposalItem(
                title = "Opción 1: Escudo Académico Conectado",
                subtitle = "Elegante, Institucional y Seguro",
                bgRes = R.drawable.ic_launcher_prop1_bg,
                fgRes = R.drawable.ic_launcher_prop1_fg
            )

            // Opción 2: Libro Digital Glow 3D
            IconProposalItem(
                title = "Opción 2: Libro Digital Glow 3D",
                subtitle = "Tecnológico, Llamativo e Innovador",
                bgRes = R.drawable.ic_launcher_prop2_bg,
                fgRes = R.drawable.ic_launcher_prop2_fg
            )

            // Opción 3: A + Nodo de Red
            IconProposalItem(
                title = "Opción 3: A + Nodo de Red",
                subtitle = "Minimalista, Limpio e Iconográfico",
                bgRes = R.drawable.ic_launcher_prop3_bg,
                fgRes = R.drawable.ic_launcher_prop3_fg
            )
        }
    }
}

@Composable
fun IconProposalItem(title: String, subtitle: String, bgRes: Int, fgRes: Int) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B)),
        shape = RoundedCornerShape(16.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(80.dp)
                    .clip(CircleShape)
                    .border(2.dp, Color(0xFF06B6D4), CircleShape)
            ) {
                Image(
                    painter = painterResource(id = bgRes),
                    contentDescription = null,
                    modifier = Modifier.fillMaxSize()
                )
                Image(
                    painter = painterResource(id = fgRes),
                    contentDescription = null,
                    modifier = Modifier.fillMaxSize()
                )
            }
            Spacer(Modifier.width(16.dp))
            Column {
                Text(title, fontWeight = FontWeight.Bold, color = Color.White, fontSize = 13.sp)
                Text(subtitle, color = Color.LightGray, fontSize = 11.sp)
            }
        }
    }
}
