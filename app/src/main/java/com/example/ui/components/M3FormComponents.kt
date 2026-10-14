package com.example.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.AvatarOption
import com.example.ui.theme.AccentCyan
import com.example.ui.theme.CosmicDarkSurface
import com.example.ui.theme.Dimensions

/**
 * Campo de texto estilizado M3 (SleekTextField) con bordes de 12.dp y colores unificados
 */
@Composable
fun SleekTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    modifier: Modifier = Modifier,
    isError: Boolean = false,
    errorMessage: String? = null,
    singleLine: Boolean = true
) {
    Column(modifier = modifier.fillMaxWidth()) {
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            label = { Text(label, fontSize = 13.sp) },
            singleLine = singleLine,
            isError = isError,
            shape = RoundedCornerShape(Dimensions.CornerMedium),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = AccentCyan,
                unfocusedBorderColor = Color.Gray.copy(alpha = 0.3f),
                focusedLabelColor = AccentCyan,
                unfocusedLabelColor = Color.Gray,
                cursorColor = AccentCyan,
                focusedTextColor = Color.White,
                unfocusedTextColor = Color.White,
                errorBorderColor = MaterialTheme.colorScheme.error,
                errorLabelColor = MaterialTheme.colorScheme.error
            ),
            modifier = Modifier.fillMaxWidth()
        )
        if (isError && errorMessage != null) {
            Spacer(Modifier.height(4.dp))
            Text(
                text = errorMessage,
                color = MaterialTheme.colorScheme.error,
                fontSize = 11.sp,
                fontWeight = FontWeight.Medium,
                modifier = Modifier.padding(start = 4.dp)
            )
        }
    }
}

/**
 * Selector de avatares moderno M3 (M3AvatarSelector) en formato de tarjetas interactivas
 */
@Composable
fun M3AvatarSelector(
    avatarOptions: List<AvatarOption>,
    selectedEmoji: String,
    onSelectAvatar: (AvatarOption) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier.fillMaxWidth()) {
        Text(
            text = "Escoge tu Avatar",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = Color.White
        )
        Spacer(Modifier.height(12.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            avatarOptions.forEach { avatar ->
                val isSelected = avatar.emoji == selectedEmoji
                val borderColor = if (isSelected) AccentCyan else Color.Gray.copy(alpha = 0.2f)
                val containerColor = if (isSelected) AccentCyan.copy(alpha = 0.15f) else CosmicDarkSurface

                Card(
                    modifier = Modifier
                        .weight(1f)
                        .clickable { onSelectAvatar(avatar) },
                    colors = CardDefaults.cardColors(containerColor = containerColor),
                    shape = RoundedCornerShape(Dimensions.CornerMedium),
                    border = BorderStroke(if (isSelected) 2.dp else 1.dp, borderColor)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 12.dp, horizontal = 4.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .background(avatar.color.copy(alpha = 0.2f), CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(avatar.emoji, fontSize = 20.sp)
                        }
                        Spacer(Modifier.height(6.dp))
                        Text(
                            text = avatar.name,
                            color = if (isSelected) AccentCyan else Color.Gray,
                            fontSize = 11.sp,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                        )
                    }
                }
            }
        }
    }
}
