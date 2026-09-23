package com.example.ui.screens.tabs

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.data.InboxMessage
import com.example.ui.theme.AccentTeal
import com.example.ui.theme.CosmicDarkSurface

@Composable
fun StudentInboxTab(
    messages: List<InboxMessage>,
    onClearInbox: () -> Unit
) {
    Column(Modifier.fillMaxSize()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = stringResource(R.string.tab_inbox),
                style = MaterialTheme.typography.titleLarge,
                color = Color.White
            )
            TextButton(onClick = onClearInbox) {
                Text("Limpiar")
            }
        }
        
        Spacer(Modifier.height(16.dp))
        
        if (messages.isEmpty()) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("Buzón vacío", color = Color.Gray)
            }
        } else {
            LazyColumn(Modifier.fillMaxSize()) {
                items(messages) { msg ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp),
                        colors = CardDefaults.cardColors(containerColor = CosmicDarkSurface)
                    ) {
                        Column(Modifier.padding(16.dp)) {
                            Text(
                                text = msg.sender,
                                fontWeight = FontWeight.Bold,
                                color = AccentTeal,
                                fontSize = 12.sp
                            )
                            Spacer(Modifier.height(4.dp))
                            Text(
                                text = msg.content,
                                color = Color.White
                            )
                        }
                    }
                }
            }
        }
    }
}
