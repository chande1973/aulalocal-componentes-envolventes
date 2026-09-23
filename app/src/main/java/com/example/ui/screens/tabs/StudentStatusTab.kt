package com.example.ui.screens.tabs

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BugReport
import androidx.compose.material.icons.filled.Terminal
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.data.UserProfile
import com.example.data.UnitProgress
import com.example.ui.components.AcademicStatusCard
import com.example.ui.components.SystemActionRow
import com.example.ui.theme.AccentAmber
import androidx.compose.ui.graphics.Color
import com.example.ui.components.StreakCard

@Composable
fun StudentStatusTab(
    profile: UserProfile?,
    studyProgress: List<UnitProgress>,
    units: List<com.example.data.model.UnitData>,
    totalClasses: Int,
    isLocked: Boolean,
    onAbandonClass: () -> Unit,
    onViewConsole: () -> Unit,
    onReportBug: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {
        val examGrades = studyProgress.filter { it.examCompleted && it.examGrade != null }
        val hasEvaluations = examGrades.isNotEmpty()
        val averageGrade = if (hasEvaluations) examGrades.sumOf { it.examGrade!!.toDouble() }.toFloat() / examGrades.size else 0f
        val attendancePct = if (totalClasses > 0) (profile?.attendanceCount?.toFloat() ?: 0f) / totalClasses else 0f
        val completedUnits = studyProgress.count { it.isRead || it.examCompleted }

        StreakCard(
            streakCount = profile?.attendanceCount ?: 0,
            completedUnitsCount = completedUnits
        )

        AcademicStatusCard(
            averageGrade = averageGrade,
            hasEvaluations = hasEvaluations,
            attendancePct = attendancePct,
            totalClasses = totalClasses,
            attendanceCount = profile?.attendanceCount ?: 0,
            onAbandonClass = if (isLocked) onAbandonClass else null
        )
        
        Spacer(Modifier.height(16.dp))
        
        SystemActionRow(
            icon = Icons.Default.Terminal,
            label = "Ver Consola",
            color = AccentAmber,
            onClick = onViewConsole
        )
        
        SystemActionRow(
            icon = Icons.Default.BugReport,
            label = "Reportar Fallo",
            color = Color.LightGray,
            onClick = onReportBug
        )
    }
}
