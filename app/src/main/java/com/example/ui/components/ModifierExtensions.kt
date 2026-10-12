package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.ui.theme.AccentCyan
import com.example.ui.theme.CosmicDarkSurface
import com.example.ui.theme.Dimensions

/**
 * Modificador reutilizable para tarjetas y contenedores estilo "Cosmic"
 */
fun Modifier.cosmicCardStyle(
    borderColor: Color = AccentCyan.copy(alpha = 0.2f),
    backgroundColor: Color = CosmicDarkSurface,
    cornerRadius: Dp = Dimensions.CornerLarge
): Modifier = this
    .fillMaxWidth()
    .padding(vertical = 4.dp)
    .clip(RoundedCornerShape(cornerRadius))
    .background(backgroundColor, RoundedCornerShape(cornerRadius))
    .border(1.dp, borderColor, RoundedCornerShape(cornerRadius))
