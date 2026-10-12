package com.example.ui.screens.tabs

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Class
import androidx.compose.material.icons.filled.FolderOpen
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.data.model.UnitData
import com.example.ui.components.EmptyStateView
import com.example.ui.components.TeacherUnitCard
import com.example.ui.theme.AccentCyan
import com.example.ui.theme.AccentTeal

@Composable
fun TeacherUnitsTab(
    units: List<UnitData>,
    liveSection: String,
    liveActiveUnit: Int,
    enabledReads: String,
    enabledExams: String,
    enabledNotebooks: String,
    isServerRunning: Boolean,
    onRefreshCourse: () -> Unit,
    onOpenExplorer: () -> Unit,
    onActivateUnit: (Int) -> Unit,
    onToggleRead: (Int, Boolean) -> Unit,
    onToggleExam: (Int, Boolean) -> Unit,
    onToggleNotebook: (Int, Boolean) -> Unit,
    onGoToAula: () -> Unit
) {
    if (liveSection.isEmpty()) {
        EmptyStateView(
            icon = Icons.Default.Class,
            title = stringResource(R.string.label_no_section_selected),
            description = stringResource(R.string.desc_no_section_selected),
            buttonText = stringResource(R.string.btn_go_to_aula),
            onButtonClick = onGoToAula
        )
    } else {
        Column(Modifier.fillMaxSize()) {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Column(Modifier.weight(1f)) {
                    Text("Aula: $liveSection", fontWeight = FontWeight.Bold, color = AccentTeal)
                    Text(stringResource(R.string.desc_units_tab_header), fontSize = 12.sp, color = Color.Gray)
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(onClick = onOpenExplorer) {
                        Icon(Icons.Default.FolderOpen, "Explorar Archivos", tint = AccentCyan)
                    }
                    if (!isServerRunning) {
                        IconButton(onClick = onRefreshCourse) {
                            Icon(Icons.Default.Refresh, "Sincronizar", tint = AccentCyan)
                        }
                    }
                }
            }
            Spacer(Modifier.height(16.dp))
            LazyColumn(Modifier.weight(1f)) {
                items(units) { unit ->
                    TeacherUnitCard(
                        unit = unit,
                        isActive = liveActiveUnit == unit.index,
                        isExam = enabledExams.split(",").contains(unit.index.toString()),
                        isNotebook = enabledNotebooks.split(",").contains(unit.index.toString()),
                        onActivate = { onActivateUnit(unit.index) },
                        onToggleExam = { onToggleExam(unit.index, it) },
                        onToggleNotebook = { onToggleNotebook(unit.index, it) }
                    )
                }
            }
        }
    }
}
