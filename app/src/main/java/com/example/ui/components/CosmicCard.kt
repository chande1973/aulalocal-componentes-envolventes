package com.example.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import com.example.ui.theme.AccentCyan
import com.example.ui.theme.CosmicDarkSurface

@Composable
fun CosmicCard(
    modifier: Modifier = Modifier,
    borderColor: Color = AccentCyan.copy(alpha = 0.2f),
    containerColor: Color = CosmicDarkSurface,
    content: @Composable ColumnScope.() -> Unit
) {
    Card(
        modifier = modifier.cosmicCardStyle(borderColor = borderColor, backgroundColor = containerColor),
        colors = CardDefaults.cardColors(containerColor = containerColor),
        content = content
    )
}
