package com.example.ui

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import com.example.data.model.ValidationError
import com.example.ui.screens.SetupAssistantScreen
import com.example.ui.screens.StudentDashboardScreen
import com.example.ui.screens.TeacherDashboardScreen
import com.example.ui.theme.AccentAmber
import com.example.ui.theme.AccentCyan
import com.example.ui.theme.AccentTeal
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.viewmodels.StudentViewModel
import com.example.ui.viewmodels.TeacherViewModel

// ============================================================
// SHARED UI MODELS
// ============================================================

data class AvatarOption(val name: String, val emoji: String, val color: Color, val desc: String)

val avatarOptionsList = listOf(
    AvatarOption("Geek", "🤓", AccentTeal, "Hardware y programación"),
    AvatarOption("SysAdmin", "🖥️", AccentCyan, "Sistemas y redes"),
    AvatarOption("Soporte", "🔧", AccentAmber, "Mantenimiento"),
    AvatarOption("Redes", "📡", Color(0xFFA855F7), "Conexiones WiFi")
)

sealed interface ValidationUiState {
    object Idle : ValidationUiState
    object Validating : ValidationUiState
    data class Success(val warnings: List<ValidationError> = emptyList()) : ValidationUiState
    data class Error(val errors: List<ValidationError>) : ValidationUiState
}

// ============================================================
// MAIN ENTRY POINT
// ============================================================

@Composable
fun ClassroomMainScreen(
    mainViewModel: MainViewModel,
    teacherViewModel: TeacherViewModel,
    studentViewModel: StudentViewModel
) {
    val userProfile by mainViewModel.userProfile.collectAsState()

    MyApplicationTheme(darkTheme = true) {
        Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
            when {
                userProfile == null -> SetupAssistantScreen(mainViewModel, studentViewModel)
                userProfile?.role == "teacher" -> TeacherDashboardScreen(mainViewModel, teacherViewModel)
                userProfile?.role == "student" -> StudentDashboardScreen(mainViewModel, studentViewModel)
            }
        }
    }
}
